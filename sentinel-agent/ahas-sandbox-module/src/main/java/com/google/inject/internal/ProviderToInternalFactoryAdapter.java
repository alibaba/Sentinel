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

import com.google.inject.Provider;

/** @author crazybob@google.com (Bob Lee) */
final class ProviderToInternalFactoryAdapter<T> implements Provider<T> {

   private final InjectorImpl injector;
   private final InternalFactory<? extends T> internalFactory;

   public ProviderToInternalFactoryAdapter(
           InjectorImpl injector, InternalFactory<? extends T> internalFactory) {
      this.injector = injector;
      this.internalFactory = internalFactory;
   }

   @Override
   public T get() {
      InternalContext context = injector.enterContext();
      try {
         // Always pretend that we are a linked binding, to support
         // scoping implicit bindings.  If we are not actually a linked
         // binding, we'll fail properly elsewhere in the chain.
         T t = internalFactory.get(context, context.getDependency(), true);
         return t;
      } catch (InternalProvisionException e) {
         throw e.toProvisionException();
      } finally {
         context.close();
      }
   }

   /** Exposed for SingletonScope. */
   InjectorImpl getInjector() {
      return injector;
   }

   @Override
   public String toString() {
      return internalFactory.toString();
   }
}
