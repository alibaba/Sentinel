package com.google.inject.internal;

import com.google.inject.spi.InjectionPoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class SingleMethodInjector implements SingleMemberInjector {
   private final InjectorImpl.MethodInvoker methodInvoker;
   private final SingleParameterInjector<?>[] parameterInjectors;
   private final InjectionPoint injectionPoint;

   SingleMethodInjector(InjectorImpl injector, InjectionPoint injectionPoint, Errors errors) throws ErrorsException {
      this.injectionPoint = injectionPoint;
      Method method = (Method)injectionPoint.getMember();
      this.methodInvoker = this.createMethodInvoker(method);
      this.parameterInjectors = injector.getParametersInjectors(injectionPoint.getDependencies(), errors);
   }

   private InjectorImpl.MethodInvoker createMethodInvoker(final Method method) {
      int modifiers = method.getModifiers();
      if (!Modifier.isPublic(modifiers) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
         method.setAccessible(true);
      }

      return new InjectorImpl.MethodInvoker() {
         public Object invoke(Object target, Object... parameters) throws IllegalAccessException, InvocationTargetException {
            return method.invoke(target, parameters);
         }
      };
   }

   public InjectionPoint getInjectionPoint() {
      return this.injectionPoint;
   }

   public void inject(InternalContext context, Object o) throws InternalProvisionException {
      Object[] parameters = SingleParameterInjector.getAll(context, this.parameterInjectors);

      try {
         this.methodInvoker.invoke(o, parameters);
      } catch (IllegalAccessException var6) {
         throw new AssertionError(var6);
      } catch (InvocationTargetException var7) {
         Throwable cause = var7.getCause() != null ? var7.getCause() : var7;
         throw InternalProvisionException.errorInjectingMethod((Throwable)cause).addSource(this.injectionPoint);
      }
   }
}
