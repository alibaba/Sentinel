package com.taobao.csp.ahas.service.bridge.aop;

import java.lang.reflect.Method;

public interface Enhancer {
   <T> T beforeAdvice(ClassLoader var1, Object var2, Method var3, Object[] var4) throws Exception;

   <T> T afterAdvice(ClassLoader var1, Object var2, Method var3, Object[] var4, Object var5) throws Exception;

   <T> T throwAdvice(ClassLoader var1, Object var2, Method var3, Object[] var4, Throwable var5) throws Throwable;
}
