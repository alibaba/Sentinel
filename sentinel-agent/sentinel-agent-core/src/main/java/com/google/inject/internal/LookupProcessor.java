package com.google.inject.internal;

import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.spi.MembersInjectorLookup;
import com.google.inject.spi.ProviderLookup;

final class LookupProcessor extends AbstractProcessor {
   LookupProcessor(Errors errors) {
      super(errors);
   }

   public <T> Boolean visit(MembersInjectorLookup<T> lookup) {
      try {
         MembersInjector<T> membersInjector = this.injector.membersInjectorStore.get(lookup.getType(), this.errors);
         lookup.initializeDelegate(membersInjector);
      } catch (ErrorsException var3) {
         this.errors.merge(var3.getErrors());
      }

      return true;
   }

   public <T> Boolean visit(ProviderLookup<T> lookup) {
      try {
         Provider<T> provider = this.injector.getProviderOrThrow(lookup.getDependency(), this.errors);
         lookup.initializeDelegate(provider);
      } catch (ErrorsException var3) {
         this.errors.merge(var3.getErrors());
      }

      return true;
   }
}
