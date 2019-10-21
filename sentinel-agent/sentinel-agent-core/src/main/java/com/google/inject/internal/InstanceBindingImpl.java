/*
 * Copyright (C) 2008 Google Inc.
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
import com.google.inject.spi.*;

import java.util.Set;

final class InstanceBindingImpl<T> extends BindingImpl<T> implements InstanceBinding<T> {

   final T instance;
   final ImmutableSet<InjectionPoint> injectionPoints;

   public InstanceBindingImpl(
           InjectorImpl injector,
           Key<T> key,
           Object source,
           InternalFactory<? extends T> internalFactory,
           Set<InjectionPoint> injectionPoints,
           T instance) {
      super(injector, key, source, internalFactory, Scoping.EAGER_SINGLETON);
      this.injectionPoints = ImmutableSet.copyOf(injectionPoints);
      this.instance = instance;
   }

   public InstanceBindingImpl(
           Object source, Key<T> key, Scoping scoping, Set<InjectionPoint> injectionPoints, T instance) {
      super(source, key, scoping);
      this.injectionPoints = ImmutableSet.copyOf(injectionPoints);
      this.instance = instance;
   }

   @Override
   public <V> V acceptTargetVisitor(BindingTargetVisitor<? super T, V> visitor) {
      return visitor.visit(this);
   }

   @Override
   public T getInstance() {
      return instance;
   }

   @Override
   public Set<InjectionPoint> getInjectionPoints() {
      return injectionPoints;
   }

   @Override
   public Set<Dependency<?>> getDependencies() {
      return instance instanceof HasDependencies
              ? ImmutableSet.copyOf(((HasDependencies) instance).getDependencies())
              : Dependency.forInjectionPoints(injectionPoints);
   }

   @Override
   public BindingImpl<T> withScoping(Scoping scoping) {
      return new InstanceBindingImpl<T>(getSource(), getKey(), scoping, injectionPoints, instance);
   }

   @Override
   public BindingImpl<T> withKey(Key<T> key) {
      return new InstanceBindingImpl<T>(getSource(), key, getScoping(), injectionPoints, instance);
   }

   @Override
   public void applyTo(Binder binder) {
      // instance bindings aren't scoped
      binder.withSource(getSource()).bind(getKey()).toInstance(instance);
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(InstanceBinding.class)
              .add("key", getKey())
              .add("source", getSource())
              .add("instance", instance)
              .toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof InstanceBindingImpl) {
         InstanceBindingImpl<?> o = (InstanceBindingImpl<?>) obj;
         return getKey().equals(o.getKey())
                 && getScoping().equals(o.getScoping())
                 && Objects.equal(instance, o.instance);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(getKey(), getScoping());
   }
}
