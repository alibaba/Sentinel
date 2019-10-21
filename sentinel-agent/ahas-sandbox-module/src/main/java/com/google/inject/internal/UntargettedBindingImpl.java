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
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.UntargettedBinding;

final class UntargettedBindingImpl<T> extends com.google.inject.internal.BindingImpl<T> implements UntargettedBinding<T> {

   UntargettedBindingImpl(InjectorImpl injector, Key<T> key, Object source) {
      super(
              injector,
              key,
              source,
              new InternalFactory<T>() {
                 @Override
                 public T get(InternalContext context, Dependency<?> dependency, boolean linked) {
                    throw new AssertionError();
                 }
              },
              Scoping.UNSCOPED);
   }

   public UntargettedBindingImpl(Object source, Key<T> key, Scoping scoping) {
      super(source, key, scoping);
   }

   @Override
   public <V> V acceptTargetVisitor(BindingTargetVisitor<? super T, V> visitor) {
      return visitor.visit(this);
   }

   @Override
   public BindingImpl<T> withScoping(Scoping scoping) {
      return new UntargettedBindingImpl<T>(getSource(), getKey(), scoping);
   }

   @Override
   public BindingImpl<T> withKey(Key<T> key) {
      return new UntargettedBindingImpl<T>(getSource(), key, getScoping());
   }

   @Override
   public void applyTo(Binder binder) {
      getScoping().applyTo(binder.withSource(getSource()).bind(getKey()));
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(UntargettedBinding.class)
              .add("key", getKey())
              .add("source", getSource())
              .toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UntargettedBindingImpl) {
         UntargettedBindingImpl<?> o = (UntargettedBindingImpl<?>) obj;
         return getKey().equals(o.getKey()) && getScoping().equals(o.getScoping());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(getKey(), getScoping());
   }
}
