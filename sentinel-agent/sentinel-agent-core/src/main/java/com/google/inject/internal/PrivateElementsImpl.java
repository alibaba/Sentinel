package com.google.inject.internal;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateBinder;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.PrivateElements;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class PrivateElementsImpl implements PrivateElements {
   private final Object source;
   private List<com.google.inject.spi.Element> elementsMutable = Lists.newArrayList();
   private List<ExposureBuilder<?>> exposureBuilders = Lists.newArrayList();
   private ImmutableList<com.google.inject.spi.Element> elements;
   private ImmutableMap<Key<?>, Object> exposedKeysToSources;
   private Injector injector;

   public PrivateElementsImpl(Object source) {
      this.source = Preconditions.checkNotNull(source, "source");
   }

   public Object getSource() {
      return this.source;
   }

   public List<com.google.inject.spi.Element> getElements() {
      if (this.elements == null) {
         this.elements = ImmutableList.copyOf(this.elementsMutable);
         this.elementsMutable = null;
      }

      return this.elements;
   }

   public Injector getInjector() {
      return this.injector;
   }

   public void initInjector(Injector injector) {
      Preconditions.checkState(this.injector == null, "injector already initialized");
      this.injector = (Injector)Preconditions.checkNotNull(injector, "injector");
   }

   public Set<Key<?>> getExposedKeys() {
      if (this.exposedKeysToSources == null) {
         Map<Key<?>, Object> exposedKeysToSourcesMutable = Maps.newLinkedHashMap();
         Iterator i$ = this.exposureBuilders.iterator();

         while(i$.hasNext()) {
            ExposureBuilder<?> exposureBuilder = (ExposureBuilder)i$.next();
            exposedKeysToSourcesMutable.put(exposureBuilder.getKey(), exposureBuilder.getSource());
         }

         this.exposedKeysToSources = ImmutableMap.copyOf(exposedKeysToSourcesMutable);
         this.exposureBuilders = null;
      }

      return this.exposedKeysToSources.keySet();
   }

   public <T> T acceptVisitor(ElementVisitor<T> visitor) {
      return visitor.visit((PrivateElements)this);
   }

   public List<com.google.inject.spi.Element> getElementsMutable() {
      return this.elementsMutable;
   }

   public void addExposureBuilder(ExposureBuilder<?> exposureBuilder) {
      this.exposureBuilders.add(exposureBuilder);
   }

   public void applyTo(Binder binder) {
      PrivateBinder privateBinder = binder.withSource(this.source).newPrivateBinder();
      Iterator i$ = this.getElements().iterator();

      while(i$.hasNext()) {
         com.google.inject.spi.Element element = (com.google.inject.spi.Element)i$.next();
         element.applyTo(privateBinder);
      }

      this.getExposedKeys();
      i$ = this.exposedKeysToSources.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Key<?>, Object> entry = (Entry)i$.next();
         privateBinder.withSource(entry.getValue()).expose((Key)entry.getKey());
      }

   }

   public Object getExposedSource(Key<?> key) {
      this.getExposedKeys();
      Object source = this.exposedKeysToSources.get(key);
      Preconditions.checkArgument(source != null, "%s not exposed by %s.", key, this);
      return source;
   }

   public String toString() {
      return MoreObjects.toStringHelper(PrivateElements.class).add("exposedKeys", this.getExposedKeys()).add("source", this.getSource()).toString();
   }
}
