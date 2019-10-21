package com.google.inject.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeEncounter;

import java.util.List;

final class EncounterImpl<T> implements TypeEncounter<T> {
   private final Errors errors;
   private final Lookups lookups;
   private List<MembersInjector<? super T>> membersInjectors;
   private List<InjectionListener<? super T>> injectionListeners;
   private boolean valid = true;

   EncounterImpl(Errors errors, Lookups lookups) {
      this.errors = errors;
      this.lookups = lookups;
   }

   void invalidate() {
      this.valid = false;
   }

   ImmutableSet<MembersInjector<? super T>> getMembersInjectors() {
      return this.membersInjectors == null ? ImmutableSet.of() : ImmutableSet.copyOf(this.membersInjectors);
   }

   ImmutableSet<InjectionListener<? super T>> getInjectionListeners() {
      return this.injectionListeners == null ? ImmutableSet.of() : ImmutableSet.copyOf(this.injectionListeners);
   }

   public void register(MembersInjector<? super T> membersInjector) {
      Preconditions.checkState(this.valid, "Encounters may not be used after hear() returns.");
      if (this.membersInjectors == null) {
         this.membersInjectors = Lists.newArrayList();
      }

      this.membersInjectors.add(membersInjector);
   }

   public void register(InjectionListener<? super T> injectionListener) {
      Preconditions.checkState(this.valid, "Encounters may not be used after hear() returns.");
      if (this.injectionListeners == null) {
         this.injectionListeners = Lists.newArrayList();
      }

      this.injectionListeners.add(injectionListener);
   }

   public void addError(String message, Object... arguments) {
      Preconditions.checkState(this.valid, "Encounters may not be used after hear() returns.");
      this.errors.addMessage(message, arguments);
   }

   public void addError(Throwable t) {
      Preconditions.checkState(this.valid, "Encounters may not be used after hear() returns.");
      this.errors.errorInUserCode(t, "An exception was caught and reported. Message: %s", t.getMessage());
   }

   public void addError(Message message) {
      Preconditions.checkState(this.valid, "Encounters may not be used after hear() returns.");
      this.errors.addMessage(message);
   }

   public <T> Provider<T> getProvider(Key<T> key) {
      Preconditions.checkState(this.valid, "Encounters may not be used after hear() returns.");
      return this.lookups.getProvider(key);
   }

   public <T> Provider<T> getProvider(Class<T> type) {
      return this.getProvider(Key.get(type));
   }

   public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
      Preconditions.checkState(this.valid, "Encounters may not be used after hear() returns.");
      return this.lookups.getMembersInjector(typeLiteral);
   }

   public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
      return this.getMembersInjector(TypeLiteral.get(type));
   }
}
