package org.aopalliance.intercept;

public interface MethodInterceptor extends Interceptor {
   Object invoke(MethodInvocation var1) throws Throwable;
}
