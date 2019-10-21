package com.google.inject.binder;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.lang.reflect.Constructor;

public interface LinkedBindingBuilder<T> extends ScopedBindingBuilder {
   ScopedBindingBuilder to(Class<? extends T> var1);

   ScopedBindingBuilder to(TypeLiteral<? extends T> var1);

   ScopedBindingBuilder to(Key<? extends T> var1);

   void toInstance(T var1);

   ScopedBindingBuilder toProvider(Provider<? extends T> var1);

   ScopedBindingBuilder toProvider(javax.inject.Provider<? extends T> var1);

   ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends T>> var1);

   ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> var1);

   ScopedBindingBuilder toProvider(Key<? extends javax.inject.Provider<? extends T>> var1);

   <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> var1);

   <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> var1, TypeLiteral<? extends S> var2);
}
