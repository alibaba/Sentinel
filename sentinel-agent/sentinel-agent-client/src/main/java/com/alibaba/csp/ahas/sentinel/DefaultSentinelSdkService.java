package com.alibaba.csp.ahas.sentinel;

import com.alibaba.csp.ahas.sentinel.util.MachineUtils;
import com.taobao.csp.ahas.service.api.client.ClientInfoService;
import com.taobao.csp.ahas.service.api.transport.TransportService;
import com.taobao.csp.ahas.service.client.DefaultClientInfoService;
import com.taobao.csp.ahas.service.heartbeat.DefaultHeartbeatService;
import com.taobao.csp.ahas.service.heartbeat.HeartbeatService;
//import com.taobao.csp.ahas.service.init.AhasInitFunc;
import com.taobao.csp.ahas.service.transport.DefaultTransportService;
import com.taobao.csp.ahas.transport.api.Response;
import com.taobao.csp.ahas.transport.api.ServiceConstants;
import com.taobao.middleware.logger.support.LogLog;
import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.cluster.server.command.handler.FetchClusterServerInfoCommandHandler;
import com.alibaba.csp.sentinel.cluster.server.command.handler.ModifyClusterServerFlowConfigHandler;
import com.alibaba.csp.sentinel.cluster.server.command.handler.ModifyServerNamespaceSetHandler;
import com.alibaba.csp.sentinel.command.handler.FetchActiveRuleCommandHandler;
import com.alibaba.csp.sentinel.command.handler.FetchClusterClientConfigHandler;
import com.alibaba.csp.sentinel.command.handler.FetchJsonTreeCommandHandler;
import com.alibaba.csp.sentinel.command.handler.FetchSimpleClusterNodeCommandHandler;
import com.alibaba.csp.sentinel.command.handler.ModifyClusterClientConfigHandler;
import com.alibaba.csp.sentinel.command.handler.OnOffGetCommandHandler;
import com.alibaba.csp.sentinel.command.handler.OnOffSetCommandHandler;
import com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler;
import com.alibaba.csp.sentinel.command.handler.VersionCommandHandler;
import com.alibaba.csp.sentinel.command.handler.cluster.FetchClusterModeCommandHandler;
import com.alibaba.csp.sentinel.command.handler.cluster.ModifyClusterModeCommandHandler;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.init.InitOrder;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.heartbeat.HeartbeatMessage;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpClient;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpRequest;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpResponse;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


@SpiOrder(-10000)
@InitOrder(Integer.MIN_VALUE)
public class DefaultSentinelSdkService implements InitFunc, HeartbeatSender {
    public static final String SENTINEL_ENV_CLASS = "com.alibaba.csp.sentinel.Env";
    private static final ClientInfoService clientInfoService = new DefaultClientInfoService();
    private static final TransportService transportService = new DefaultTransportService();
    private static final HeartbeatService heartbeatService = new DefaultHeartbeatService();
    private static final String HEARTBEAT_PATH = "/registry/machine";
    private static final int OK_STATUS = 200;

    private static final long DEFAULT_INTERVAL = 1000 * 10;

    private final HeartbeatMessage heartBeat = new HeartbeatMessage();
    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    private final List<InetSocketAddress> addressList;

