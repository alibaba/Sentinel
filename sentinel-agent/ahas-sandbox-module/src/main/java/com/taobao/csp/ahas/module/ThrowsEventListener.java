package com.taobao.csp.ahas.module;

import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.event.ThrowsEvent;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import java.lang.reflect.Method;

public class ThrowsEventListener implements EventListener {
   private AspectPlugin plugin;
   private ClassLoader classLoader;
   private Object target;
   private Object[] arguments;

   public ThrowsEventListener(AspectPlugin plugin) {
      this.plugin = plugin;
   }

   public void onEvent(Event event) throws Throwable {
      if (event instanceof BeforeEvent) {
         BeforeEvent beforeEvent = (BeforeEvent)event;
         this.classLoader = beforeEvent.javaClassLoader;
         this.target = beforeEvent.target;
         this.arguments = beforeEvent.argumentArray;
      }

      if (event instanceof ThrowsEvent) {
         ThrowsEvent throwsEvent = (ThrowsEvent)event;
         this.plugin.getEnhancer().throwAdvice(this.classLoader, this.target, (Method)null, this.arguments, throwsEvent.throwable);
      }
   }
}
