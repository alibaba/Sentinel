package com.taobao.csp.ahas.module;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleLifecycle;
import com.alibaba.jvm.sandbox.api.event.Event.Type;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.taobao.middleware.logger.Logger;
import com.taobao.csp.ahas.module.api.PluginService;
import com.taobao.csp.ahas.module.util.SpiBeanFactory;
import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import com.taobao.csp.ahas.service.bridge.aop.Enhancer;
import com.taobao.csp.ahas.service.bridge.aop.PointCut;
import com.taobao.csp.ahas.module.util.LoggerInit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;

@Information(
   id = "ahas",
   version = "1.0",
   author = "changjun.xcj",
   isActiveOnLoad = true
)
public class AhasSandboxModule implements Module, ModuleLifecycle {
   private static final Logger LOGGER;
   @Resource
   private ModuleEventWatcher moduleEventWatcher;
   private List<Integer> watcherIds = new ArrayList();

   public void onLoad() throws Throwable {
      LOGGER.info("Load ahas");
   }

   public void loadCompleted() {
      LOGGER.info("Load ahas completed");
   }

   public void onUnload() throws Throwable {
      LOGGER.info("Unload ahas");
   }

   public void onActive() throws Throwable {
      LOGGER.info("Active ahas");
      this.loadAspectPlugins();
   }

   private void loadAspectPlugins() {
      List<PluginService> pluginServices = SpiBeanFactory.getInstances(PluginService.class);
      Iterator var2 = pluginServices.iterator();

      while(true) {
         PluginService pluginService;
         List aspectPlugins;
         do {
            if (!var2.hasNext()) {
               return;
            }

            pluginService = (PluginService)var2.next();
            aspectPlugins = pluginService.getAspectPlugins();
         } while(aspectPlugins == null);

         try {
            Iterator var5 = aspectPlugins.iterator();

            while(var5.hasNext()) {
               AspectPlugin aspectPlugin = (AspectPlugin)var5.next();

               try {
                  PointCut pointCut = aspectPlugin.getPointCut();
                  Enhancer enhancer = aspectPlugin.getEnhancer();
                  int beforeId = this.moduleEventWatcher.watch(SandboxEnhancerFactory.createFilter(enhancer.getClass().getSimpleName(), pointCut,enhancer.getClass().getClassLoader()), SandboxEnhancerFactory.createBeforeEventListener(aspectPlugin), new Type[]{Type.BEFORE});
                  this.watcherIds.add(beforeId);
                  int afterId = this.moduleEventWatcher.watch(SandboxEnhancerFactory.createFilter(enhancer.getClass().getSimpleName(), pointCut,enhancer.getClass().getClassLoader()), SandboxEnhancerFactory.createAfterEventListener(aspectPlugin), new Type[]{Type.BEFORE, Type.RETURN});
                  this.watcherIds.add(afterId);
                  int throwsId = this.moduleEventWatcher.watch(SandboxEnhancerFactory.createFilter(enhancer.getClass().getSimpleName(), pointCut,enhancer.getClass().getClassLoader()), SandboxEnhancerFactory.createThrowsEventListener(aspectPlugin), new Type[]{Type.BEFORE, Type.THROWS});
                  this.watcherIds.add(throwsId);
                  LOGGER.info("Watch {} enhancer", enhancer.getClass().getSimpleName());
               } catch (Throwable var12) {
                  LOGGER.warn("Load {} occurs exception.", aspectPlugin.getClass().getName());
               }
            }
         } catch (Throwable var13) {
            LOGGER.warn("Load {} plugin service exception. {}", pluginService.getClass().getName(), new Object[]{var13.getMessage()});
         }
      }
   }

   public void onFrozen() throws Throwable {
      LOGGER.info("Frozen ahas");
   }

   static {
      LOGGER = LoggerInit.LOGGER;
   }
}
