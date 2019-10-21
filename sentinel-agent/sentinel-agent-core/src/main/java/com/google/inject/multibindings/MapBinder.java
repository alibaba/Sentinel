package com.google.inject.multibindings;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.internal.RealMapBinder;

import java.lang.annotation.Annotation;

public class MapBinder<K, V> {
   private final RealMapBinder<K, V> delegate;

   public static <K, V> MapBinder<K, V> newMapBinder(Binder binder, TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return new MapBinder(RealMapBinder.newMapRealBinder(binder.skipSources(MapBinder.class), keyType, valueType));
   }

   public static <K, V> MapBinder<K, V> newMapBinder(Binder binder, Class<K> keyType, Class<V> valueType) {
      return newMapBinder(binder, TypeLiteral.get(keyType), TypeLiteral.get(valueType));
   }

   public static <K, V> MapBinder<K, V> newMapBinder(Binder binder, TypeLiteral<K> keyType, TypeLiteral<V> valueType, Annotation annotation) {
      return new MapBinder(RealMapBinder.newRealMapBinder(binder.skipSources(MapBinder.class), keyType, valueType, annotation));
   }

   public static <K, V> MapBinder<K, V> newMapBinder(Binder binder, Class<K> keyType, Class<V> valueType, Annotation annotation) {
      return newMapBinder(binder, TypeLiteral.get(keyType), TypeLiteral.get(valueType), annotation);
   }

   public static <K, V> MapBinder<K, V> newMapBinder(Binder binder, TypeLiteral<K> keyType, TypeLiteral<V> valueType, Class<? extends Annotation> annotationType) {
      return new MapBinder(RealMapBinder.newRealMapBinder(binder.skipSources(MapBinder.class), keyType, valueType, annotationType));
   }

   public static <K, V> MapBinder<K, V> newMapBinder(Binder binder, Class<K> keyType, Class<V> valueType, Class<? extends Annotation> annotationType) {
      return newMapBinder(binder, TypeLiteral.get(keyType), TypeLiteral.get(valueType), annotationType);
   }

   private MapBinder(RealMapBinder<K, V> delegate) {
      this.delegate = delegate;
   }

   public MapBinder<K, V> permitDuplicates() {
      this.delegate.permitDuplicates();
      return this;
   }

   public LinkedBindingBuilder<V> addBinding(K key) {
      return this.delegate.addBinding(key);
   }

   public boolean equals(Object obj) {
      return obj instanceof MapBinder ? this.delegate.equals(((MapBinder)obj).delegate) : false;
   }

   public int hashCode() {
      return this.delegate.hashCode();
   }
}
