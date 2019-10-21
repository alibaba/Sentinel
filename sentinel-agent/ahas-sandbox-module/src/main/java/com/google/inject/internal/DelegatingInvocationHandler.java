package com.google.inject.internal;

import com.google.common.base.Preconditions;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DelegatingInvocationHandler<T> implements InvocationHandler {
   private volatile boolean initialized;
   private T delegate;

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      try {
         Preconditions.checkState(this.initialized, "This is a proxy used to support circular references. The object we're proxying is not constructed yet. Please wait until after injection has completed to use this object.");
         Preconditions.checkNotNull(this.delegate, "This is a proxy used to support circular references. The object we're  proxying is initialized to null. No methods can be called.");
         return method.invoke(this.delegate, args);
      } catch (IllegalAccessException var5) {
         throw new RuntimeException(var5);
      } catch (IllegalArgumentException var6) {
         throw new RuntimeException(var6);
      } catch (InvocationTargetException var7) {
         throw var7.getTargetException();
      }
   }

   void setDelegate(T delegate) {
      this.delegate = delegate;
      this.initialized = true;
   }
}
