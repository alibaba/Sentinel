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

package com.google.inject.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProviderWithDependencies;
import java.util.Set;

/**
 * Static utility methods for creating and working with instances of {@link Provider}.
 *
 * @author Kevin Bourrillion (kevinb9n@gmail.com)
 * @since 2.0
 */
public final class Providers {

   private Providers() {}

   /**
    * Returns a provider which always provides {@code instance}. This should not be necessary to use
    * in your application, but is helpful for several types of unit tests.
    *
    * @param instance the instance that should always be provided. This is also permitted to be null,
    *     to enable aggressive testing, although in real life a Guice-supplied Provider will never
    *     return null.
    */
   public static <T> Provider<T> of(final T instance) {
      return new ConstantProvider<T>(instance);
   }

   private static final class ConstantProvider<T> implements Provider<T> {
      private final T instance;

      private ConstantProvider(T instance) {
         this.instance = instance;
      }

      @Override
      public T get() {
         return instance;
      }

      @Override
      public String toString() {
         return "of(" + instance + ")";
      }

      @Override
      public boolean equals(Object obj) {
         return (obj instanceof ConstantProvider)
                 && Objects.equal(instance, ((ConstantProvider<?>) obj).instance);
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(instance);
      }
   }

   /**
    * Returns a Guice-friendly {@code com.google.inject.Provider} for the given JSR-330 {@code
    * javax.inject.Provider}. The converse method is unnecessary, since Guice providers directly
    * implement the JSR-330 interface.
    *
    * @since 3.0
    */
   public static <T> Provider<T> guicify(javax.inject.Provider<T> provider) {
      if (provider instanceof Provider) {
         return (Provider<T>) provider;
      }

      final javax.inject.Provider<T> delegate = checkNotNull(provider, "provider");

      // Ensure that we inject all injection points from the delegate provider.
      Set<InjectionPoint> injectionPoints =
              InjectionPoint.forInstanceMethodsAndFields(provider.getClass());
      if (injectionPoints.isEmpty()) {
         return new GuicifiedProvider<T>(delegate);
      } else {
         Set<Dependency<?>> mutableDeps = Sets.newHashSet();
         for (InjectionPoint ip : injectionPoints) {
            mutableDeps.addAll(ip.getDependencies());
         }
         final Set<Dependency<?>> dependencies = ImmutableSet.copyOf(mutableDeps);
         return new GuicifiedProviderWithDependencies<T>(dependencies, delegate);
      }
   }

   private static class GuicifiedProvider<T> implements Provider<T> {
      protected final javax.inject.Provider<T> delegate;

      private GuicifiedProvider(javax.inject.Provider<T> delegate) {
         this.delegate = delegate;
      }

      @Override
      public T get() {
         return delegate.get();
      }

      @Override
      public String toString() {
         return "guicified(" + delegate + ")";
      }

      @Override
      public boolean equals(Object obj) {
         return (obj instanceof GuicifiedProvider)
                 && Objects.equal(delegate, ((GuicifiedProvider<?>) obj).delegate);
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(delegate);
      }
   }

   private static final class GuicifiedProviderWithDependencies<T> extends GuicifiedProvider<T>
           implements ProviderWithDependencies<T> {
      private final Set<Dependency<?>> dependencies;

      private GuicifiedProviderWithDependencies(
              Set<Dependency<?>> dependencies, javax.inject.Provider<T> delegate) {
         super(delegate);
         this.dependencies = dependencies;
      }

      @SuppressWarnings("unused")
      @Inject
      void initialize(Injector injector) {
         injector.injectMembers(delegate);
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
         return dependencies;
      }
   }
}
