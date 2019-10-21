package com.google.inject.multibindings;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.internal.RealMultibinder;
import java.lang.annotation.Annotation;

public class Multibinder<T> {
   private final RealMultibinder<T> delegate;

   public static <T> Multibinder<T> newSetBinder(Binder binder, TypeLiteral<T> type) {
      return newSetBinder(binder, Key.get(type));
   }

   public static <T> Multibinder<T> newSetBinder(Binder binder, Class<T> type) {
      return newSetBinder(binder, Key.get(type));
   }

   public static <T> Multibinder<T> newSetBinder(Binder binder, TypeLiteral<T> type, Annotation annotation) {
      return newSetBinder(binder, Key.get(type, annotation));
   }

   public static <T> Multibinder<T> newSetBinder(Binder binder, Class<T> type, Annotation annotation) {
      return newSetBinder(binder, Key.get(type, annotation));
   }

   public static <T> Multibinder<T> newSetBinder(Binder binder, TypeLiteral<T> type, Class<? extends Annotation> annotationType) {
      return newSetBinder(binder, Key.get(type, annotationType));
   }

   public static <T> Multibinder<T> newSetBinder(Binder binder, Key<T> key) {
      return new Multibinder(RealMultibinder.newRealSetBinder(binder.skipSources(Multibinder.class), key));
   }

   public static <T> Multibinder<T> newSetBinder(Binder binder, Class<T> type, Class<? extends Annotation> annotationType) {
      return newSetBinder(binder, Key.get(type, annotationType));
   }

   private Multibinder(RealMultibinder<T> delegate) {
      this.delegate = delegate;
   }

   public Multibinder<T> permitDuplicates() {
      this.delegate.permitDuplicates();
      return this;
   }

   public LinkedBindingBuilder<T> addBinding() {
      return this.delegate.addBinding();
   }

   public boolean equals(Object obj) {
      return obj instanceof Multibinder ? this.delegate.equals(((Multibinder)obj).delegate) : false;
   }

   public int hashCode() {
      return this.delegate.hashCode();
   }
}
