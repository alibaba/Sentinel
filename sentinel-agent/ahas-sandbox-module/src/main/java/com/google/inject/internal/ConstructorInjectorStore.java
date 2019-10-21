package com.google.inject.internal;

import com.google.inject.spi.InjectionPoint;

final class ConstructorInjectorStore {
   private final InjectorImpl injector;
   private final FailableCache<InjectionPoint, ConstructorInjector<?>> cache = new FailableCache<InjectionPoint, ConstructorInjector<?>>() {
      protected ConstructorInjector<?> create(InjectionPoint constructorInjector, Errors errors) throws ErrorsException {
         return ConstructorInjectorStore.this.createConstructor(constructorInjector, errors);
      }
   };

   ConstructorInjectorStore(InjectorImpl injector) {
      this.injector = injector;
   }

   public ConstructorInjector<?> get(InjectionPoint constructorInjector, Errors errors) throws ErrorsException {
      return (ConstructorInjector)this.cache.get(constructorInjector, errors);
   }

   boolean remove(InjectionPoint ip) {
      return this.cache.remove(ip);
   }

   private <T> ConstructorInjector<T> createConstructor(InjectionPoint injectionPoint, Errors errors) throws ErrorsException {
      int numErrorsBefore = errors.size();
      SingleParameterInjector<?>[] constructorParameterInjectors = this.injector.getParametersInjectors(injectionPoint.getDependencies(), errors);

      MembersInjectorImpl<T> membersInjector = (MembersInjectorImpl<T>)
              injector.membersInjectorStore.get(injectionPoint.getDeclaringType(), errors);
      ConstructionProxyFactory<T> factory = new DefaultConstructionProxyFactory(injectionPoint);
      errors.throwIfNewErrors(numErrorsBefore);
      return new ConstructorInjector(membersInjector.getInjectionPoints(), factory.create(), constructorParameterInjectors, membersInjector);
   }



}
