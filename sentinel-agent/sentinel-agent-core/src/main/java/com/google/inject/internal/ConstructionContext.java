package com.google.inject.internal;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class ConstructionContext<T> {
   T currentReference;
   boolean constructing;
   List<DelegatingInvocationHandler<T>> invocationHandlers;

   public T getCurrentReference() {
      return this.currentReference;
   }

   public void removeCurrentReference() {
      this.currentReference = null;
   }

   public void setCurrentReference(T currentReference) {
      this.currentReference = currentReference;
   }

   public boolean isConstructing() {
      return this.constructing;
   }

   public void startConstruction() {
      this.constructing = true;
   }

   public void finishConstruction() {
      this.constructing = false;
      this.invocationHandlers = null;
   }

   public Object createProxy(InjectorImpl.InjectorOptions injectorOptions, Class<?> expectedType) throws InternalProvisionException {
      if (injectorOptions.disableCircularProxies) {
         throw InternalProvisionException.circularDependenciesDisabled(expectedType);
      } else if (!expectedType.isInterface()) {
         throw InternalProvisionException.cannotProxyClass(expectedType);
      } else {
         if (this.invocationHandlers == null) {
            this.invocationHandlers = new ArrayList();
         }

         DelegatingInvocationHandler<T> invocationHandler = new DelegatingInvocationHandler();
         this.invocationHandlers.add(invocationHandler);
         ClassLoader classLoader = BytecodeGen.getClassLoader(expectedType);
         return expectedType.cast(Proxy.newProxyInstance(classLoader, new Class[]{expectedType, CircularDependencyProxy.class}, invocationHandler));
      }
   }

   public void setProxyDelegates(T delegate) {
      if (this.invocationHandlers != null) {
         Iterator i$ = this.invocationHandlers.iterator();

         while(i$.hasNext()) {
            DelegatingInvocationHandler<T> handler = (DelegatingInvocationHandler)i$.next();
            handler.setDelegate(delegate);
         }

         this.invocationHandlers = null;
      }

   }
}
