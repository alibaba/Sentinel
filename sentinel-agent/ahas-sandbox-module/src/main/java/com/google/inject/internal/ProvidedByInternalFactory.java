/*
 * Copyright (C) 2011 Google Inc.
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

import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import com.google.inject.internal.InjectorImpl.JitLimitation;

import javax.inject.Provider;

/**
 * An {@link InternalFactory} for {@literal @}{@link ProvidedBy} bindings.
 *
 * @author sameb@google.com (Sam Berlin)
 */
class ProvidedByInternalFactory<T> extends ProviderInternalFactory<T> implements DelayedInitialize {

   private final Class<?> rawType;
   private final Class<? extends Provider<?>> providerType;
   private final Key<? extends Provider<T>> providerKey;
   private BindingImpl<? extends Provider<T>> providerBinding;
   private ProvisionListenerStackCallback<T> provisionCallback;

   ProvidedByInternalFactory(
           Class<?> rawType,
           Class<? extends Provider<?>> providerType,
           Key<? extends Provider<T>> providerKey) {
      super(providerKey);
      this.rawType = rawType;
      this.providerType = providerType;
      this.providerKey = providerKey;
   }

   void setProvisionListenerCallback(ProvisionListenerStackCallback<T> listener) {
      provisionCallback = listener;
   }

   @Override
   public void initialize(InjectorImpl injector, Errors errors) throws ErrorsException {
      providerBinding =
              injector.getBindingOrThrow(providerKey, errors, JitLimitation.NEW_OR_EXISTING_JIT);
   }

   @Override
   public T get(InternalContext context, com.google.inject.spi.Dependency<?> dependency, boolean linked)
           throws InternalProvisionException {
      BindingImpl<? extends Provider<T>> localProviderBinding = providerBinding;
      if (localProviderBinding == null) {
         throw new IllegalStateException("not initialized");
      }
      Key<? extends Provider<T>> localProviderKey = providerKey;
      context.pushState(localProviderKey, localProviderBinding.getSource());

      try {
         Provider<? extends T> provider =
                 localProviderBinding.getInternalFactory().get(context, dependency, true);
         return circularGet(provider, context, dependency, provisionCallback);
      } catch (InternalProvisionException ipe) {
         throw ipe.addSource(localProviderKey);
      } finally {
         context.popState();

      }
   }

   @Override
   protected T provision(
           javax.inject.Provider<? extends T> provider,
           com.google.inject.spi.Dependency<?> dependency,
           ConstructionContext<T> constructionContext)
           throws InternalProvisionException {
      try {
         Object o = super.provision(provider, dependency, constructionContext);
         if (o != null && !rawType.isInstance(o)) {
            throw InternalProvisionException.subtypeNotProvided(providerType, rawType);
         }
         @SuppressWarnings("unchecked") // protected by isInstance() check above
                 T t = (T) o;
         return t;
      } catch (RuntimeException e) {
         throw InternalProvisionException.errorInProvider(e).addSource(source);
      }
   }
}
