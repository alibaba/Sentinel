package com.google.inject.internal;

import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import java.lang.reflect.Field;

final class SingleFieldInjector implements SingleMemberInjector {
   final Field field;
   final InjectionPoint injectionPoint;
   final Dependency<?> dependency;
   final BindingImpl<?> binding;

   public SingleFieldInjector(InjectorImpl injector, InjectionPoint injectionPoint, Errors errors) throws ErrorsException {
      this.injectionPoint = injectionPoint;
      this.field = (Field)injectionPoint.getMember();
      this.dependency = (Dependency)injectionPoint.getDependencies().get(0);
      this.field.setAccessible(true);
      this.binding = injector.getBindingOrThrow(this.dependency.getKey(), errors, InjectorImpl.JitLimitation.NO_JIT);
   }

   public InjectionPoint getInjectionPoint() {
      return this.injectionPoint;
   }

   public void inject(InternalContext context, Object o) throws InternalProvisionException {
      Dependency previous = context.pushDependency(this.dependency, this.binding.getSource());

      try {
         Object value = this.binding.getInternalFactory().get(context, this.dependency, false);
         this.field.set(o, value);
      } catch (InternalProvisionException var9) {
         throw var9.addSource(this.dependency);
      } catch (IllegalAccessException var10) {
         throw new AssertionError(var10);
      } finally {
         context.popStateAndSetDependency(previous);
      }

   }
}