    private static final AtomicBoolean isAhasInit = new AtomicBoolean(false);
    private static final AtomicBoolean isCommandHandlerRegistered = new AtomicBoolean(false);
    private static final AtomicBoolean ahasInitSuccess = new AtomicBoolean(false);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("DefaultSentinelSdkService-thread"));

    private int currentAddressIdx = 0;

    public DefaultSentinelSdkService() {
        List<InetSocketAddress> newAddrs = getDefaultConsoleIps();
        RecordLog.info("[SimpleHttpHeartbeatSender] Default console address list retrieved: " + newAddrs);
        this.addressList = newAddrs;
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    if (!DefaultSentinelSdkService.isCommandHandlerRegistered.compareAndSet(false, true)) {
                        DefaultSentinelSdkService.this.registerCommandHandler();
                    }
                } catch (Throwable var3) {
                    RecordLog.info("[DefaultSentinelSdkService] registerCommandHandler error", var3);
                }

                try {
                   // DefaultSentinelSdkService.this.initAhas();
                    DefaultSentinelSdkService.ahasInitSuccess.set(true);
                } catch (Throwable var2) {
                    RecordLog.warn("[DefaultSentinelSdkService] init ahas error", var2);
                    System.err.println("ERROR: AHAS init fail, ahas.license is needed");
                }

            }
        };
        executor.submit(runnable);
    }

    public void init() throws Exception {
        Runnable runnable = new Runnable() {
            public void run() {
                if (DefaultSentinelSdkService.ahasInitSuccess.get()) {
                    try {
                        LogLog.setQuietMode(true);
                        MachineUtils.setCurrentProcessConfigurationId(DefaultSentinelSdkService.clientInfoService.getAid());
                        SentinelAcmDataSourceService acmDataSourceService = new SentinelAcmDataSourceService(DefaultSentinelSdkService.clientInfoService);
                        acmDataSourceService.initAcmDataSource();
                        DefaultSentinelSdkService.this.initSentinel();
                    } catch (Throwable var2) {
                        RecordLog.warn("[DefaultSentinelSdkService] init acm error", var2);
                    }

                }
            }
        };
        executor.submit(runnable);
    }

