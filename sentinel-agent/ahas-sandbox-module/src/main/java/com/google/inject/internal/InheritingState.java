package com.google.inject.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.ModuleAnnotatedMethodScannerBinding;
import com.google.inject.spi.ProvisionListenerBinding;
import com.google.inject.spi.ScopeBinding;
import com.google.inject.spi.TypeConverterBinding;
import com.google.inject.spi.TypeListenerBinding;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

final class InheritingState implements com.google.inject.internal.State {
   private final State parent;
   private final Map<Key<?>, Binding<?>> explicitBindingsMutable = Maps.newLinkedHashMap();
   private final Map<Key<?>, Binding<?>> explicitBindings;
   private final Map<Class<? extends Annotation>, ScopeBinding> scopes;
   private final List<TypeConverterBinding> converters;
   private final List<TypeListenerBinding> typeListenerBindings;
   private final List<ProvisionListenerBinding> provisionListenerBindings;
   private final List<ModuleAnnotatedMethodScannerBinding> scannerBindings;
   private final WeakKeySet blacklistedKeys;
   private final Object lock;

   InheritingState(State parent) {
      this.explicitBindings = Collections.unmodifiableMap(this.explicitBindingsMutable);
      this.scopes = Maps.newHashMap();
      this.converters = Lists.newArrayList();
      this.typeListenerBindings = Lists.newArrayList();
      this.provisionListenerBindings = Lists.newArrayList();
      this.scannerBindings = Lists.newArrayList();
      this.parent = (State)Preconditions.checkNotNull(parent, "parent");
      this.lock = parent == State.NONE ? this : parent.lock();
      this.blacklistedKeys = new WeakKeySet(this.lock);
   }

   public State parent() {
      return this.parent;
   }

   public <T> BindingImpl<T> getExplicitBinding(Key<T> key) {
      Binding<?> binding = (Binding)this.explicitBindings.get(key);
      return binding != null ? (BindingImpl)binding : this.parent.getExplicitBinding(key);
   }

   public Map<Key<?>, Binding<?>> getExplicitBindingsThisLevel() {
      return this.explicitBindings;
   }

   public void putBinding(Key<?> key, BindingImpl<?> binding) {
      this.explicitBindingsMutable.put(key, binding);
   }

   public ScopeBinding getScopeBinding(Class<? extends Annotation> annotationType) {
      ScopeBinding scopeBinding = (ScopeBinding)this.scopes.get(annotationType);
      return scopeBinding != null ? scopeBinding : this.parent.getScopeBinding(annotationType);
   }

   public void putScopeBinding(Class<? extends Annotation> annotationType, ScopeBinding scope) {
      this.scopes.put(annotationType, scope);
   }

   public Iterable<TypeConverterBinding> getConvertersThisLevel() {
      return this.converters;
   }

   public void addConverter(TypeConverterBinding typeConverterBinding) {
      this.converters.add(typeConverterBinding);
   }

   public TypeConverterBinding getConverter(String stringValue, TypeLiteral<?> type, Errors errors, Object source) {
      TypeConverterBinding matchingConverter = null;

      for(Object s = this; s != State.NONE; s = ((State)s).parent()) {
         Iterator i$ = ((State)s).getConvertersThisLevel().iterator();

         while(i$.hasNext()) {
            TypeConverterBinding converter = (TypeConverterBinding)i$.next();
            if (converter.getTypeMatcher().matches(type)) {
               if (matchingConverter != null) {
                  errors.ambiguousTypeConversion(stringValue, source, type, matchingConverter, converter);
               }

               matchingConverter = converter;
            }
         }
      }

      return matchingConverter;
   }

   public void addTypeListener(TypeListenerBinding listenerBinding) {
      this.typeListenerBindings.add(listenerBinding);
   }

   public List<TypeListenerBinding> getTypeListenerBindings() {
      List<TypeListenerBinding> parentBindings = this.parent.getTypeListenerBindings();
      List<TypeListenerBinding> result = Lists.newArrayListWithCapacity(parentBindings.size() + this.typeListenerBindings.size());
      result.addAll(parentBindings);
      result.addAll(this.typeListenerBindings);
      return result;
   }

   public void addProvisionListener(ProvisionListenerBinding listenerBinding) {
      this.provisionListenerBindings.add(listenerBinding);
   }

   public List<ProvisionListenerBinding> getProvisionListenerBindings() {
      List<ProvisionListenerBinding> parentBindings = this.parent.getProvisionListenerBindings();
      List<ProvisionListenerBinding> result = Lists.newArrayListWithCapacity(parentBindings.size() + this.provisionListenerBindings.size());
      result.addAll(parentBindings);
      result.addAll(this.provisionListenerBindings);
      return result;
   }

   public void addScanner(ModuleAnnotatedMethodScannerBinding scanner) {
      this.scannerBindings.add(scanner);
   }

   public List<ModuleAnnotatedMethodScannerBinding> getScannerBindings() {
      List<ModuleAnnotatedMethodScannerBinding> parentBindings = this.parent.getScannerBindings();
      List<ModuleAnnotatedMethodScannerBinding> result = Lists.newArrayListWithCapacity(parentBindings.size() + this.scannerBindings.size());
      result.addAll(parentBindings);
      result.addAll(this.scannerBindings);
      return result;
   }

   public void blacklist(Key<?> key, State state, Object source) {
      this.parent.blacklist(key, state, source);
      this.blacklistedKeys.add(key, state, source);
   }

   public boolean isBlacklisted(Key<?> key) {
      return this.blacklistedKeys.contains(key);
   }

   public Set<Object> getSourcesForBlacklistedKey(Key<?> key) {
      return this.blacklistedKeys.getSources(key);
   }

   public Object lock() {
      return this.lock;
   }

   public Map<Class<? extends Annotation>, Scope> getScopes() {
      Builder<Class<? extends Annotation>, Scope> builder = ImmutableMap.builder();
      Iterator i$ = this.scopes.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Class<? extends Annotation>, ScopeBinding> entry = (Entry)i$.next();
         builder.put(entry.getKey(), ((ScopeBinding)entry.getValue()).getScope());
      }

      return builder.build();
   }
}
