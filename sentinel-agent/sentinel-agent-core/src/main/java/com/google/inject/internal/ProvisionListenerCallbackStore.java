package com.google.inject.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.ProvisionListenerBinding;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

final class ProvisionListenerCallbackStore {
   private static final ImmutableSet<Key<?>> INTERNAL_BINDINGS = ImmutableSet.of(Key.get(Injector.class), Key.get(Stage.class), Key.get(Logger.class));
   private final ImmutableList<ProvisionListenerBinding> listenerBindings;
   private final LoadingCache<KeyBinding, ProvisionListenerStackCallback<?>> cache = CacheBuilder.newBuilder().build(new CacheLoader<KeyBinding, ProvisionListenerStackCallback<?>>() {
      public ProvisionListenerStackCallback<?> load(KeyBinding key) {
         return ProvisionListenerCallbackStore.this.create(key.binding);
      }
   });

   ProvisionListenerCallbackStore(List<ProvisionListenerBinding> listenerBindings) {
      this.listenerBindings = ImmutableList.copyOf(listenerBindings);
   }

   public <T> ProvisionListenerStackCallback<T> get(Binding<T> binding) {
      if (!INTERNAL_BINDINGS.contains(binding.getKey())) {
         ProvisionListenerStackCallback<T> callback = (ProvisionListenerStackCallback)this.cache.getUnchecked(new KeyBinding(binding.getKey(), binding));
         return callback.hasListeners() ? callback : null;
      } else {
         return null;
      }
   }

   boolean remove(Binding<?> type) {
      return this.cache.asMap().remove(type) != null;
   }

   private <T> ProvisionListenerStackCallback<T> create(Binding<T> binding) {
      List<ProvisionListener> listeners = null;
      Iterator i$ = this.listenerBindings.iterator();

      while(i$.hasNext()) {
         ProvisionListenerBinding provisionBinding = (ProvisionListenerBinding)i$.next();
         if (provisionBinding.getBindingMatcher().matches(binding)) {
            if (listeners == null) {
               listeners = Lists.newArrayList();
            }

            listeners.addAll(provisionBinding.getListeners());
         }
      }

      if (listeners != null && !listeners.isEmpty()) {
         return new ProvisionListenerStackCallback(binding, listeners);
      } else {
         return ProvisionListenerStackCallback.emptyListener();
      }
   }

   private static class KeyBinding {
      final Key<?> key;
      final Binding<?> binding;

      KeyBinding(Key<?> key, Binding<?> binding) {
         this.key = key;
         this.binding = binding;
      }

      public boolean equals(Object obj) {
         return obj instanceof KeyBinding && this.key.equals(((KeyBinding)obj).key);
      }

      public int hashCode() {
         return this.key.hashCode();
      }
   }
}
