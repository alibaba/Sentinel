package com.alibaba.csp.service;

//import com.taobao.csp.ahas.middleware.logger.Logger;
import com.alibaba.csp.service.api.util.SpiBeanFactory;
import com.alibaba.csp.service.exception.PanicException;
//import com.taobao.csp.ahas.service.bridge.logger.LoggerInit;
import com.alibaba.csp.service.bridge.service.Service;
import com.alibaba.csp.service.bridge.service.ServiceProvider;
import com.alibaba.csp.service.exception.AhasClientException;

import java.util.Iterator;
import java.util.List;

public class ServiceLauncher {
 //  private static final Logger LOGGER;

   public static synchronized void init() throws PanicException {
      initService();
   }

   private static void initService() throws PanicException {
      List<Service> services = SpiBeanFactory.getInstances(Service.class);
      Iterator var1 = services.iterator();

      while(var1.hasNext()) {
         Service service = (Service)var1.next();
      //   LOGGER.info("Starting service {}", service.getName());

         try {
            service.start();
      //      LOGGER.info("Start service {} finished.", service.getName());
         } catch (AhasClientException var4) {
       //     LOGGER.error("Start important service {} exception, ahas exit.", service.getName(), var4);
            clearService();
            throw new PanicException(var4);
         } catch (PanicException var5) {
      //      LOGGER.error("Start important service {} exception, ahas exit.", service.getName(), var5);
            clearService();
            throw var5;
         } catch (Throwable var6) {
       //     LOGGER.error("Start service {} occurs exception.", service.getName(), var6);
         }
      }

      ServiceProvider.cache(services);
   }

   private static void clearService() {
      List<Service> services = SpiBeanFactory.getInstances(Service.class);
      Iterator var1 = services.iterator();

      while(var1.hasNext()) {
         Service service = (Service)var1.next();
      //   LOGGER.info("Destroy service {}", service.getName());

         try {
            service.destroy();
         } catch (Exception var4) {
     //       LOGGER.warn("destroy service {} occurs exception.", service.getName(), new Object[]{var4});
         }
      }

      ServiceProvider.clear();
   }

   static {
   //   LOGGER = LoggerInit.LOGGER;
   }
}
