package com.google.inject.internal;

import com.google.common.collect.Lists;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.MembersInjectorLookup;
import com.google.inject.spi.ProviderLookup;

import java.util.List;

final class DeferredLookups implements Lookups {
   private final InjectorImpl injector;
   private final List<com.google.inject.spi.Element> lookups = Lists.newArrayList();

   DeferredLookups(InjectorImpl injector) {
      this.injector = injector;
   }

   void initialize(Errors errors) {
      this.injector.lookups = this.injector;
      (new LookupProcessor(errors)).process(this.injector, this.lookups);
   }

   public <T> Provider<T> getProvider(Key<T> key) {
      ProviderLookup<T> lookup = new ProviderLookup(key, key);
      this.lookups.add(lookup);
      return lookup.getProvider();
   }

   public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> type) {
      MembersInjectorLookup<T> lookup = new MembersInjectorLookup(type, type);
      this.lookups.add(lookup);
      return lookup.getMembersInjector();
   }
}
