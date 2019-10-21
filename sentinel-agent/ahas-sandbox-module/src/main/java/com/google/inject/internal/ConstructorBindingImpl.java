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

import static com.google.common.base.Preconditions.checkState;
import static com.google.inject.internal.Annotations.findScopeAnnotation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.Classes;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;

final class ConstructorBindingImpl<T> extends com.google.inject.internal.BindingImpl<T>
        implements ConstructorBinding<T>, DelayedInitialize {

   private final Factory<T> factory;
   private final InjectionPoint constructorInjectionPoint;

   private ConstructorBindingImpl(
           InjectorImpl injector,
           Key<T> key,
           Object source,
           InternalFactory<? extends T> scopedFactory,
           Scoping scoping,
           Factory<T> factory,
           InjectionPoint constructorInjectionPoint) {
      super(injector, key, source, scopedFactory, scoping);
      this.factory = factory;
      this.constructorInjectionPoint = constructorInjectionPoint;
   }

   public ConstructorBindingImpl(
           Key<T> key,
           Object source,
           Scoping scoping,
           InjectionPoint constructorInjectionPoint,
           Set<InjectionPoint> injectionPoints) {
      super(source, key, scoping);
      this.factory = new Factory<>(false, key);
      ConstructionProxy<T> constructionProxy =
              new DefaultConstructionProxyFactory<T>(constructorInjectionPoint).create();
      this.constructorInjectionPoint = constructorInjectionPoint;
      factory.constructorInjector =
              new ConstructorInjector<T>(injectionPoints, constructionProxy, null, null);
   }

   /**
    * @param constructorInjector the constructor to use, or {@code null} to use the default.
    * @param failIfNotLinked true if this ConstructorBindingImpl's InternalFactory should only
    *     succeed if retrieved from a linked binding
    */
   static <T> ConstructorBindingImpl<T> create(
           InjectorImpl injector,
           Key<T> key,
           InjectionPoint constructorInjector,
           Object source,
           Scoping scoping,
           Errors errors,
           boolean failIfNotLinked,
           boolean failIfNotExplicit)
           throws ErrorsException {
      int numErrors = errors.size();

      @SuppressWarnings("unchecked") // constructorBinding guarantees type is consistent
              Class<? super T> rawType =
              constructorInjector == null
                      ? key.getTypeLiteral().getRawType()
                      : (Class) constructorInjector.getDeclaringType().getRawType();

      // We can't inject abstract classes.
      if (Modifier.isAbstract(rawType.getModifiers())) {
         errors.missingImplementationWithHint(key, injector);
      }

      // Error: Inner class.
      if (Classes.isInnerClass(rawType)) {
         errors.cannotInjectInnerClass(rawType);
      }

      errors.throwIfNewErrors(numErrors);

      // Find a constructor annotated @Inject
      if (constructorInjector == null) {
         try {
            constructorInjector = InjectionPoint.forConstructorOf(key.getTypeLiteral());
            if (failIfNotExplicit && !hasAtInject((Constructor) constructorInjector.getMember())) {
               errors.atInjectRequired(rawType);
            }
         } catch (ConfigurationException e) {
            throw errors.merge(e.getErrorMessages()).toException();
         }
      }

      // if no scope is specified, look for a scoping annotation on the concrete class
      if (!scoping.isExplicitlyScoped()) {
         Class<?> annotatedType = constructorInjector.getMember().getDeclaringClass();
         Class<? extends Annotation> scopeAnnotation = findScopeAnnotation(errors, annotatedType);
         if (scopeAnnotation != null) {
            scoping =
                    Scoping.makeInjectable(
                            Scoping.forAnnotation(scopeAnnotation), injector, errors.withSource(rawType));
         }
      }

      errors.throwIfNewErrors(numErrors);

      Factory<T> factoryFactory = new Factory<>(failIfNotLinked, key);
      InternalFactory<? extends T> scopedFactory =
              Scoping.scope(key, injector, factoryFactory, source, scoping);

      return new ConstructorBindingImpl<T>(
              injector, key, source, scopedFactory, scoping, factoryFactory, constructorInjector);
   }

   /** Returns true if the inject annotation is on the constructor. */
   private static boolean hasAtInject(Constructor cxtor) {
      return cxtor.isAnnotationPresent(Inject.class)
              || cxtor.isAnnotationPresent(javax.inject.Inject.class);
   }

   @Override
   @SuppressWarnings("unchecked") // the result type always agrees with the ConstructorInjector type
   public void initialize(InjectorImpl injector, Errors errors) throws ErrorsException {
      factory.constructorInjector =
              (ConstructorInjector<T>) injector.constructors.get(constructorInjectionPoint, errors);
      factory.provisionCallback = injector.provisionListenerStore.get(this);
   }

   /** True if this binding has been initialized and is ready for use. */
   boolean isInitialized() {
      return factory.constructorInjector != null;
   }

   /** Returns an injection point that can be used to clean up the constructor store. */
   InjectionPoint getInternalConstructor() {
      if (factory.constructorInjector != null) {
         return factory.constructorInjector.getConstructionProxy().getInjectionPoint();
      } else {
         return constructorInjectionPoint;
      }
   }

   /** Returns a set of dependencies that can be iterated over to clean up stray JIT bindings. */
   Set<Dependency<?>> getInternalDependencies() {
      ImmutableSet.Builder<InjectionPoint> builder = ImmutableSet.builder();
      if (factory.constructorInjector == null) {
         builder.add(constructorInjectionPoint);
         // If the below throws, it's OK -- we just ignore those dependencies, because no one
         // could have used them anyway.
         try {
            builder.addAll(
                    InjectionPoint.forInstanceMethodsAndFields(
                            constructorInjectionPoint.getDeclaringType()));
         } catch (ConfigurationException ignored) {
         }
      } else {
         builder.add(getConstructor()).addAll(getInjectableMembers());
      }

      return Dependency.forInjectionPoints(builder.build());
   }

   @Override
   public <V> V acceptTargetVisitor(BindingTargetVisitor<? super T, V> visitor) {
      checkState(factory.constructorInjector != null, "not initialized");
      return visitor.visit(this);
   }

   @Override
   public InjectionPoint getConstructor() {
      checkState(factory.constructorInjector != null, "Binding is not ready");
      return factory.constructorInjector.getConstructionProxy().getInjectionPoint();
   }

   @Override
   public Set<InjectionPoint> getInjectableMembers() {
      checkState(factory.constructorInjector != null, "Binding is not ready");
      return factory.constructorInjector.getInjectableMembers();
   }

   /*if[AOP]*/
//   @Override
//   public Map<Method, List<org.aopalliance.intercept.MethodInterceptor>> getMethodInterceptors() {
//      checkState(factory.constructorInjector != null, "Binding is not ready");
//      return factory.constructorInjector.getConstructionProxy().getMethodInterceptors();
//   }
   /*end[AOP]*/

   @Override
   public Set<Dependency<?>> getDependencies() {
      return Dependency.forInjectionPoints(
              new ImmutableSet.Builder<InjectionPoint>()
                      .add(getConstructor())
                      .addAll(getInjectableMembers())
                      .build());
   }

   @Override
   protected BindingImpl<T> withScoping(Scoping scoping) {
      return new ConstructorBindingImpl<T>(
              null, getKey(), getSource(), factory, scoping, factory, constructorInjectionPoint);
   }

   @Override
   protected BindingImpl<T> withKey(Key<T> key) {
      return new ConstructorBindingImpl<T>(
              null, key, getSource(), factory, getScoping(), factory, constructorInjectionPoint);
   }

   @Override
   @SuppressWarnings("unchecked") // the raw constructor member and declaring type always agree
   public void applyTo(Binder binder) {
      InjectionPoint constructor = getConstructor();
      getScoping()
              .applyTo(
                      binder
                              .withSource(getSource())
                              .bind(getKey())
                              .toConstructor(
                                      (Constructor) getConstructor().getMember(),
                                      (TypeLiteral) constructor.getDeclaringType()));
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(ConstructorBinding.class)
              .add("key", getKey())
              .add("source", getSource())
              .add("scope", getScoping())
              .toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ConstructorBindingImpl) {
         ConstructorBindingImpl<?> o = (ConstructorBindingImpl<?>) obj;
         return getKey().equals(o.getKey())
                 && getScoping().equals(o.getScoping())
                 && Objects.equal(constructorInjectionPoint, o.constructorInjectionPoint);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(getKey(), getScoping(), constructorInjectionPoint);
   }

   private static class Factory<T> implements InternalFactory<T> {
      private final boolean failIfNotLinked;
      private final Key<?> key;
      private ConstructorInjector<T> constructorInjector;
      private ProvisionListenerStackCallback<T> provisionCallback;

      Factory(boolean failIfNotLinked, Key<?> key) {
         this.failIfNotLinked = failIfNotLinked;
         this.key = key;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get(InternalContext context, Dependency<?> dependency, boolean linked)
              throws InternalProvisionException {
         ConstructorInjector<T> localInjector = constructorInjector;
         if (localInjector == null) {
            throw new IllegalStateException("Constructor not ready");
         }

         if (!linked && failIfNotLinked) {
            throw InternalProvisionException.jitDisabled(key);
         }

         // This may not actually be safe because it could return a super type of T (if that's all the
         // client needs), but it should be OK in practice thanks to the wonders of erasure.
         return (T) localInjector.construct(context, dependency, provisionCallback);
      }
   }
}
