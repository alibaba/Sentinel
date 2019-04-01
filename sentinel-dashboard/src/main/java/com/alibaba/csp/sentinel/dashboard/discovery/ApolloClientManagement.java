package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Apollo Client Management
 *
 * @author longqiang
 */
@Component
public class ApolloClientManagement {

    private static AppManagement appManagement;
    private static final Map<String, ApolloOpenApiClient> clientPool = new ConcurrentHashMap<>(16);
    private static final Map<MachineInfo, String> portalMapping = new ConcurrentHashMap<>(16);

    public ApolloClientManagement(AppManagement appManagement) {
        ApolloClientManagement.appManagement = appManagement;
    }

    public static ApolloOpenApiClient getOrCreateClient(ApolloMachineInfo apolloMachineInfo){
        return getOrCreate(apolloMachineInfo.getPortalUrl(), apolloMachineInfo.getToken(), apolloMachineInfo.getConnectTimeout(), apolloMachineInfo.getReadTimeout());
    }

    public static ApolloOpenApiClient getOrCreate(String portalUrl, String token, int connectTimeout, int readTimeout) {
        return clientPool.computeIfAbsent(portalUrl, e -> ApolloOpenApiClient.newBuilder()
                                                                              .withPortalUrl(portalUrl)
                                                                              .withConnectTimeout(connectTimeout)
                                                                              .withReadTimeout(readTimeout)
                                                                              .withToken(token)
                                                                              .build());
    }

    public static String getOrCreatePortal(String app, String ip, int port) {
        try {
            Optional<MachineInfo> machineInfoOptional = appManagement.getDetailApp(app).getMachine(ip, port);
            ApolloMachineInfo apolloMachineInfo = (ApolloMachineInfo) machineInfoOptional.get();
            return computeIfAbsentPortal(apolloMachineInfo);
        } catch (Exception e) {
            RecordLog.warn(String.format("[ApolloClientManagement] can't create mapping for ApolloMachineInfo(app:%s ,ip:%s, port:%s) with portal url", app, ip, port), e);
            return null;
        }
    }

    public static String computeIfAbsentPortal(ApolloMachineInfo apolloMachineInfo) {
        return portalMapping.computeIfAbsent(apolloMachineInfo, e -> {
            StringBuilder portalKey = new StringBuilder(apolloMachineInfo.getPortalUrl());
            portalKey.append(apolloMachineInfo.getAppId());
            portalKey.append(apolloMachineInfo.getEnv());
            portalKey.append(apolloMachineInfo.getClusterName());
            portalKey.append(apolloMachineInfo.getNamespace());
            return portalKey.toString();
        });
    }

}
