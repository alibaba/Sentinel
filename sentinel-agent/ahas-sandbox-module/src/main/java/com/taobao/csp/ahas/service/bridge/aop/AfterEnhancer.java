package com.taobao.csp.ahas.service.bridge.aop;

import java.lang.reflect.Method;

public abstract class AfterEnhancer implements Enhancer {
   public <T> T beforeAdvice(ClassLoader classLoader, Object target, Method method, Object[] methodArguments) throws Exception {
      return null;
   }

   public <T> T throwAdvice(ClassLoader classLoader, Object target, Method method, Object[] methodArguments, Throwable t) throws Throwable {
      return null;
   }
}
