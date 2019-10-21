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

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.Stopwatch;
import com.google.inject.spi.TypeConverterBinding;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds a tree of injectors. This is a primary injector, plus child injectors needed for each
 * {@code Binder.newPrivateBinder() private environment}. The primary injector is not necessarily a
 * top-level injector.
 *
 * <p>Injector construction happens in two phases.
 *
 * <ol>
 * <li>Static building. In this phase, we interpret commands, create bindings, and inspect
 *     dependencies. During this phase, we hold a lock to ensure consistency with parent injectors.
 *     No user code is executed in this phase.
 * <li>Dynamic injection. In this phase, we call user code. We inject members that requested
 *     injection. This may require user's objects be created and their providers be called. And we
 *     create eager singletons. In this phase, user code may have started other threads. This phase
 *     is not executed for injectors created using {@link Stage#TOOL the tool stage}
 * </ol>
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 */
public final class InternalInjectorCreator {

   private final Stopwatch stopwatch = new Stopwatch();
   private final Errors errors = new Errors();

   private final Initializer initializer = new Initializer();
   private final ProcessedBindingData bindingData;
   private final InjectionRequestProcessor injectionRequestProcessor;

   private final InjectorShell.Builder shellBuilder = new InjectorShell.Builder();
   private List<InjectorShell> shells;

   public InternalInjectorCreator() {
      injectionRequestProcessor = new InjectionRequestProcessor(errors, initializer);
      bindingData = new ProcessedBindingData();
   }

   public InternalInjectorCreator stage(Stage stage) {
      shellBuilder.stage(stage);
      return this;
   }

   /**
    * Sets the parent of the injector to-be-constructed. As a side effect, this sets this injector's
    * stage to the stage of {@code parent} and sets {@link #requireExplicitBindings()} if the parent
    * injector also required them.
    */
   public InternalInjectorCreator parentInjector(InjectorImpl parent) {
      shellBuilder.parent(parent);
      return this;
   }

   public InternalInjectorCreator addModules(Iterable<? extends Module> modules) {
      shellBuilder.addModules(modules);
      return this;
   }

   public Injector build() {
      if (shellBuilder == null) {
         throw new AssertionError("Already built, builders are not reusable.");
      }

      // Synchronize while we're building up the bindings and other injector state. This ensures that
      // the JIT bindings in the parent injector don't change while we're being built
      synchronized (shellBuilder.lock()) {
         shells = shellBuilder.build(initializer, bindingData, stopwatch, errors);
         stopwatch.resetAndLog("Injector construction");

         initializeStatically();
      }

      injectDynamically();

      if (shellBuilder.getStage() == Stage.TOOL) {
         // wrap the primaryInjector in a ToolStageInjector
         // to prevent non-tool-friendy methods from being called.
         return new ToolStageInjector(primaryInjector());
      } else {
         return primaryInjector();
      }
   }

   /** Initialize and validate everything. */
   private void initializeStatically() {
      bindingData.initializeBindings();
      stopwatch.resetAndLog("Binding initialization");

      for (InjectorShell shell : shells) {
         shell.getInjector().index();
      }
      stopwatch.resetAndLog("Binding indexing");

      injectionRequestProcessor.process(shells);
      stopwatch.resetAndLog("Collecting injection requests");

      bindingData.runCreationListeners(errors);
      stopwatch.resetAndLog("Binding validation");

      injectionRequestProcessor.validate();
      stopwatch.resetAndLog("Static validation");

      initializer.validateOustandingInjections(errors);
      stopwatch.resetAndLog("Instance member validation");

      new LookupProcessor(errors).process(shells);
      for (InjectorShell shell : shells) {
         ((DeferredLookups) shell.getInjector().lookups).initialize(errors);
      }
      stopwatch.resetAndLog("Provider verification");

      // This needs to come late since some user bindings rely on requireBinding calls to create
      // jit bindings during the LookupProcessor.
      bindingData.initializeDelayedBindings();
      stopwatch.resetAndLog("Delayed Binding initialization");

      for (InjectorShell shell : shells) {
         if (!shell.getElements().isEmpty()) {
            throw new AssertionError("Failed to execute " + shell.getElements());
         }
      }

      errors.throwCreationExceptionIfErrorsExist();
   }

   /** Returns the injector being constructed. This is not necessarily the root injector. */
   private Injector primaryInjector() {
      return shells.get(0).getInjector();
   }

   /**
    * Inject everything that can be injected. This method is intentionally not synchronized. If we
    * locked while injecting members (ie. running user code), things would deadlock should the user
    * code build a just-in-time binding from another thread.
    */
   private void injectDynamically() {
      injectionRequestProcessor.injectMembers();
      stopwatch.resetAndLog("Static member injection");

      initializer.injectAll(errors);
      stopwatch.resetAndLog("Instance injection");
      errors.throwCreationExceptionIfErrorsExist();

      if (shellBuilder.getStage() != Stage.TOOL) {
         for (InjectorShell shell : shells) {
            loadEagerSingletons(shell.getInjector(), shellBuilder.getStage(), errors);
         }
         stopwatch.resetAndLog("Preloading singletons");
      }
      errors.throwCreationExceptionIfErrorsExist();
   }

   /**
    * Loads eager singletons, or all singletons if we're in Stage.PRODUCTION. Bindings discovered
    * while we're binding these singletons are not be eager.
    */
   void loadEagerSingletons(InjectorImpl injector, Stage stage, final Errors errors) {
      List<BindingImpl<?>> candidateBindings = new ArrayList<>();
      @SuppressWarnings("unchecked") // casting Collection<Binding> to Collection<BindingImpl> is safe
              Collection<BindingImpl<?>> bindingsAtThisLevel =
              (Collection) injector.state.getExplicitBindingsThisLevel().values();
      candidateBindings.addAll(bindingsAtThisLevel);
      synchronized (injector.state.lock()) {
         // jit bindings must be accessed while holding the lock.
         candidateBindings.addAll(injector.jitBindings.values());
      }
      InternalContext context = injector.enterContext();
      try {
         for (BindingImpl<?> binding : candidateBindings) {
            if (isEagerSingleton(injector, binding, stage)) {
               com.google.inject.spi.Dependency<?> dependency = com.google.inject.spi.Dependency.get(binding.getKey());
               com.google.inject.spi.Dependency previous = context.pushDependency(dependency, binding.getSource());

               try {
                  binding.getInternalFactory().get(context, dependency, false);
               } catch (InternalProvisionException e) {
                  errors.withSource(dependency).merge(e);
               } finally {
                  context.popStateAndSetDependency(previous);
               }
            }
         }
      } finally {
         context.close();
      }
   }

   private boolean isEagerSingleton(InjectorImpl injector, BindingImpl<?> binding, Stage stage) {
      if (binding.getScoping().isEagerSingleton(stage)) {
         return true;
      }

      // handle a corner case where a child injector links to a binding in a parent injector, and
      // that binding is singleton. We won't catch this otherwise because we only iterate the child's
      // bindings.
      if (binding instanceof LinkedBindingImpl) {
         Key<?> linkedBinding = ((LinkedBindingImpl<?>) binding).getLinkedKey();
         return isEagerSingleton(injector, injector.getBinding(linkedBinding), stage);
      }

      return false;
   }

   /** {@link Injector} exposed to users in {@link Stage#TOOL}. */
   static class ToolStageInjector implements Injector {
      private final Injector delegateInjector;

      ToolStageInjector(Injector delegateInjector) {
         this.delegateInjector = delegateInjector;
      }

      @Override
      public void injectMembers(Object o) {
         throw new UnsupportedOperationException(
                 "Injector.injectMembers(Object) is not supported in Stage.TOOL");
      }

      @Override
      public Map<Key<?>, Binding<?>> getBindings() {
         return this.delegateInjector.getBindings();
      }

      @Override
      public Map<Key<?>, Binding<?>> getAllBindings() {
         return this.delegateInjector.getAllBindings();
      }

      @Override
      public <T> Binding<T> getBinding(Key<T> key) {
         return this.delegateInjector.getBinding(key);
      }

      @Override
      public <T> Binding<T> getBinding(Class<T> type) {
         return this.delegateInjector.getBinding(type);
      }

      @Override
      public <T> Binding<T> getExistingBinding(Key<T> key) {
         return this.delegateInjector.getExistingBinding(key);
      }

      @Override
      public <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> type) {
         return this.delegateInjector.findBindingsByType(type);
      }

      @Override
      public Injector getParent() {
         return delegateInjector.getParent();
      }

      @Override
      public Injector createChildInjector(Iterable<? extends Module> modules) {
         return delegateInjector.createChildInjector(modules);
      }

      @Override
      public Injector createChildInjector(Module... modules) {
         return delegateInjector.createChildInjector(modules);
      }

      @Override
      public Map<Class<? extends Annotation>, Scope> getScopeBindings() {
         return delegateInjector.getScopeBindings();
      }

      @Override
      public Set<TypeConverterBinding> getTypeConverterBindings() {
         return delegateInjector.getTypeConverterBindings();
      }

      @Override
      public <T> Provider<T> getProvider(Key<T> key) {
         throw new UnsupportedOperationException(
                 "Injector.getProvider(Key<T>) is not supported in Stage.TOOL");
      }

      @Override
      public <T> Provider<T> getProvider(Class<T> type) {
         throw new UnsupportedOperationException(
                 "Injector.getProvider(Class<T>) is not supported in Stage.TOOL");
      }

      @Override
      public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
         throw new UnsupportedOperationException(
                 "Injector.getMembersInjector(TypeLiteral<T>) is not supported in Stage.TOOL");
      }

      @Override
      public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
         throw new UnsupportedOperationException(
                 "Injector.getMembersInjector(Class<T>) is not supported in Stage.TOOL");
      }

      @Override
      public <T> T getInstance(Key<T> key) {
         throw new UnsupportedOperationException(
                 "Injector.getInstance(Key<T>) is not supported in Stage.TOOL");
      }

      @Override
      public <T> T getInstance(Class<T> type) {
         throw new UnsupportedOperationException(
                 "Injector.getInstance(Class<T>) is not supported in Stage.TOOL");
      }
   }
}
