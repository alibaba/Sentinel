package com.alibaba.csp;

import com.alibaba.jvm.sandbox.api.resource.LoadedClassDataSource;
import com.alibaba.jvm.sandbox.core.CoreConfigure;
import com.alibaba.jvm.sandbox.core.manager.*;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultCoreModuleManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultLoadedClassDataSource;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultModuleLifeCycleEventBus;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultModuleResourceManager;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultProviderManager;
import com.alibaba.jvm.sandbox.core.util.Initializer;
import com.alibaba.csp.util.StringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

public class AgentModuleManager {
   private static AgentModuleManager manager;
   private final Initializer initializer = new Initializer(true);
   private ModuleResourceManager moduleResourceManager;
   private CoreModuleManager coreModuleManager;

   public static AgentModuleManager getInstance() {
      if (null == manager) {
         Class var0 = AgentModuleManager.class;
         synchronized(AgentModuleManager.class) {
            if (null == manager) {
               manager = new AgentModuleManager();
            }
         }
      }

      return manager;
   }

   public void initLogback() {
      String logBase = System.getProperty("ahas.log.path", "/var/lib/aliyunahas/agent/logs");
      File logBasePath;
      if (logBase.equals("/var/lib/aliyunahas/agent/logs")) {
         logBasePath = new File("/var/lib");
         if (!logBasePath.exists() || !logBasePath.canWrite()) {
            logBase = System.getProperty("user.home") + "/logs/aliyunahas/agent/logs";
         }
      }

      logBase = logBase + "." + this.getIdentify();
      logBasePath = new File(logBase);
      if (!logBasePath.exists()) {
         logBasePath.mkdirs();
      }

      System.setProperty("ahas.log.path", logBase);
      System.setProperty("csp.sentinel.log.dir", logBase);
      System.setProperty("JM.LOG.PATH", logBase);
      System.setProperty("JM.SNAPSHOT.PATH", logBase);
      System.setProperty("csp.sentinel.log.dir", logBase + "/sentinel");
      System.setProperty("ahas.cert", logBase + "/" + ".ahas.cert");
      System.setProperty("ahas.log.level", System.getProperty("ahas.log.level", "info"));
      System.setProperty("JM.LOG.RETAIN.COUNT", System.getProperty("ahas.log.count", "2"));
      System.setProperty("JM.LOG.FILE.SIZE", System.getProperty("ahas.log.size", "40MB"));
      this.cleanPreLog(logBasePath);
   }

   private void cleanPreLog(File logBasePath) {
      try {
         File[] logPath = logBasePath.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
               return !name.contains("diamond");
            }
         });
         File[] var3 = logPath;
         int var4 = logPath.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            this.deleteDir(file);
         }
      } catch (Exception var7) {
      }

   }

   private void deleteDir(File file) {
      if (file.isDirectory()) {
         File[] files = file.listFiles();
         File[] var3 = files;
         int var4 = files.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File f = var3[var5];
            this.deleteDir(f);
         }
      }

      file.delete();
   }

   public boolean isBind() {
      return this.initializer.isInitialized();
   }

   public void initManager(CoreConfigure cfg, Instrumentation inst) {
      try {
//         ModuleLifeCycleEventBus moduleLifeCycleEventBus = new DefaultModuleLifeCycleEventBus();
//         LoadedClassDataSource classDataSource = new DefaultLoadedClassDataSource(inst);
//         ClassLoader sandboxClassLoader = this.getClass().getClassLoader();
//         this.moduleResourceManager = new DefaultModuleResourceManager();
//         moduleLifeCycleEventBus.append(this.moduleResourceManager);
//         ProviderManager providerManager = new DefaultProviderManager(cfg, sandboxClassLoader);
//         this.coreModuleManager = new DefaultCoreModuleManager(inst, classDataSource, cfg, sandboxClassLoader, moduleLifeCycleEventBus, providerManager);

         ModuleLifeCycleEventBus moduleLifeCycleEventBus = new DefaultModuleLifeCycleEventBus();
      //   LoadedClassDataSource classDataSource = new DefaultLoadedClassDataSource(inst,true);
         CoreLoadedClassDataSource classDataSource = new DefaultLoadedClassDataSource(inst,true);


         ClassLoader sandboxClassLoader = this.getClass().getClassLoader();
         this.moduleResourceManager = new DefaultModuleResourceManager();
         moduleLifeCycleEventBus.append(this.moduleResourceManager);
         ProviderManager providerManager = new DefaultProviderManager(cfg);
   //      this.coreModuleManager = new DefaultCoreModuleManager(inst, classDataSource, cfg, sandboxClassLoader, moduleLifeCycleEventBus, providerManager);

         this.coreModuleManager = new DefaultCoreModuleManager(cfg, inst, classDataSource, providerManager);

      } catch (Throwable var7) {
         throw new RuntimeException(var7);
      }
   }

   private String getIdentify() {
      String processInfo = ManagementFactory.getRuntimeMXBean().getName();
      if (StringUtil.isBlank(processInfo)) {
         return "";
      } else if (processInfo.indexOf("@") == -1) {
         return "";
      } else {
         String[] split = processInfo.split("@");
         String processId = split[0];
         if (StringUtil.isBlank(processId)) {
            return "";
         } else {
            String hostname = "";
            if (split.length > 1) {
               hostname = split[1];
            }

            return processId + "." + hostname;
         }
      }
   }
}
