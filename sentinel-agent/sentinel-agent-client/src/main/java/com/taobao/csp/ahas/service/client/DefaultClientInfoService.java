package com.taobao.csp.ahas.service.client;

import com.taobao.csp.ahas.service.api.client.ClientInfoService;
import com.taobao.csp.ahas.service.api.config.ConfigService;
import com.taobao.csp.ahas.service.config.DefaultConfigService;
import com.taobao.csp.ahas.service.exception.AhasClientException;
import com.taobao.csp.ahas.service.init.AhasInitFunc;
import com.taobao.csp.ahas.service.util.AppNameUtil;
import com.taobao.csp.ahas.service.util.VpcEcsUtil;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

public class DefaultClientInfoService implements ClientInfoService, AhasInitFunc {
   public static final String PID_DELIMITER = "@";
   public static final String DOCKER_ENV_FILE = "/.dockerenv";
   public static final int DEFAULT_GATEWAY_PORT = 9527;
   public static final String PUBLIC_REGION_ID = "cn-public";
   public static final int HOST_TYPE = 0;
   public static final int CONTAINER_TYPE = 1;
   public static final String JAVA_SDK = "JAVA_SDK";
   public static final String JAVA_AGENT = "JAVA_AGENT";
   private ConfigService configService;
   private String pid;
   private String appName;
   private String appType;
   private String ahasAppName;
   private List<String> ips;
   private String vpcId;
   private String instanceId;
   private List<String> jvmArgs;
   private String gatewayHost;
   private int gatewayPort;
   private String acmHost;
   private String cid;
   private String userId;
   private String namespace;
   private String hostname;
   private String hostIp;
   private String privateIp;
   private int deviceType = -1;
   private String tid;
   private String env;
   private String version;
   private String buildNumber;
   private String license;
   private boolean isPrivate;
   private String type;