//    private void initAhas() throws Exception {
//        if (isAhasInit.compareAndSet(false, true)) {
//            System.setProperty("ahas.version", this.getWholeVersion("1.4.2"));
//            int times = 0;
//
//            while(true) {
//                try {
//                    ((AhasInitFunc)clientInfoService).init("JAVA_SDK", (ClassLoader)null);
//                    transportService.init(clientInfoService);
//                    heartbeatService.init(clientInfoService, transportService);
//                    break;
//                } catch (Throwable var5) {
//                    RecordLog.info("[DefaultSentinelSdkService] init ahas fail, will retry " + times, var5);
//                    ++times;
//
//                    try {
//                        Thread.sleep(10000L);
//                    } catch (Exception var4) {
//                    }
//                }
//            }
//
//            RecordLog.info("[DefaultSentinelSdkService] AHAS gateway host: " + clientInfoService.getGatewayHost() + "port:" + clientInfoService.getGatewayPort());
//            RecordLog.info("[DefaultSentinelSdkService] Really init AHAS");
//            AhasGlobalContext.setClientInfoService(clientInfoService);
//        }
//    }

    private String getWholeVersion(String defaultGwVersion) {
        String coreVersion = Constants.SENTINEL_VERSION;
        String gwVersion = defaultGwVersion;

        String version;
        try {
            version = DefaultSentinelSdkService.class.getPackage().getImplementationVersion();
            if (StringUtil.isNotBlank(version)) {
                gwVersion = version;
            }
        } catch (Throwable var5) {
            RecordLog.warn("[DefaultSentinelSdkService] Using default version, ignore exception", var5);
        }

        version = coreVersion + "_" + gwVersion;
        RecordLog.info("[DefaultSentinelSdkService] wholeVersion: " + version);
        return version;
    }

    private void initSentinel() throws Exception {
        try {
            this.getClass().getClassLoader().loadClass("com.alibaba.csp.sentinel.Env");
        } catch (ClassNotFoundException var2) {
            RecordLog.warn("[DefaultSentinelSdkService][ERROR] com.alibaba.csp.sentinel.Env not found!", (Throwable)var2);
        }

    }

    private void registerCommandHandler() {
        transportService.registerHandler(ServiceConstants.Sentinel.CLUSTER_NODE.getHandlerName(), new SentinelRequestHandler(new FetchSimpleClusterNodeCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.JSON_TREE.getHandlerName(), new SentinelRequestHandler(new FetchJsonTreeCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.METRIC.getHandlerName(), new SentinelRequestHandler(new SendMetricCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.GET_RULES.getHandlerName(), new SentinelRequestHandler(new FetchActiveRuleCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.VERSION.getHandlerName(), new SentinelRequestHandler(new VersionCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.GET_SWITCH.getHandlerName(), new SentinelRequestHandler(new OnOffGetCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.SET_SWITCH.getHandlerName(), new SentinelRequestHandler(new OnOffSetCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.SET_CLUSTER_MODE.getHandlerName(), new SentinelRequestHandler(new ModifyClusterModeCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.GET_CLUSTER_MODE.getHandlerName(), new SentinelRequestHandler(new FetchClusterModeCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.GET_CLUSTER_CLIENT_INFO.getHandlerName(), new SentinelRequestHandler(new FetchClusterClientConfigHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.MODIFY_CLUSTER_CLIENT_CONFIG.getHandlerName(), new SentinelRequestHandler(new ModifyClusterClientConfigHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.GET_CLUSTER_SERVER_INFO.getHandlerName(), new SentinelRequestHandler(new FetchClusterServerInfoCommandHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.MODIFY_CLUSTER_SERVER_FLOW_CONFIG.getHandlerName(), new SentinelRequestHandler(new ModifyClusterServerFlowConfigHandler()));
        transportService.registerHandler(ServiceConstants.Sentinel.MODIFY_CLUSTER_SERVER_NAMESPACE_SET.getHandlerName(), new SentinelRequestHandler(new ModifyServerNamespaceSetHandler()));
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (TransportConfig.getRuntimePort() <= 0) {
            RecordLog.info("[SimpleHttpHeartbeatSender] Runtime port not initialized, won't send heartbeat");
            return false;
        }
        InetSocketAddress addr = getAvailableAddress();
        if (addr == null) {
            return false;
        }

        SimpleHttpRequest request = new SimpleHttpRequest(addr, HEARTBEAT_PATH);
        request.setParams(heartBeat.generateCurrentMessage());
        try {
            SimpleHttpResponse response = httpClient.post(request);
            if (response.getStatusCode() == OK_STATUS) {
                return true;
            }
        } catch (Exception e) {
            RecordLog.warn("[SimpleHttpHeartbeatSender] Failed to send heartbeat to " + addr + " : ", e);
        }
        return false;
    }


    public long intervalMs() {
        return 5000L;
    }

    private InetSocketAddress getAvailableAddress() {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }
        if (currentAddressIdx < 0) {
            currentAddressIdx = 0;
        }
        int index = currentAddressIdx % addressList.size();
        return addressList.get(index);
    }

    private List<InetSocketAddress> getDefaultConsoleIps() {
        List<InetSocketAddress> newAddrs = new ArrayList<InetSocketAddress>();
        try {
            String ipsStr = TransportConfig.getConsoleServer();
            if (StringUtil.isEmpty(ipsStr)) {
                RecordLog.warn("[SimpleHttpHeartbeatSender] Dashboard server address not configured");
                return newAddrs;
            }

            for (String ipPortStr : ipsStr.split(",")) {
                if (ipPortStr.trim().length() == 0) {
                    continue;
                }
                if (ipPortStr.startsWith("http://")) {
                    ipPortStr = ipPortStr.trim().substring(7);
                }
                String[] ipPort = ipPortStr.trim().split(":");
                int port = 80;
                if (ipPort.length > 1) {
                    port = Integer.parseInt(ipPort[1].trim());
                }
                newAddrs.add(new InetSocketAddress(ipPort[0].trim(), port));
            }
        } catch (Exception ex) {
            RecordLog.warn("[SimpleHeartbeatSender] Parse dashboard list failed, current address list: " + newAddrs, ex);
            ex.printStackTrace();
        }
        return newAddrs;
    }


//   public boolean sendHeartbeat() throws Exception {
//      this.initAhas();
//      Response<?> mapResponse = heartbeatService.sendHeartbeat();
//      return mapResponse.isSuccess();
////      if (!ahasInitSuccess.get()) {
////         return false;
////      } else {
////         Response<?> mapResponse = heartbeatService.sendHeartbeat();
////         if (!mapResponse.isSuccess()) {
////            throw new RuntimeException(mapResponse.toString());
////         } else {
////            return mapResponse.isSuccess();
////         }
////      }
//
//
//
//   }
}
