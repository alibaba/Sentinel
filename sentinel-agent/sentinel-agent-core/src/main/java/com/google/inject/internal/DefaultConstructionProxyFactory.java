package com.google.inject.internal;

import com.google.inject.spi.InjectionPoint;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

final class DefaultConstructionProxyFactory<T> implements ConstructionProxyFactory<T> {
   private final InjectionPoint injectionPoint;

   DefaultConstructionProxyFactory(InjectionPoint injectionPoint) {
      this.injectionPoint = injectionPoint;
   }

   public ConstructionProxy<T> create() {
      Constructor<T> constructor = (Constructor)this.injectionPoint.getMember();
      return new ReflectiveProxy(this.injectionPoint, constructor);
   }

   private static final class ReflectiveProxy<T> implements ConstructionProxy<T> {
      final Constructor<T> constructor;
      final InjectionPoint injectionPoint;

      ReflectiveProxy(InjectionPoint injectionPoint, Constructor<T> constructor) {
         if (!Modifier.isPublic(constructor.getDeclaringClass().getModifiers()) || !Modifier.isPublic(constructor.getModifiers())) {
            constructor.setAccessible(true);
         }

         this.injectionPoint = injectionPoint;
         this.constructor = constructor;
      }

      public T newInstance(Object... arguments) throws InvocationTargetException {
         try {
            return this.constructor.newInstance(arguments);
         } catch (InstantiationException var3) {
            throw new AssertionError(var3);
         } catch (IllegalAccessException var4) {
            throw new AssertionError(var4);
         }
      }

      public InjectionPoint getInjectionPoint() {
         return this.injectionPoint;
      }

      public Constructor<T> getConstructor() {
         return this.constructor;
      }
   }
}
