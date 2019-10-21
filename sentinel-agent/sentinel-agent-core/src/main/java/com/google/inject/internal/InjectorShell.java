package com.google.inject.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.internal.util.SourceProvider;
import com.google.inject.internal.util.Stopwatch;
import com.google.inject.spi.*;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

final class InjectorShell {
   private final List<com.google.inject.spi.Element> elements;
   private final InjectorImpl injector;

   private InjectorShell(List<com.google.inject.spi.Element> elements, InjectorImpl injector) {
      this.elements = elements;
      this.injector = injector;
   }

   InjectorImpl getInjector() {
      return this.injector;
   }

   List<com.google.inject.spi.Element> getElements() {
      return this.elements;
   }

   private static void bindInjector(InjectorImpl injector) {
      Key<Injector> key = Key.get(Injector.class);
      InjectorFactory injectorFactory = new InjectorFactory(injector);
      injector.state.putBinding(key, new ProviderInstanceBindingImpl(injector, key, SourceProvider.UNKNOWN_SOURCE, injectorFactory, Scoping.UNSCOPED, injectorFactory, ImmutableSet.of()));
   }

   private static void bindLogger(InjectorImpl injector) {
      Key<Logger> key = Key.get(Logger.class);
      LoggerFactory loggerFactory = new LoggerFactory();
      injector.state.putBinding(key, new ProviderInstanceBindingImpl(injector, key, SourceProvider.UNKNOWN_SOURCE, loggerFactory, Scoping.UNSCOPED, loggerFactory, ImmutableSet.of()));
   }

   private static void bindStage(InjectorImpl injector, Stage stage) {
      Key<Stage> key = Key.get(Stage.class);
      InstanceBindingImpl<Stage> stageBinding = new InstanceBindingImpl(injector, key, SourceProvider.UNKNOWN_SOURCE, new ConstantFactory(Initializables.of(stage)), ImmutableSet.of(), stage);
      injector.state.putBinding(key, stageBinding);
   }

   // $FF: synthetic method
   InjectorShell(List x0, InjectorImpl x1, Object x2) {
      this(x0, x1);
   }

   private static class InheritedScannersModule implements Module {
      private final State state;

      InheritedScannersModule(State state) {
         this.state = state;
      }

      public void configure(Binder binder) {
         Iterator i$ = this.state.getScannerBindings().iterator();

         while(i$.hasNext()) {
            ModuleAnnotatedMethodScannerBinding binding = (ModuleAnnotatedMethodScannerBinding)i$.next();
            binding.applyTo(binder);
         }

      }
   }

   private static class RootModule implements Module {
      private RootModule() {
      }

      public void configure(Binder binder) {
         binder = binder.withSource(SourceProvider.UNKNOWN_SOURCE);
         binder.bindScope(Singleton.class, Scopes.SINGLETON);
         binder.bindScope(javax.inject.Singleton.class, Scopes.SINGLETON);
      }

      // $FF: synthetic method
      RootModule(Object x0) {
         this();
      }
   }

   private static class LoggerFactory implements InternalFactory<Logger>, Provider<Logger> {
      private LoggerFactory() {
      }

      public Logger get(InternalContext context, Dependency<?> dependency, boolean linked) {
         InjectionPoint injectionPoint = dependency.getInjectionPoint();
         return injectionPoint == null ? Logger.getAnonymousLogger() : Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
      }

      public Logger get() {
         return Logger.getAnonymousLogger();
      }

      public String toString() {
         return "Provider<Logger>";
      }

      // $FF: synthetic method
      LoggerFactory(Object x0) {
         this();
      }
   }

   private static class InjectorFactory implements InternalFactory<Injector>, Provider<Injector> {
      private final Injector injector;

      private InjectorFactory(Injector injector) {
         this.injector = injector;
      }

      public Injector get(InternalContext context, Dependency<?> dependency, boolean linked) {
         return this.injector;
      }

      public Injector get() {
         return this.injector;
      }

      public String toString() {
         return "Provider<Injector>";
      }

      // $FF: synthetic method
      InjectorFactory(Injector x0, Object x1) {
         this(x0);
      }
   }

   static class Builder {
      private final List<com.google.inject.spi.Element> elements = Lists.newArrayList();
      private final List<Module> modules = Lists.newArrayList();
      private State state;
      private InjectorImpl parent;
      private InjectorImpl.InjectorOptions options;
      private Stage stage;
      private PrivateElementsImpl privateElements;

