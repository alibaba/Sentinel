package com.taobao.csp.ahas.service.bridge.aop;

import java.lang.reflect.Method;

public abstract class BeforeEnhancer implements com.taobao.csp.ahas.service.bridge.aop.Enhancer {
   public <T> T afterAdvice(ClassLoader classLoader, Object target, Method method, Object[] methodArguments, Object returnValue) throws Exception {
      return null;
   }

   public <T> T throwAdvice(ClassLoader classLoader, Object target, Method method, Object[] methodArguments, Throwable t) throws Throwable {
      return null;
   }
}
