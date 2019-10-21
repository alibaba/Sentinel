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
import com.google.inject.Key;
import com.google.inject.internal.InjectorImpl.JitLimitation;
import com.google.inject.spi.Dependency;

/**
 * A placeholder which enables us to swap in the real factory once the injector is created. Used for
 * a linked binding, so that getting the linked binding returns the link's factory.
 */
final class FactoryProxy<T> implements com.google.inject.internal.InternalFactory<T>, CreationListener {

   private final InjectorImpl injector;
   private final Key<T> key;
   private final Key<? extends T> targetKey;
   private final Object source;

   private InternalFactory<? extends T> targetFactory;

   FactoryProxy(InjectorImpl injector, Key<T> key, Key<? extends T> targetKey, Object source) {
      this.injector = injector;
      this.key = key;
      this.targetKey = targetKey;
      this.source = source;
   }

   @Override
   public void notify(final Errors errors) {
      try {
         targetFactory =
                 injector.getInternalFactory(
                         targetKey, errors.withSource(source), JitLimitation.NEW_OR_EXISTING_JIT);
      } catch (ErrorsException e) {
         errors.merge(e.getErrors());
      }
   }

   @Override
   public T get(InternalContext context, Dependency<?> dependency, boolean linked)
           throws InternalProvisionException {
      Key<? extends T> localTargetKey = targetKey;
      context.pushState(localTargetKey, source);

      try {
         return targetFactory.get(context, dependency, true);
      } catch (InternalProvisionException ipe) {
         throw ipe.addSource(localTargetKey);
      } finally {
         context.popState();

      }
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(FactoryProxy.class)
              .add("key", key)
              .add("provider", targetFactory)
              .toString();
   }
}