      Builder stage(Stage stage) {
         this.stage = stage;
         return this;
      }

      Builder parent(InjectorImpl parent) {
         this.parent = parent;
         this.state = new InheritingState(parent.state);
         this.options = parent.options;
         this.stage = this.options.stage;
         return this;
      }

      Builder privateElements(PrivateElements privateElements) {
         this.privateElements = (PrivateElementsImpl)privateElements;
         this.elements.addAll(privateElements.getElements());
         return this;
      }

      void addModules(Iterable<? extends Module> modules) {
         Iterator i$ = modules.iterator();

         while(i$.hasNext()) {
            Module module = (Module)i$.next();
            this.modules.add(module);
         }

      }

      Stage getStage() {
         return this.options.stage;
      }

      Object lock() {
         return this.getState().lock();
      }

      List<InjectorShell> build(Initializer initializer, ProcessedBindingData bindingData, Stopwatch stopwatch, Errors errors) {
         Preconditions.checkState(this.stage != null, "Stage not initialized");
         Preconditions.checkState(this.privateElements == null || this.parent != null, "PrivateElements with no parent");
         Preconditions.checkState(this.state != null, "no state. Did you remember to lock() ?");
         if (this.parent == null) {
            this.modules.add(0, new RootModule());
         } else {
            this.modules.add(0, new InheritedScannersModule(this.parent.state));
         }

         this.elements.addAll(Elements.getElements(this.stage, (Iterable)this.modules));
         InjectorOptionsProcessor optionsProcessor = new InjectorOptionsProcessor(errors);
         optionsProcessor.process((InjectorImpl)null, this.elements);
         this.options = optionsProcessor.getOptions(this.stage, this.options);
         InjectorImpl injector = new InjectorImpl(this.parent, this.state, this.options);
         if (this.privateElements != null) {
            this.privateElements.initInjector(injector);
         }

         if (this.parent == null) {
            TypeConverterBindingProcessor.prepareBuiltInConverters(injector);
         }

         stopwatch.resetAndLog("Module execution");
         (new MessageProcessor(errors)).process(injector, this.elements);
         (new ListenerBindingProcessor(errors)).process(injector, this.elements);
         List<TypeListenerBinding> typeListenerBindings = injector.state.getTypeListenerBindings();
         injector.membersInjectorStore = new MembersInjectorStore(injector, typeListenerBindings);
         List<ProvisionListenerBinding> provisionListenerBindings = injector.state.getProvisionListenerBindings();
         injector.provisionListenerStore = new ProvisionListenerCallbackStore(provisionListenerBindings);
         stopwatch.resetAndLog("TypeListeners & ProvisionListener creation");
         (new ScopeBindingProcessor(errors)).process(injector, this.elements);
         stopwatch.resetAndLog("Scopes creation");
         (new TypeConverterBindingProcessor(errors)).process(injector, this.elements);
         stopwatch.resetAndLog("Converters creation");
         InjectorShell.bindStage(injector, this.stage);
         InjectorShell.bindInjector(injector);
         InjectorShell.bindLogger(injector);
         (new BindingProcessor(errors, initializer, bindingData)).process(injector, this.elements);
         (new UntargettedBindingProcessor(errors, bindingData)).process(injector, this.elements);
         stopwatch.resetAndLog("Binding creation");
         (new ModuleAnnotatedMethodScannerProcessor(errors)).process(injector, this.elements);
         stopwatch.resetAndLog("Module annotated method scanners creation");
         List<InjectorShell> injectorShells = Lists.newArrayList();
         injectorShells.add(new InjectorShell(this.elements, injector));
         PrivateElementProcessor processor = new PrivateElementProcessor(errors);
         processor.process(injector, this.elements);
         Iterator i$ = processor.getInjectorShellBuilders().iterator();

         while(i$.hasNext()) {
            Builder builder = (Builder)i$.next();
            injectorShells.addAll(builder.build(initializer, bindingData, stopwatch, errors));
         }

         stopwatch.resetAndLog("Private environment creation");
         return injectorShells;
      }

      private State getState() {
         if (this.state == null) {
            this.state = new InheritingState(State.NONE);
         }

         return this.state;
      }
   }
}
