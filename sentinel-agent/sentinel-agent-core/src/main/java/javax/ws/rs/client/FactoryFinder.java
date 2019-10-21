package javax.ws.rs.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

final class FactoryFinder {
   private static final Logger LOGGER = Logger.getLogger(FactoryFinder.class.getName());

   private FactoryFinder() {
   }

   static ClassLoader getContextClassLoader() {
      return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
         ClassLoader cl = null;
         try {
            cl = Thread.currentThread().getContextClassLoader();
         } catch (SecurityException ex) {
            LOGGER.log(
                    Level.WARNING,
                    "Unable to get context classloader instance.",
                    ex);
         }
         return cl;
      });
   }

   private static Object newInstance(String className, ClassLoader classLoader) throws ClassNotFoundException {
      try {
         Class spiClass;
         if (classLoader == null) {
            spiClass = Class.forName(className);
         } else {
            try {
               spiClass = Class.forName(className, false, classLoader);
            } catch (ClassNotFoundException var4) {
               LOGGER.log(Level.FINE, "Unable to load provider class " + className + " using custom classloader " + classLoader.getClass().getName() + " trying again with current classloader.", var4);
               spiClass = Class.forName(className);
            }
         }

         return spiClass.getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException var5) {
         throw var5;
      } catch (Exception var6) {
         throw new ClassNotFoundException("Provider " + className + " could not be instantiated: " + var6, var6);
      }
   }

   static <T> Object find(String factoryId, String fallbackClassName, Class<T> service) throws ClassNotFoundException {
      ClassLoader classLoader = getContextClassLoader();

      Iterator iterator;
      try {
         iterator = ServiceLoader.load(service, getContextClassLoader()).iterator();
         if (iterator.hasNext()) {
            return iterator.next();
         }
      } catch (ServiceConfigurationError | Exception var26) {
         LOGGER.log(Level.FINER, "Failed to load service " + factoryId + ".", var26);
      }

      try {
         iterator = ServiceLoader.load(service, FactoryFinder.class.getClassLoader()).iterator();
         if (iterator.hasNext()) {
            return iterator.next();
         }
      } catch (ServiceConfigurationError | Exception var25) {
         LOGGER.log(Level.FINER, "Failed to load service " + factoryId + ".", var25);
      }

      FileInputStream inputStream = null;
      String configFile = null;

      String systemProp;
      try {
         systemProp = System.getProperty("java.home");
         configFile = systemProp + File.separator + "lib" + File.separator + "jaxrs.properties";
         File f = new File(configFile);
         if (f.exists()) {
            Properties props = new Properties();
            inputStream = new FileInputStream(f);
            props.load(inputStream);
            String factoryClassName = props.getProperty(factoryId);
            Object var10 = newInstance(factoryClassName, classLoader);
            return var10;
         }
      } catch (Exception var27) {
         LOGGER.log(Level.FINER, "Failed to load service " + factoryId + " from $java.home/lib/jaxrs.properties", var27);
      } finally {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (IOException var23) {
               LOGGER.log(Level.FINER, String.format("Error closing %s file.", configFile), var23);
            }
         }

      }

      try {
         systemProp = System.getProperty(factoryId);
         if (systemProp != null) {
            return newInstance(systemProp, classLoader);
         }
      } catch (SecurityException var24) {
         LOGGER.log(Level.FINER, "Failed to load service " + factoryId + " from a system property", var24);
      }

      if (fallbackClassName == null) {
         throw new ClassNotFoundException("Provider for " + factoryId + " cannot be found", (Throwable)null);
      } else {
         return newInstance(fallbackClassName, classLoader);
      }
   }
}
