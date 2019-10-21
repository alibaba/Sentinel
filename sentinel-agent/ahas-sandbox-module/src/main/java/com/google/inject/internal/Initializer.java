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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.CycleDetectingLock.CycleDetectingLockFactory;
import com.google.inject.spi.InjectionPoint;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Manages and injects instances at injector-creation time. This is made more complicated by
 * instances that request other instances while they're being injected. We overcome this by using
 * {@link Initializable}, which attempts to perform injection before use.
 *
 * @author jessewilson@google.com (Jesse Wilson)
 */
final class Initializer {

   /** Is set to true once {@link #validateOustandingInjections} is called. */
   private volatile boolean validationStarted = false;

   /**
    * Allows us to detect circular dependencies. It's only used during injectable reference
    * initialization. After initialization direct access through volatile field is used.
    */
   private final CycleDetectingLockFactory<Class<?>> cycleDetectingLockFactory =
           new CycleDetectingLockFactory<Class<?>>();

   /**
    * Instances that need injection during injector creation to a source that registered them. New
    * references added before {@link #validateOustandingInjections}. Cleared up in {@link
    * #injectAll}.
    */
   private final List<InjectableReference<?>> pendingInjections = Lists.newArrayList();

   /**
    * Map that guarantees that no instance would get two references. New references added before
    * {@link #validateOustandingInjections}. Cleared up in {@link #validateOustandingInjections}.
    */
   private final IdentityHashMap<Object, InjectableReference<?>> initializablesCache =
           Maps.newIdentityHashMap();

   /**
    * Registers an instance for member injection when that step is performed.
    *
    * @param instance an instance that optionally has members to be injected (each annotated
    *     with @Inject).
    * @param binding the binding that caused this initializable to be created, if it exists.
    * @param source the source location that this injection was requested
    */
   <T> Initializable<T> requestInjection(
           InjectorImpl injector,
           T instance,
           Binding<T> binding,
           Object source,
           Set<InjectionPoint> injectionPoints) {
      checkNotNull(source);
      Preconditions.checkState(
              !validationStarted, "Member injection could not be requested after validation is started");
      ProvisionListenerStackCallback<T> provisionCallback =
              binding == null ? null : injector.provisionListenerStore.get(binding);

      // short circuit if the object has no injections or listeners.
      if (instance == null
              || (injectionPoints.isEmpty()
              && !injector.membersInjectorStore.hasTypeListeners()
              && provisionCallback == null)) {
         return Initializables.of(instance);
      }

      if (initializablesCache.containsKey(instance)) {
         @SuppressWarnings("unchecked") // Map from T to InjectableReference<T>
                 Initializable<T> cached = (Initializable<T>) initializablesCache.get(instance);
         return cached;
      }

      InjectableReference<T> injectableReference =
              new InjectableReference<T>(
                      injector,
                      instance,
                      binding == null ? null : binding.getKey(),
                      provisionCallback,
                      source,
                      cycleDetectingLockFactory.create(instance.getClass()));
      initializablesCache.put(instance, injectableReference);
      pendingInjections.add(injectableReference);
      return injectableReference;
   }

   /**
    * Prepares member injectors for all injected instances. This prompts Guice to do static analysis
    * on the injected instances.
    */
   void validateOustandingInjections(Errors errors) {
      validationStarted = true;
      initializablesCache.clear();
      for (InjectableReference<?> reference : pendingInjections) {
         try {
            reference.validate(errors);
         } catch (ErrorsException e) {
            errors.merge(e.getErrors());
         }
      }
   }

   /**
    * Performs creation-time injections on all objects that require it. Whenever fulfilling an
    * injection depends on another object that requires injection, we inject it first. If the two
    * instances are codependent (directly or transitively), ordering of injection is arbitrary.
    */
   void injectAll(final Errors errors) {
      Preconditions.checkState(validationStarted, "Validation should be done before injection");
      for (InjectableReference<?> reference : pendingInjections) {
         try {
            reference.get();
         } catch (InternalProvisionException ipe) {
            errors.merge(ipe);
         }
      }
      pendingInjections.clear();
   }

