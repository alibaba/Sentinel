package com.taobao.csp.ahas.service.api.client;

import java.util.List;
import java.util.Map;

public interface ClientInfoService {
   String NAME = "clientinfo";

   String getVpcId();

   String getPid();

   String getAppName();

   String getAppType();

   String getAhasAppName();

   String getHostIp();

   String getPrivateIp();

   List<String> getIps();

   Map<String, String> getSystemProperties();

   List<String> getJvmArgs();

   String getInstanceId();

   String getAid();

   String setAid(String var1);

   String getAcmEndpoint();

   String getGatewayHost();

   int getGatewayPort();

   String getUserId();

   String setUserId(String var1);

   String getNamespace();

   String getType();

   String getHostname();

   int getDeviceType();

   String getTid();

   void setTid(String var1);

   String getVersion();

   String getBuildNumber();

   String getLicense();

   boolean isPrivate();
}
