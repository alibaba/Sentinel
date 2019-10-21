package org.aopalliance.intercept;

public interface ConstructorInterceptor extends Interceptor {
   Object construct(ConstructorInvocation var1) throws Throwable;
}
