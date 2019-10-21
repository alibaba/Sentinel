package com.taobao.csp.ahas.module;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.ReturnEvent;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import java.lang.reflect.Method;

public class AfterEventListener implements EventListener {
   private AspectPlugin plugin;
   private ClassLoader classLoader;
   private Object target;

   public AfterEventListener(AspectPlugin plugin) {
      this.plugin = plugin;
   }

   public void onEvent(Event event) throws Throwable {
      if (event instanceof BeforeEvent) {
         BeforeEvent beforeEvent = (BeforeEvent)event;
         this.classLoader = beforeEvent.javaClassLoader;
         this.target = beforeEvent.target;
      }

      if (event instanceof ReturnEvent) {
         ReturnEvent returnEvent = (ReturnEvent)event;
         this.plugin.getEnhancer().afterAdvice(this.classLoader, this.target, (Method)null, (Object[])null, returnEvent.object);
      }
   }
}
