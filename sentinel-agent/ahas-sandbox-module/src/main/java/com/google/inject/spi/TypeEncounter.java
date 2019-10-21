package com.google.inject.spi;

import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

public interface TypeEncounter<I> {
   void addError(String var1, Object... var2);

   void addError(Throwable var1);

   void addError(Message var1);

   <T> Provider<T> getProvider(Key<T> var1);

   <T> Provider<T> getProvider(Class<T> var1);

   <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> var1);

   <T> MembersInjector<T> getMembersInjector(Class<T> var1);

   void register(MembersInjector<? super I> var1);

   void register(InjectionListener<? super I> var1);
}
