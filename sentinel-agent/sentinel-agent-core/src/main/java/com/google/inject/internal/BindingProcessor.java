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

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.*;

import java.util.Set;

/**
 * Handles {@link Binder#bind} and {@link Binder#bindConstant} elements.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 */
final class BindingProcessor extends AbstractBindingProcessor {

   private final Initializer initializer;

   BindingProcessor(Errors errors, Initializer initializer, ProcessedBindingData bindingData) {
      super(errors, bindingData);
      this.initializer = initializer;
   }

   @Override
   public <T> Boolean visit(Binding<T> command) {
      Class<?> rawType = command.getKey().getTypeLiteral().getRawType();
      if (Void.class.equals(rawType)) {
         if (command instanceof ProviderInstanceBinding
                 && ((ProviderInstanceBinding) command).getUserSuppliedProvider()
                 instanceof ProviderMethod) {
            errors.voidProviderMethod();
         } else {
            errors.missingConstantValues();
         }
         return true;
      }

      if (rawType == Provider.class) {
         errors.bindingToProvider();
         return true;
      }

      return command.acceptTargetVisitor(
              new Processor<T, Boolean>((BindingImpl<T>) command) {
                 @Override
                 public Boolean visit(ConstructorBinding<? extends T> binding) {
                    prepareBinding();
                    try {
                       ConstructorBindingImpl<T> onInjector =
                               ConstructorBindingImpl.create(
                                       injector,
                                       key,
                                       binding.getConstructor(),
                                       source,
                                       scoping,
                                       errors,
                                       false,
                                       false);
                       scheduleInitialization(onInjector);
                       putBinding(onInjector);
                    } catch (ErrorsException e) {
                       errors.merge(e.getErrors());
                       putBinding(invalidBinding(injector, key, source));
                    }
                    return true;
                 }

                 @Override
                 public Boolean visit(InstanceBinding<? extends T> binding) {
                    prepareBinding();
                    Set<InjectionPoint> injectionPoints = binding.getInjectionPoints();
                    T instance = binding.getInstance();
                    @SuppressWarnings("unchecked") // safe to cast to binding<T> because
                            // the processor was constructed w/ it
                            Initializable<T> ref =
                            initializer.requestInjection(
                                    injector, instance, (Binding<T>) binding, source, injectionPoints);
                    ConstantFactory<? extends T> factory = new ConstantFactory<>(ref);
                    InternalFactory<? extends T> scopedFactory =
                            Scoping.scope(key, injector, factory, source, scoping);
                    putBinding(
                            new InstanceBindingImpl<T>(
                                    injector, key, source, scopedFactory, injectionPoints, instance));
                    return true;
                 }

                 @Override
                 public Boolean visit(ProviderInstanceBinding<? extends T> binding) {
                    prepareBinding();
                    javax.inject.Provider<? extends T> provider = binding.getUserSuppliedProvider();
                    if (provider instanceof InternalProviderInstanceBindingImpl.Factory) {
                       @SuppressWarnings("unchecked")
                       InternalProviderInstanceBindingImpl.Factory<T> asProviderMethod =
                               (InternalProviderInstanceBindingImpl.Factory<T>) provider;
                       return visitInternalProviderInstanceBindingFactory(asProviderMethod);
                    }
                    Set<InjectionPoint> injectionPoints = binding.getInjectionPoints();
                    Initializable<? extends javax.inject.Provider<? extends T>> initializable =
                            initializer.<javax.inject.Provider<? extends T>>requestInjection(
                                    injector, provider, null, source, injectionPoints);
                    // always visited with Binding<T>
                    @SuppressWarnings("unchecked")
                    InternalFactory<T> factory =
                            new InternalFactoryToInitializableAdapter<T>(
                                    initializable,
                                    source,
                                    injector.provisionListenerStore.get((ProviderInstanceBinding<T>) binding));
                    InternalFactory<? extends T> scopedFactory =
                            Scoping.scope(key, injector, factory, source, scoping);
                    putBinding(
                            new ProviderInstanceBindingImpl<T>(
                                    injector, key, source, scopedFactory, scoping, provider, injectionPoints));
                    return true;
                 }

                 @Override
                 public Boolean visit(ProviderKeyBinding<? extends T> binding) {
                    prepareBinding();
                    Key<? extends javax.inject.Provider<? extends T>> providerKey =
                            binding.getProviderKey();
                    // always visited with Binding<T>
                    @SuppressWarnings("unchecked")
                    BoundProviderFactory<T> boundProviderFactory =
                            new BoundProviderFactory<T>(
                                    injector,
                                    providerKey,
                                    source,
                                    injector.provisionListenerStore.get((ProviderKeyBinding<T>) binding));
                    bindingData.addCreationListener(boundProviderFactory);
                    InternalFactory<? extends T> scopedFactory =
                            Scoping.scope(
                                    key,
                                    injector,
                                    (InternalFactory<? extends T>) boundProviderFactory,
                                    source,
                                    scoping);
                    putBinding(
                            new LinkedProviderBindingImpl<T>(
                                    injector, key, source, scopedFactory, scoping, providerKey));
                    return true;
                 }

                 @Override
                 public Boolean visit(LinkedKeyBinding<? extends T> binding) {
                    prepareBinding();
                    Key<? extends T> linkedKey = binding.getLinkedKey();
                    if (key.equals(linkedKey)) {
                       errors.recursiveBinding();
                    }

                    FactoryProxy<T> factory = new FactoryProxy<>(injector, key, linkedKey, source);
                    bindingData.addCreationListener(factory);
                    InternalFactory<? extends T> scopedFactory =
                            Scoping.scope(key, injector, factory, source, scoping);
                    putBinding(
                            new LinkedBindingImpl<T>(injector, key, source, scopedFactory, scoping, linkedKey));
                    return true;
                 }

                 /** Handle ProviderMethods specially. */
                 private Boolean visitInternalProviderInstanceBindingFactory(
                         InternalProviderInstanceBindingImpl.Factory<T> provider) {
                    InternalProviderInstanceBindingImpl<T> binding =
                            new InternalProviderInstanceBindingImpl<T>(
                                    injector,
                                    key,
                                    source,
                                    provider,
                                    Scoping.scope(key, injector, provider, source, scoping),
                                    scoping);
                    switch (binding.getInitializationTiming()) {
                       case DELAYED:
                          scheduleDelayedInitialization(binding);
                          break;
                       case EAGER:
                          scheduleInitialization(binding);
                          break;
                       default:
                          throw new AssertionError();
                    }
                    putBinding(binding);
                    return true;
                 }

                 @Override
                 public Boolean visit(UntargettedBinding<? extends T> untargetted) {
                    return false;
                 }

                 @Override
                 public Boolean visit(ExposedBinding<? extends T> binding) {
                    throw new IllegalArgumentException("Cannot apply a non-module element");
                 }

                 @Override
                 public Boolean visit(ConvertedConstantBinding<? extends T> binding) {
                    throw new IllegalArgumentException("Cannot apply a non-module element");
                 }

                 @Override
                 public Boolean visit(ProviderBinding<? extends T> binding) {
                    throw new IllegalArgumentException("Cannot apply a non-module element");
                 }

                 @Override
                 protected Boolean visitOther(Binding<? extends T> binding) {
                    throw new IllegalStateException("BindingProcessor should override all visitations");
                 }
              });
   }

   @Override
   public Boolean visit(PrivateElements privateElements) {
      for (Key<?> key : privateElements.getExposedKeys()) {
         bindExposed(privateElements, key);
      }
      return false; // leave the private elements for the PrivateElementsProcessor to handle
   }

   private <T> void bindExposed(PrivateElements privateElements, Key<T> key) {
      ExposedKeyFactory<T> exposedKeyFactory = new ExposedKeyFactory<>(key, privateElements);
      bindingData.addCreationListener(exposedKeyFactory);
      putBinding(
              new ExposedBindingImpl<T>(
                      injector,
                      privateElements.getExposedSource(key),
                      key,
                      exposedKeyFactory,
                      privateElements));
   }
}
