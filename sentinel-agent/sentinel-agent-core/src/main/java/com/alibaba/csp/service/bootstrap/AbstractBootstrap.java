package com.alibaba.csp.service.bootstrap;

import com.alibaba.csp.service.ServiceLauncher;
import com.alibaba.csp.service.api.util.SpiBeanFactory;
import com.alibaba.csp.service.ioc.ServiceModule;
import com.alibaba.csp.util.EnvUtil;
import com.google.inject.Guice;
import com.alibaba.csp.service.exception.PanicException;

public abstract class AbstractBootstrap implements Bootstrap {
   public static String clientType = "JAVA_SDK";
   private static boolean isInitialized;

   public synchronized void launch(String type) {
      if (!isInitialized) {
         clientType = type;

         try {
            this.launch0();
         } catch (Exception var3) {
            throw new RuntimeException(var3);
         }

         isInitialized = true;
      }
   }

   private void launch0() throws Exception {
      SpiBeanFactory.INJECTOR = Guice.createInjector(new ServiceModule());
      SpiBeanFactory.setClassLoader(this.getClass().getClassLoader());
      this.checkEnvironment();
      ServiceLauncher.init();
   }

   private void checkEnvironment() throws PanicException {
      if (!EnvUtil.isJdk6OrHigher()) {
         throw new PanicException("Java version is less than 1.6, Ahas dose not support.");
      }
   }
}
