/*
 * Copyright (C) 2007 Google Inc.
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
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.LinkedKeyBinding;
import java.util.Set;

public final class LinkedBindingImpl<T> extends com.google.inject.internal.BindingImpl<T>
        implements LinkedKeyBinding<T>, HasDependencies {

   final Key<? extends T> targetKey;

   public LinkedBindingImpl(
           InjectorImpl injector,
           Key<T> key,
           Object source,
           InternalFactory<? extends T> internalFactory,
           Scoping scoping,
           Key<? extends T> targetKey) {
      super(injector, key, source, internalFactory, scoping);
      this.targetKey = targetKey;
   }

   public LinkedBindingImpl(Object source, Key<T> key, Scoping scoping, Key<? extends T> targetKey) {
      super(source, key, scoping);
      this.targetKey = targetKey;
   }

   @Override
   public <V> V acceptTargetVisitor(BindingTargetVisitor<? super T, V> visitor) {
      return visitor.visit(this);
   }

   @Override
   public Key<? extends T> getLinkedKey() {
      return targetKey;
   }

   @Override
   public Set<Dependency<?>> getDependencies() {
      return ImmutableSet.<Dependency<?>>of(Dependency.get(targetKey));
   }

   @Override
   public BindingImpl<T> withScoping(Scoping scoping) {
      return new LinkedBindingImpl<T>(getSource(), getKey(), scoping, targetKey);
   }

   @Override
   public BindingImpl<T> withKey(Key<T> key) {
      return new LinkedBindingImpl<T>(getSource(), key, getScoping(), targetKey);
   }

   @Override
   public void applyTo(Binder binder) {
      getScoping().applyTo(binder.withSource(getSource()).bind(getKey()).to(getLinkedKey()));
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(LinkedKeyBinding.class)
              .add("key", getKey())
              .add("source", getSource())
              .add("scope", getScoping())
              .add("target", targetKey)
              .toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof LinkedBindingImpl) {
         LinkedBindingImpl<?> o = (LinkedBindingImpl<?>) obj;
         return getKey().equals(o.getKey())
                 && getScoping().equals(o.getScoping())
                 && Objects.equal(targetKey, o.targetKey);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(getKey(), getScoping(), targetKey);
   }
}
