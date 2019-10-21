package com.taobao.csp.ahas.service.config;

import com.taobao.csp.ahas.service.api.config.ConfigService;
import com.taobao.csp.ahas.service.exception.AhasClientException;
import com.taobao.csp.ahas.service.util.ConfigFileHelper;

import java.util.Properties;

public class DefaultConfigService implements ConfigService {
   public static final String UNKNOWN = "unknown";
   private Properties configs = new Properties();

   public ConfigService init(String param, ClassLoader classLoader) throws AhasClientException {
      Properties properties = ConfigFileHelper.loadVersionProperties(classLoader);
      if (properties != null) {
         this.configs.putAll(properties);
      }

      properties = ConfigFileHelper.loadConfigFile(classLoader);
      if (properties != null) {
         this.configs.putAll(properties);
      }

      return this;
   }

   public String getLicense() {
      return this.getProperty("ahas.license");
   }

   public boolean isPrivate() {
      String scope = this.getProperty("ahas.scope");
      return "private".equalsIgnoreCase(scope);
   }

   public String getVersion() {
      return this.getProperty("ahas.version", "unknown");
   }

   public String getBuildNumber() {
      return this.getProperty("ahas.build.number", "unknown");
   }

   public void setIsPrivate(boolean isPrivate) {
      if (isPrivate) {
         System.setProperty("ahas.scope", "private");
      } else {
         System.setProperty("ahas.scope", "public");
      }

   }

   public String getProperty(String key) {
      return this.getProperty(key, (String)null);
   }

   public String getProperty(String key, String defaultValue) {
      String value = this.getPropertyFromSystemProperties(key);
      if (value != null) {
         return value;
      } else {
         value = this.getPropertyFromSystemEnvironment(key);
         return value != null ? value : this.configs.getProperty(key, defaultValue);
      }
   }

   protected String getPropertyFromSystemProperties(String key) {
      return System.getProperty(key);
   }

   protected String getPropertyFromSystemEnvironment(String key) {
      return System.getenv(key);
   }
}
