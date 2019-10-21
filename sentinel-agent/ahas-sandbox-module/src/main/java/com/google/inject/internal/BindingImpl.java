/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.internal;

import com.google.common.base.MoreObjects;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.BindingScopingVisitor;

/** @author crazybob@google.com (Bob Lee) */
public abstract class BindingImpl<T> implements Binding<T> {

   private final InjectorImpl injector;
   private final Key<T> key;
   private final Object source;
   private final Scoping scoping;
   private final InternalFactory<? extends T> internalFactory;

   public BindingImpl(
           InjectorImpl injector,
           Key<T> key,
           Object source,
           InternalFactory<? extends T> internalFactory,
           Scoping scoping) {
      this.injector = injector;
      this.key = key;
      this.source = source;
      this.internalFactory = internalFactory;
      this.scoping = scoping;
   }

   protected BindingImpl(Object source, Key<T> key, Scoping scoping) {
      this.internalFactory = null;
      this.injector = null;
      this.source = source;
      this.key = key;
      this.scoping = scoping;
   }

   @Override
   public Key<T> getKey() {
      return key;
   }

   @Override
   public Object getSource() {
      return source;
   }

   private volatile Provider<T> provider;

   @Override
   public Provider<T> getProvider() {
      if (provider == null) {
         if (injector == null) {
            throw new UnsupportedOperationException("getProvider() not supported for module bindings");
         }

         provider = injector.getProvider(key);
      }
      return provider;
   }

   public InternalFactory<? extends T> getInternalFactory() {
      return internalFactory;
   }

   public Scoping getScoping() {
      return scoping;
   }

   /**
    * Is this a constant binding? This returns true for constant bindings as well as toInstance()
    * bindings.
    */
   public boolean isConstant() {
      return this instanceof com.google.inject.spi.InstanceBinding;
   }

   @Override
   public <V> V acceptVisitor(com.google.inject.spi.ElementVisitor<V> visitor) {
      return visitor.visit(this);
   }

   @Override
   public <V> V acceptScopingVisitor(BindingScopingVisitor<V> visitor) {
      return scoping.acceptVisitor(visitor);
   }

   protected BindingImpl<T> withScoping(Scoping scoping) {
      throw new AssertionError();
   }

   protected BindingImpl<T> withKey(Key<T> key) {
      throw new AssertionError();
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(Binding.class)
              .add("key", key)
              .add("scope", scoping)
              .add("source", source)
              .toString();
   }

   public InjectorImpl getInjector() {
      return injector;
   }
}
