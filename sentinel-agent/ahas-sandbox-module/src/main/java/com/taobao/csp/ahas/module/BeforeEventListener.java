package com.taobao.csp.ahas.module;

import com.alibaba.jvm.sandbox.api.ProcessControlException;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import com.taobao.csp.ahas.module.util.InterruptProcessException;
import com.taobao.csp.ahas.module.util.InterruptProcessException.State;
import com.taobao.csp.ahas.module.util.ReflectUtil;
import java.lang.reflect.Method;

public class BeforeEventListener implements EventListener {
   private AspectPlugin plugin;

   public BeforeEventListener(AspectPlugin plugin) {
      this.plugin = plugin;
   }

   public void onEvent(Event event) throws Throwable {
      if (event instanceof BeforeEvent) {
         this.handleEvent((BeforeEvent)event);
      }

   }

   private void handleEvent(BeforeEvent event) throws Throwable {
      Object target = event.target;
      Class<?> clazz = target.getClass();
      Method method = null;

      try {
         method = ReflectUtil.getMethod(clazz, event.javaMethodDesc, event.javaMethodName);
      } catch (NoSuchMethodException var7) {
         return;
      }

      try {
         this.plugin.getEnhancer().beforeAdvice(event.javaClassLoader, target, method, event.argumentArray);
      } catch (Exception var8) {
         if (!(var8 instanceof InterruptProcessException)) {
            throw var8;
         }

         InterruptProcessException exception = (InterruptProcessException)var8;
         if (exception.getState() == State.RETURN_IMMEDIATELY) {
            ProcessControlException.throwReturnImmediately(exception.getResponse());
         } else if (exception.getState() == State.THROWS_IMMEDIATELY) {
            ProcessControlException.throwThrowsImmediately((Throwable)exception.getResponse());
         }
      }

   }
}
