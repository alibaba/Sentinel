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

import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.ProvisionListenerStackCallback.ProvisionCallback;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Creates instances using an injectable constructor. After construction, all injectable fields and
 * methods are injected.
 *
 * @author crazybob@google.com (Bob Lee)
 */
final class ConstructorInjector<T> {

   private final ImmutableSet<InjectionPoint> injectableMembers;
   private final SingleParameterInjector<?>[] parameterInjectors;
   private final ConstructionProxy<T> constructionProxy;
   private final MembersInjectorImpl<T> membersInjector;

   ConstructorInjector(
           Set<InjectionPoint> injectableMembers,
           ConstructionProxy<T> constructionProxy,
           SingleParameterInjector<?>[] parameterInjectors,
           MembersInjectorImpl<T> membersInjector) {
      this.injectableMembers = ImmutableSet.copyOf(injectableMembers);
      this.constructionProxy = constructionProxy;
      this.parameterInjectors = parameterInjectors;
      this.membersInjector = membersInjector;
   }

   public ImmutableSet<InjectionPoint> getInjectableMembers() {
      return injectableMembers;
   }

   ConstructionProxy<T> getConstructionProxy() {
      return constructionProxy;
   }

   /**
    * Construct an instance. Returns {@code Object} instead of {@code T} because it may return a
    * proxy.
    */
   Object construct(
           final InternalContext context,
           Dependency<?> dependency,
           /* @Nullable */ ProvisionListenerStackCallback<T> provisionCallback)
           throws InternalProvisionException {
      final ConstructionContext<T> constructionContext = context.getConstructionContext(this);
      // We have a circular reference between constructors. Return a proxy.
      if (constructionContext.isConstructing()) {
         // TODO (crazybob): if we can't proxy this object, can we proxy the other object?
         return constructionContext.createProxy(
                 context.getInjectorOptions(), dependency.getKey().getTypeLiteral().getRawType());
      }

      // If we're re-entering this factory while injecting fields or methods,
      // return the same instance. This prevents infinite loops.
      T t = constructionContext.getCurrentReference();
      if (t != null) {
         if (context.getInjectorOptions().disableCircularProxies) {
            throw InternalProvisionException.circularDependenciesDisabled(
                    dependency.getKey().getTypeLiteral().getRawType());
         } else {
            return t;
         }
      }

      constructionContext.startConstruction();
      try {
         // Optimization: Don't go through the callback stack if we have no listeners.
         if (provisionCallback == null) {
            return provision(context, constructionContext);
         } else {
            return provisionCallback.provision(
                    context,
                    new ProvisionCallback<T>() {
                       @Override
                       public T call() throws InternalProvisionException {
                          return provision(context, constructionContext);
                       }
                    });
         }
      } finally {
         constructionContext.finishConstruction();
      }
   }

   /** Provisions a new T. */
   private T provision(InternalContext context, ConstructionContext<T> constructionContext)
           throws InternalProvisionException {
      try {
         T t;
         try {
            Object[] parameters = SingleParameterInjector.getAll(context, parameterInjectors);
            t = constructionProxy.newInstance(parameters);
            constructionContext.setProxyDelegates(t);
         } finally {
            constructionContext.finishConstruction();
         }

         // Store reference. If an injector re-enters this factory, they'll get the same reference.
         constructionContext.setCurrentReference(t);

         MembersInjectorImpl<T> localMembersInjector = membersInjector;
         localMembersInjector.injectMembers(t, context, false);
         localMembersInjector.notifyListeners(t);

         return t;
      } catch (InvocationTargetException userException) {
         Throwable cause = userException.getCause() != null ? userException.getCause() : userException;
         throw InternalProvisionException.errorInjectingConstructor(cause)
                 .addSource(constructionProxy.getInjectionPoint());
      } finally {
         constructionContext.removeCurrentReference();
      }
   }
}
