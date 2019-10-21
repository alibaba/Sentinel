package com.google.inject.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

interface State {
   State NONE = new State() {
      public State parent() {
         throw new UnsupportedOperationException();
      }

      public <T> BindingImpl<T> getExplicitBinding(Key<T> key) {
         return null;
      }

      public Map<Key<?>, Binding<?>> getExplicitBindingsThisLevel() {
         throw new UnsupportedOperationException();
      }

      public void putBinding(Key<?> key, BindingImpl<?> binding) {
         throw new UnsupportedOperationException();
      }

      public ScopeBinding getScopeBinding(Class<? extends Annotation> scopingAnnotation) {
         return null;
      }

      public void putScopeBinding(Class<? extends Annotation> annotationType, ScopeBinding scope) {
         throw new UnsupportedOperationException();
      }

      public void addConverter(TypeConverterBinding typeConverterBinding) {
         throw new UnsupportedOperationException();
      }

      public TypeConverterBinding getConverter(String stringValue, TypeLiteral<?> type, Errors errors, Object source) {
         throw new UnsupportedOperationException();
      }

      public Iterable<TypeConverterBinding> getConvertersThisLevel() {
         return ImmutableSet.of();
      }

      public void addTypeListener(TypeListenerBinding typeListenerBinding) {
         throw new UnsupportedOperationException();
      }

      public List<TypeListenerBinding> getTypeListenerBindings() {
         return ImmutableList.of();
      }

      public void addProvisionListener(ProvisionListenerBinding provisionListenerBinding) {
         throw new UnsupportedOperationException();
      }

      public List<ProvisionListenerBinding> getProvisionListenerBindings() {
         return ImmutableList.of();
      }

      public void addScanner(ModuleAnnotatedMethodScannerBinding scanner) {
         throw new UnsupportedOperationException();
      }

      public List<ModuleAnnotatedMethodScannerBinding> getScannerBindings() {
         return ImmutableList.of();
      }

      public void blacklist(Key<?> key, State state, Object source) {
      }

      public boolean isBlacklisted(Key<?> key) {
         return true;
      }

      public Set<Object> getSourcesForBlacklistedKey(Key<?> key) {
         throw new UnsupportedOperationException();
      }

      public Object lock() {
         throw new UnsupportedOperationException();
      }

      public Object singletonCreationLock() {
         throw new UnsupportedOperationException();
      }

      public Map<Class<? extends Annotation>, Scope> getScopes() {
         return ImmutableMap.of();
      }
   };

   State parent();

   <T> BindingImpl<T> getExplicitBinding(Key<T> var1);

   Map<Key<?>, Binding<?>> getExplicitBindingsThisLevel();

   void putBinding(Key<?> var1, BindingImpl<?> var2);

   ScopeBinding getScopeBinding(Class<? extends Annotation> var1);

   void putScopeBinding(Class<? extends Annotation> var1, ScopeBinding var2);

   void addConverter(TypeConverterBinding var1);

   TypeConverterBinding getConverter(String var1, TypeLiteral<?> var2, Errors var3, Object var4);

   Iterable<TypeConverterBinding> getConvertersThisLevel();

   void addTypeListener(TypeListenerBinding var1);

   List<TypeListenerBinding> getTypeListenerBindings();

   void addProvisionListener(ProvisionListenerBinding var1);

   List<ProvisionListenerBinding> getProvisionListenerBindings();

   void addScanner(ModuleAnnotatedMethodScannerBinding var1);

   List<ModuleAnnotatedMethodScannerBinding> getScannerBindings();

   void blacklist(Key<?> var1, State var2, Object var3);

   boolean isBlacklisted(Key<?> var1);

   Set<Object> getSourcesForBlacklistedKey(Key<?> var1);

   Object lock();

   Map<Class<? extends Annotation>, Scope> getScopes();
}
