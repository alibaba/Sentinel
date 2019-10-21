package com.taobao.csp.ahas.service.api.config;

public interface ConfigService {
   String NAME = "config";

   String getProperty(String var1);

   String getProperty(String var1, String var2);

   String getLicense();

   boolean isPrivate();

   String getVersion();

   String getBuildNumber();

   void setIsPrivate(boolean var1);
}
