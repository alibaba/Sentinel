package com.google.inject.multibindings;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.internal.RealOptionalBinder;

public class OptionalBinder<T> {
   private final RealOptionalBinder<T> delegate;

   public static <T> OptionalBinder<T> newOptionalBinder(Binder binder, Class<T> type) {
      return new OptionalBinder(RealOptionalBinder.newRealOptionalBinder(binder.skipSources(OptionalBinder.class), Key.get(type)));
   }

   public static <T> OptionalBinder<T> newOptionalBinder(Binder binder, TypeLiteral<T> type) {
      return new OptionalBinder(RealOptionalBinder.newRealOptionalBinder(binder.skipSources(OptionalBinder.class), Key.get(type)));
   }

   public static <T> OptionalBinder<T> newOptionalBinder(Binder binder, Key<T> type) {
      return new OptionalBinder(RealOptionalBinder.newRealOptionalBinder(binder.skipSources(OptionalBinder.class), type));
   }

   private OptionalBinder(RealOptionalBinder<T> delegate) {
      this.delegate = delegate;
   }

   public LinkedBindingBuilder<T> setDefault() {
      return this.delegate.setDefault();
   }

   public LinkedBindingBuilder<T> setBinding() {
      return this.delegate.setBinding();
   }

   public boolean equals(Object obj) {
      return obj instanceof OptionalBinder ? this.delegate.equals(((OptionalBinder)obj).delegate) : false;
   }

   public int hashCode() {
      return this.delegate.hashCode();
   }
}