   public static boolean isBlank(String str) {
      int strLen;
      if (str != null && (strLen = str.length()) != 0) {
         for(int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public void init(String type, ClassLoader classLoader) throws AhasClientException {
      this.configService = (new DefaultConfigService()).init(type, classLoader);
      this.isPrivate = this.configService.isPrivate();
      this.checkIdentity(this.isPrivate);
      this.initVpcId(this.isPrivate);
      this.initEndpoint(this.isPrivate);
      this.initPidAndHostname();
      this.type = type;
   }

   private void checkIdentity(boolean isPrivate) throws AhasClientException {
      String license;
      if (isPrivate) {
         license = this.getUserId();
         if (license == null || license.length() == 0) {
            throw new AhasClientException("Cannot get uid from ecs.");
         }
      } else {
         license = this.getLicense();
         if (license == null || license.length() == 0) {
            String userId = this.getUserId();
            if (userId == null || userId.length() <= 0) {
               throw new AhasClientException("Cannot find license.");
            }

            this.isPrivate = true;
            this.configService.setIsPrivate(true);
         }
      }

   }

   private void initVpcId(boolean isPrivate) throws AhasClientException {
      if (isPrivate) {
         this.vpcId = VpcEcsUtil.getVpcId();
      } else {
         this.vpcId = this.getLicense();
         if (this.vpcId == null || this.vpcId.length() == 0) {
            throw new AhasClientException("cannot get vpc id");
         }
      }
   }

   private void initEndpoint(boolean isPrivate) throws AhasClientException {
      this.env = System.getProperty("ahas.env", "prod");
      String regionId;
      if (isPrivate) {
         regionId = VpcEcsUtil.getRegionId();
      } else {
         regionId = "cn-public";
      }

      String endpoint = (String)UrlConstants.GATEWAY_MAP.get(this.env + "-" + regionId);
      this.acmHost = (String)UrlConstants.ACM_MAP.get(regionId);
      if (!isBlank(endpoint) && !isBlank(this.acmHost)) {
         String[] split = endpoint.split(":");
         if (split.length != 2) {
            throw new AhasClientException("Ahas agent endpoint length error");
         } else {
            this.gatewayHost = split[0];

            try {
               this.gatewayPort = Integer.valueOf(split[1]);
            } catch (NumberFormatException var6) {
               this.gatewayPort = 9527;
            }

         }
      } else {
         throw new AhasClientException("Cannot get ahas host or acm endpoint");
      }
   }

   private void initPidAndHostname() {
      String processInfo = ManagementFactory.getRuntimeMXBean().getName();
      if (!isBlank(processInfo)) {
         if (processInfo.indexOf("@") != -1) {
            String[] split = processInfo.split("@");
            String processId = split[0];
            if (!isBlank(processId)) {
               this.hostname = split[1];

               try {
                  Integer.valueOf(processId);
                  this.pid = processId;
               } catch (NumberFormatException var5) {
               }

            }
         }
      }
   }

   private List<String> getAllIps() {
      ArrayList ips = new ArrayList();

      try {
         Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

         while(interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface)interfaces.nextElement();
            Enumeration inetAddresses = networkInterface.getInetAddresses();

            while(inetAddresses.hasMoreElements()) {
               InetAddress inetAddress = (InetAddress)inetAddresses.nextElement();
               if (!inetAddress.isLoopbackAddress() && !(inetAddress instanceof Inet6Address)) {
                  ips.add(inetAddress.getHostAddress());
               }
            }
         }
      } catch (Exception var6) {
      }

      return ips;
   }

   public String getVpcId() {
      return this.vpcId;
   }

   public String getPid() {
      if (this.pid == null) {
         this.initPidAndHostname();
      }

      return this.pid;
   }

   public String getAppName() {
      if (this.appName != null) {
         return this.appName;
      } else {
         this.appName = AppNameUtil.getAppName();
         return this.appName;
      }
   }

   public String getAppType() {
      if (this.appType != null) {
         return this.appType;
      } else {
         this.appType = String.valueOf(AppTypeParser.parseAppType());
         return this.appType;
      }
   }

   public String getAhasAppName() {
      if (this.ahasAppName != null) {
         return this.ahasAppName;
      } else {
         this.ahasAppName = AppNameUtil.getAhasAppName();
         return this.ahasAppName;
      }
   }

   public String getHostIp() {
      if (this.hostIp != null) {
         return this.hostIp;
      } else {
         if (this.isPrivate()) {
            this.hostIp = VpcEcsUtil.getPrivateIpv4();
         } else {
            this.hostIp = this.getPrivateIp();
         }

         return this.hostIp;
      }
   }

   public String getPrivateIp() {
      if (this.privateIp != null) {
         return this.privateIp;
      } else if (this.isPrivate() && this.getDeviceType() == 0) {
         this.privateIp = this.getHostIp();
         return this.privateIp;
      } else {
         List<String> ips = this.getIps();
         if (ips != null && ips.size() > 0) {
            this.privateIp = (String)ips.get(0);
            return this.privateIp;
         } else {
            return null;
         }
      }
   }

   public List<String> getIps() {
      if (this.ips != null && !this.ips.isEmpty()) {
         return this.ips;
      } else {
         this.ips = this.getAllIps();
         return this.ips;
      }
   }

   public Map<String, String> getSystemProperties() {
      Map<String, String> map = new HashMap(16);
      Properties properties = System.getProperties();
      Iterator var3 = properties.stringPropertyNames().iterator();

      while(var3.hasNext()) {
         String name = (String)var3.next();
         map.put(name, properties.getProperty(name));
      }

      return map;
   }

   public List<String> getJvmArgs() {
      if (this.jvmArgs != null) {
         return this.jvmArgs;
      } else {
         RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
         this.jvmArgs = runtimeMXBean.getInputArguments();
         return this.jvmArgs;
      }
   }

   public String getInstanceId() {
      if (this.instanceId != null) {
         return this.instanceId;
      } else {
         if (this.isPrivate()) {
            this.instanceId = VpcEcsUtil.getInstanceId();
         } else {
            this.instanceId = this.getHostname();
         }

         return this.instanceId;
      }
   }

   public String getAid() {
      return this.cid;
   }

   public String setAid(String aid) {
      return this.cid = aid;
   }

   public String getUserId() {
      if (this.userId != null) {
         return this.userId;
      } else {
         this.userId = VpcEcsUtil.getUid();
         return this.userId;
      }
   }

   public String setUserId(String userId) {
      this.userId = userId;
      return userId;
   }

   public String getNamespace() {
      if (this.namespace != null) {
         return this.namespace;
      } else {
         this.namespace = System.getProperty("ahas.namespace", "default");
         return this.namespace;
      }
   }

   public String getType() {
      return this.type;
   }

   public String getHostname() {
      if (this.hostname == null) {
         this.initPidAndHostname();
      }

      return this.hostname;
   }

   public int getDeviceType() {
      if (this.deviceType != -1) {
         return this.deviceType;
      } else {
         File file = new File("/.dockerenv");
         if (file.exists()) {
            this.deviceType = 1;
         } else {
            this.deviceType = 0;
         }

         return this.deviceType;
      }
   }

   public String getTid() {
      return this.tid;
   }

   public void setTid(String tid) {
      this.tid = tid;
   }

   public String getVersion() {
      if (this.version == null) {
         this.version = this.configService.getVersion();
      }

      return this.version;
   }

   public String getBuildNumber() {
      if (this.buildNumber == null) {
         this.buildNumber = this.configService.getBuildNumber();
      }

      return this.buildNumber;
   }

   public String getLicense() {
      if (this.license == null) {
         this.license = this.configService.getLicense();
      }

      return this.license;
   }

   public boolean isPrivate() {
      return this.isPrivate;
   }

   public String getAcmEndpoint() {
      return this.acmHost;
   }

   public String getGatewayHost() {
      return this.gatewayHost;
   }

   public int getGatewayPort() {
      return this.gatewayPort;
   }
}