   private enum InjectableReferenceState {
      NEW,
      VALIDATED,
      INJECTING,
      READY
   }

   private static class InjectableReference<T> implements Initializable<T> {
      private volatile InjectableReferenceState state = InjectableReferenceState.NEW;
      private volatile MembersInjectorImpl<T> membersInjector = null;

      private final InjectorImpl injector;
      private final T instance;
      private final Object source;
      private final Key<T> key;
      private final ProvisionListenerStackCallback<T> provisionCallback;
      private final CycleDetectingLock<?> lock;

      public InjectableReference(
              InjectorImpl injector,
              T instance,
              Key<T> key,
              ProvisionListenerStackCallback<T> provisionCallback,
              Object source,
              CycleDetectingLock<?> lock) {
         this.injector = injector;
         this.key = key; // possibly null!
         this.provisionCallback = provisionCallback; // possibly null!
         this.instance = checkNotNull(instance, "instance");
         this.source = checkNotNull(source, "source");
         this.lock = checkNotNull(lock, "lock");
      }

      public void validate(Errors errors) throws ErrorsException {
         @SuppressWarnings("unchecked") // the type of 'T' is a TypeLiteral<T>
                 TypeLiteral<T> type = TypeLiteral.get((Class<T>) instance.getClass());
         membersInjector = injector.membersInjectorStore.get(type, errors.withSource(source));
         Preconditions.checkNotNull(
                 membersInjector,
                 "No membersInjector available for instance: %s, from key: %s",
                 instance,
                 key);
         state = InjectableReferenceState.VALIDATED;
      }

      /**
       * Reentrant. If {@code instance} was registered for injection at injector-creation time, this
       * method will ensure that all its members have been injected before returning.
       */
      @Override
      public T get() throws InternalProvisionException {
         // skipping acquiring lock if initialization is already finished
         if (state == InjectableReferenceState.READY) {
            return instance;
         }

         // acquire lock for current binding to initialize an instance
         Multimap<?, ?> lockCycle = lock.lockOrDetectPotentialLocksCycle();
         if (!lockCycle.isEmpty()) {
            // Potential deadlock detected and creation lock is not taken.
            // According to injectAll()'s contract return non-initialized instance.

            // This condition should not be possible under the current Guice implementation.
            // This clause exists for defensive programming purposes.

            // Reasoning:
            // get() is called either directly from injectAll(), holds no locks and can not create
            // a cycle, or it is called through a singleton scope, which resolves deadlocks by itself.
            // Before calling get() object has to be requested for injection.
            // Initializer.requestInjection() is called either for constant object bindings, which wrap
            // creation into a Singleton scope, or from Binder.requestInjection(), which
            // has to use Singleton scope to reuse the same InjectableReference to potentially
            // create a lock cycle.
            return instance;
         }
         try {
            // lock acquired, current thread owns this instance initialization
            switch (state) {
               case READY:
                  return instance;
               // When instance depends on itself in the same thread potential dead lock
               // is not detected. We have to prevent a stack overflow and we use
               // an "injecting" stage to short-circuit a call.
               case INJECTING:
                  return instance;
               case VALIDATED:
                  state = InjectableReferenceState.INJECTING;
                  break;
               case NEW:
                  throw new IllegalStateException("InjectableReference is not validated yet");
               default:
                  throw new IllegalStateException("Unknown state: " + state);
            }

            // if in Stage.TOOL, we only want to inject & notify toolable injection points.
            // (otherwise we'll inject all of them)
            try {
               membersInjector.injectAndNotify(
                       instance, key, provisionCallback, source, injector.options.stage == Stage.TOOL);
            } catch (InternalProvisionException ipe) {
               throw ipe.addSource(source);
            }
            // mark instance as ready to skip a lock on subsequent calls
            state = InjectableReferenceState.READY;
            return instance;
         } finally {
            // always release our creation lock, even on failures
            lock.unlock();
         }
      }

      @Override
      public String toString() {
         return instance.toString();
      }
   }
}
