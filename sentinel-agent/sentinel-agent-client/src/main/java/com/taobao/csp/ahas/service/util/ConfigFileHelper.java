package com.taobao.csp.ahas.service.util;

import com.taobao.csp.ahas.service.exception.AhasClientException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Properties;

public class ConfigFileHelper {
   public static final String configFileName = "ahas-java-agent.config";
   public static final String VERSION_CONFIG = "version.properties";
   public static final int CONFIG_MAX_SIZE = 1048576;

   public static Properties loadConfigFile(ClassLoader classLoader) throws AhasClientException {
      String configFile = getConfigFile(classLoader);
      if (configFile == null) {
         return null;
      } else {
         File file = new File(configFile);
         if (!file.exists()) {
            return null;
         } else if (file.length() > 1048576L) {
            throw new AhasClientException("config file is incorrect.");
         } else {
            try {
               return load(file);
            } catch (Exception var4) {
               throw new AhasClientException(var4);
            }
         }
      }
   }

   private static Properties load(File file) throws Exception {
      Properties properties = new Properties();
      FileInputStream is = null;

      try {
         is = new FileInputStream(file);
         properties.load(is);
      } catch (IOException var11) {
         throw var11;
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (IOException var10) {
            }
         }

      }

      return properties;
   }

   private static String getConfigFile(ClassLoader classLoader) {
      File homeDirectory = findHomeDirectory(classLoader);
      return homeDirectory == null ? null : homeDirectory.getAbsolutePath() + "/" + "ahas-java-agent.config";
   }

   private static File findHomeDirectory(ClassLoader classLoader) {
      File homeDir = findHomeDirectoryFromProperty();
      if (homeDir != null) {
         return homeDir;
      } else {
         homeDir = findHomeDirectoryFromEnvironmentVariable();
         return homeDir != null ? homeDir : getAgentJarDirectory(classLoader);
      }
   }

   public static File getAgentJarDirectory(ClassLoader classLoader) {
      URL agentJarUrl = getAgentJarUrl(classLoader);
      if (agentJarUrl != null) {
         File file = new File(getAgentJarFileName(agentJarUrl));
         if (file.exists()) {
            return file.getParentFile();
         }
      }

      return null;
   }

   private static URL getAgentJarUrl(ClassLoader classLoader) {
      try {
         if (classLoader == null) {
            classLoader = ConfigFileHelper.class.getClassLoader();
            return classLoader.getClass().getProtectionDomain().getCodeSource().getLocation();
         } else {
            CodeSource codeSource = ConfigFileHelper.class.getProtectionDomain().getCodeSource();
            return codeSource == null ? classLoader.getClass().getProtectionDomain().getCodeSource().getLocation() : codeSource.getLocation();
         }
      } catch (Exception var2) {
         return null;
      }
   }

   private static String getAgentJarFileName(URL agentJarUrl) {
      if (agentJarUrl == null) {
         return null;
      } else {
         try {
            return URLDecoder.decode(agentJarUrl.getFile().replace("+", "%2B"), "UTF-8");
         } catch (IOException var2) {
            return null;
         }
      }
   }

   private static File findHomeDirectoryFromProperty() {
      String filePath = System.getProperty("ahas.home");
      if (filePath != null) {
         File homeDir = new File(filePath);
         if (homeDir.exists()) {
            return homeDir;
         }
      }

      return null;
   }

   private static File findHomeDirectoryFromEnvironmentVariable() {
      String filePath = System.getenv("AHAS_HOME");
      if (filePath != null) {
         File homeDir = new File(filePath);
         if (homeDir.exists()) {
            return homeDir;
         }
      }

      return null;
   }

   public static Properties loadVersionProperties(ClassLoader classLoader) {
      if (classLoader == null) {
         classLoader = EmbeddedJarUtil.class.getClassLoader();
      }

      if (classLoader == null) {
         return null;
      } else {
         InputStream jarStream = classLoader.getResourceAsStream("version.properties");
         if (jarStream == null) {
            return null;
         } else {
            Properties properties = new Properties();

            Properties var4;
            try {
               properties.load(jarStream);
               Properties var3 = properties;
               return var3;
            } catch (Exception var14) {
               var4 = properties;
            } finally {
               try {
                  jarStream.close();
               } catch (Exception var13) {
               }

            }

            return var4;
         }
      }
   }
}
