package com.google.inject.internal;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import com.google.inject.spi.Dependency;

final class InternalFactoryToProviderAdapter<T> implements InternalFactory<T> {
   private final Provider<? extends T> provider;
   private final Object source;

   public InternalFactoryToProviderAdapter(Provider<? extends T> provider, Object source) {
      this.provider = (Provider)Preconditions.checkNotNull(provider, "provider");
      this.source = Preconditions.checkNotNull(source, "source");
   }

   public T get(InternalContext context, Dependency<?> dependency, boolean linked) throws InternalProvisionException {
      try {
         T t = this.provider.get();
         if (t == null && !dependency.isNullable()) {
            InternalProvisionException.onNullInjectedIntoNonNullableDependency(this.source, dependency);
         }

         return t;
      } catch (RuntimeException var5) {
         throw InternalProvisionException.errorInProvider(var5).addSource(this.source);
      }
   }

   public String toString() {
      return this.provider.toString();
   }
}
