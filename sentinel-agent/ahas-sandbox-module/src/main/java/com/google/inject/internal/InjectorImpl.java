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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.SourceProvider;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.TypeConverterBinding;
import com.google.inject.util.Providers;
import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default {@link Injector} implementation.
 *
 * @author crazybob@google.com (Bob Lee)
 */
final class InjectorImpl implements Injector, com.google.inject.internal.Lookups {
   public static final TypeLiteral<String> STRING_TYPE = TypeLiteral.get(String.class);

   /** Options that control how the injector behaves. */
   static class InjectorOptions {
      final Stage stage;
      final boolean jitDisabled;
      final boolean disableCircularProxies;
      final boolean atInjectRequired;
      final boolean exactBindingAnnotationsRequired;

      InjectorOptions(
              Stage stage,
              boolean jitDisabled,
              boolean disableCircularProxies,
              boolean atInjectRequired,
              boolean exactBindingAnnotationsRequired) {
         this.stage = stage;
         this.jitDisabled = jitDisabled;
         this.disableCircularProxies = disableCircularProxies;
         this.atInjectRequired = atInjectRequired;
         this.exactBindingAnnotationsRequired = exactBindingAnnotationsRequired;
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(getClass())
                 .add("stage", stage)
                 .add("jitDisabled", jitDisabled)
                 .add("disableCircularProxies", disableCircularProxies)
                 .add("atInjectRequired", atInjectRequired)
                 .add("exactBindingAnnotationsRequired", exactBindingAnnotationsRequired)
                 .toString();
      }
   }

   /** some limitations on what just in time bindings are allowed. */
   enum JitLimitation {
      /** does not allow just in time bindings */
      NO_JIT,
      /** allows existing just in time bindings, but does not allow new ones */
      EXISTING_JIT,
      /** allows existing just in time bindings & allows new ones to be created */
      NEW_OR_EXISTING_JIT,
   }

   final State state;
   final InjectorImpl parent;
   final ListMultimap<TypeLiteral<?>, Binding<?>> bindingsMultimap = ArrayListMultimap.create();
   final InjectorOptions options;

   /** Just-in-time binding cache. Guarded by state.lock() */
   final Map<Key<?>, BindingImpl<?>> jitBindings = Maps.newHashMap();
   /**
    * Cache of Keys that we were unable to create JIT bindings for, so we don't keep trying. Also
    * guarded by state.lock().
    */
   final Set<Key<?>> failedJitBindings = Sets.newHashSet();

   Lookups lookups = new DeferredLookups(this);

   InjectorImpl(InjectorImpl parent, State state, InjectorOptions injectorOptions) {
      this.parent = parent;
      this.state = state;
      this.options = injectorOptions;

      if (parent != null) {
         localContext = parent.localContext;
      } else {
         // No ThreadLocal.initialValue(), as that would cause classloader leaks. See
         // https://github.com/google/guice/issues/288#issuecomment-48216933,
         // https://github.com/google/guice/issues/288#issuecomment-48216944
         localContext = new ThreadLocal<>();
      }
   }

   /** Indexes bindings by type. */
   void index() {
      for (Binding<?> binding : state.getExplicitBindingsThisLevel().values()) {
         bindingsMultimap.put(binding.getKey().getTypeLiteral(), binding);
      }
   }

   @Override
   public <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> type) {
      @SuppressWarnings("unchecked") // safe because we only put matching entries into the map
              List<Binding<T>> list = (List<Binding<T>>) (List) bindingsMultimap.get(type);
      return Collections.unmodifiableList(list);
   }

   /** Returns the binding for {@code key} */
   @Override
   public <T> BindingImpl<T> getBinding(Key<T> key) {
      Errors errors = new Errors(key);
      try {
         BindingImpl<T> result = getBindingOrThrow(key, errors, JitLimitation.EXISTING_JIT);
         errors.throwConfigurationExceptionIfErrorsExist();
         return result;
      } catch (ErrorsException e) {
         throw new ConfigurationException(errors.merge(e.getErrors()).getMessages());
      }
   }

   @Override
   public <T> BindingImpl<T> getExistingBinding(Key<T> key) {
      // Check explicit bindings, i.e. bindings created by modules.
      BindingImpl<T> explicitBinding = state.getExplicitBinding(key);
      if (explicitBinding != null) {
         return explicitBinding;
      }
      synchronized (state.lock()) {
         // See if any jit bindings have been created for this key.
         for (InjectorImpl injector = this; injector != null; injector = injector.parent) {
            @SuppressWarnings("unchecked")
            BindingImpl<T> jitBinding = (BindingImpl<T>) injector.jitBindings.get(key);
            if (jitBinding != null) {
               return jitBinding;
            }
         }
      }

      // If Key is a Provider, we have to see if the type it is providing exists,
      // and, if so, we have to create the binding for the provider.
      if (isProvider(key)) {
         try {
            // This is safe because isProvider above ensures that T is a Provider<?>
            @SuppressWarnings({"unchecked", "cast"})
            Key<?> providedKey = (Key<?>) getProvidedKey((Key) key, new Errors());
            if (getExistingBinding(providedKey) != null) {
               return getBinding(key);
            }
         } catch (ErrorsException e) {
            throw new ConfigurationException(e.getErrors().getMessages());
         }
      }

      // No existing binding exists.
      return null;
   }

   /**
    * Gets a binding implementation. First, it check to see if the parent has a binding. If the
    * parent has a binding and the binding is scoped, it will use that binding. Otherwise, this
    * checks for an explicit binding. If no explicit binding is found, it looks for a just-in-time
    * binding.
    */
   <T> BindingImpl<T> getBindingOrThrow(Key<T> key, Errors errors, JitLimitation jitType)
           throws ErrorsException {
      // Check explicit bindings, i.e. bindings created by modules.
      BindingImpl<T> binding = state.getExplicitBinding(key);
      if (binding != null) {
         return binding;
      }

      // Look for an on-demand binding.
      return getJustInTimeBinding(key, errors, jitType);
   }

   @Override
   public <T> Binding<T> getBinding(Class<T> type) {
      return getBinding(Key.get(type));
   }

   @Override
   public Injector getParent() {
      return parent;
   }

   @Override
   public Injector createChildInjector(Iterable<? extends Module> modules) {
      return new InternalInjectorCreator().parentInjector(this).addModules(modules).build();
   }

   @Override
   public Injector createChildInjector(Module... modules) {
      return createChildInjector(ImmutableList.copyOf(modules));
   }

   /**
    * Returns a just-in-time binding for {@code key}, creating it if necessary.
    *
    * @throws ErrorsException if the binding could not be created.
    */
   private <T> BindingImpl<T> getJustInTimeBinding(Key<T> key, Errors errors, JitLimitation jitType)
           throws ErrorsException {

      boolean jitOverride = isProvider(key) || isTypeLiteral(key) || isMembersInjector(key);
      synchronized (state.lock()) {
         // first try to find a JIT binding that we've already created
         for (InjectorImpl injector = this; injector != null; injector = injector.parent) {
            @SuppressWarnings("unchecked") // we only store bindings that match their key
                    BindingImpl<T> binding = (BindingImpl<T>) injector.jitBindings.get(key);

            if (binding != null) {
               // If we found a JIT binding and we don't allow them,
               // fail.  (But allow bindings created through TypeConverters.)
               if (options.jitDisabled
                       && jitType == JitLimitation.NO_JIT
                       && !jitOverride
                       && !(binding instanceof ConvertedConstantBindingImpl)) {
                  throw errors.jitDisabled(key).toException();
               } else {
                  return binding;
               }
            }
         }

         // If we previously failed creating this JIT binding and our Errors has
         // already recorded an error, then just directly throw that error.
         // We need to do this because it's possible we already cleaned up the
         // entry in jitBindings (during cleanup), and we may be trying
         // to create it again (in the case of a recursive JIT binding).
         // We need both of these guards for different reasons
         // failedJitBindings.contains: We want to continue processing if we've never
         //   failed before, so that our initial error message contains
         //   as much useful information as possible about what errors exist.
         // errors.hasErrors: If we haven't already failed, then it's OK to
         //   continue processing, to make sure the ultimate error message
         //   is the correct one.
         // See: ImplicitBindingsTest#testRecursiveJitBindingsCleanupCorrectly
         // for where this guard compes into play.
         if (failedJitBindings.contains(key) && errors.hasErrors()) {
            throw errors.toException();
         }
         return createJustInTimeBindingRecursive(key, errors, options.jitDisabled, jitType);
      } // end synchronized(state.lock())
   }

   /** Returns true if the key type is Provider (but not a subclass of Provider). */
   private static boolean isProvider(Key<?> key) {
      return key.getTypeLiteral().getRawType().equals(Provider.class);
   }

   private static boolean isTypeLiteral(Key<?> key) {
      return key.getTypeLiteral().getRawType().equals(TypeLiteral.class);
   }

   private static <T> Key<T> getProvidedKey(Key<Provider<T>> key, Errors errors)
           throws ErrorsException {
      Type providerType = key.getTypeLiteral().getType();

      // If the Provider has no type parameter (raw Provider)...
      if (!(providerType instanceof ParameterizedType)) {
         throw errors.cannotInjectRawProvider().toException();
      }

      Type entryType = ((ParameterizedType) providerType).getActualTypeArguments()[0];

      @SuppressWarnings("unchecked") // safe because T came from Key<Provider<T>>
              Key<T> providedKey = (Key<T>) key.ofType(entryType);
      return providedKey;
   }

   /** Returns true if the key type is MembersInjector (but not a subclass of MembersInjector). */
   private static boolean isMembersInjector(Key<?> key) {
      return key.getTypeLiteral().getRawType().equals(MembersInjector.class)
              && key.getAnnotationType() == null;
   }

   private <T> BindingImpl<MembersInjector<T>> createMembersInjectorBinding(
           Key<MembersInjector<T>> key, Errors errors) throws ErrorsException {
      Type membersInjectorType = key.getTypeLiteral().getType();
      if (!(membersInjectorType instanceof ParameterizedType)) {
         throw errors.cannotInjectRawMembersInjector().toException();
      }

      @SuppressWarnings("unchecked") // safe because T came from Key<MembersInjector<T>>
              TypeLiteral<T> instanceType =
              (TypeLiteral<T>)
                      TypeLiteral.get(((ParameterizedType) membersInjectorType).getActualTypeArguments()[0]);
      MembersInjector<T> membersInjector = membersInjectorStore.get(instanceType, errors);

      InternalFactory<MembersInjector<T>> factory =
              new ConstantFactory<MembersInjector<T>>(Initializables.of(membersInjector));

      return new InstanceBindingImpl<MembersInjector<T>>(
              this,
              key,
              SourceProvider.UNKNOWN_SOURCE,
              factory,
              ImmutableSet.<InjectionPoint>of(),
              membersInjector);
   }

   /**
    * Creates a synthetic binding to {@code Provider<T>}, i.e. a binding to the provider from {@code
    * Binding<T>}.
    */
   private <T> BindingImpl<Provider<T>> createProviderBinding(Key<Provider<T>> key, Errors errors)
           throws ErrorsException {
      Key<T> providedKey = getProvidedKey(key, errors);
      BindingImpl<T> delegate = getBindingOrThrow(providedKey, errors, JitLimitation.NO_JIT);
      return new ProviderBindingImpl<T>(this, key, delegate);
   }

   private static class ProviderBindingImpl<T> extends BindingImpl<Provider<T>>
           implements ProviderBinding<Provider<T>>, HasDependencies {
      final BindingImpl<T> providedBinding;

      ProviderBindingImpl(InjectorImpl injector, Key<Provider<T>> key, Binding<T> providedBinding) {
         super(
                 injector,
                 key,
                 providedBinding.getSource(),
                 createInternalFactory(providedBinding),
                 Scoping.UNSCOPED);
         this.providedBinding = (BindingImpl<T>) providedBinding;
      }

      static <T> InternalFactory<Provider<T>> createInternalFactory(Binding<T> providedBinding) {
         final Provider<T> provider = providedBinding.getProvider();
         return new InternalFactory<Provider<T>>() {
            @Override
            public Provider<T> get(InternalContext context, Dependency<?> dependency, boolean linked) {
               return provider;
            }
         };
      }

      @Override
      public Key<? extends T> getProvidedKey() {
         return providedBinding.getKey();
      }

      @Override
      public <V> V acceptTargetVisitor(BindingTargetVisitor<? super Provider<T>, V> visitor) {
         return visitor.visit(this);
      }

      @Override
      public void applyTo(Binder binder) {
         throw new UnsupportedOperationException("This element represents a synthetic binding.");
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(ProviderBinding.class)
                 .add("key", getKey())
                 .add("providedKey", getProvidedKey())
                 .toString();
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
         return ImmutableSet.<Dependency<?>>of(Dependency.get(getProvidedKey()));
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof ProviderBindingImpl) {
            ProviderBindingImpl<?> o = (ProviderBindingImpl<?>) obj;
            return getKey().equals(o.getKey())
                    && getScoping().equals(o.getScoping())
                    && Objects.equal(providedBinding, o.providedBinding);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(getKey(), getScoping(), providedBinding);
      }
   }

   /**
    * Converts a constant string binding to the required type.
    *
    * @return the binding if it could be resolved, or null if the binding doesn't exist
    * @throws ErrorsException if there was an error resolving the binding
    */
   private <T> BindingImpl<T> convertConstantStringBinding(Key<T> key, Errors errors)
           throws ErrorsException {
      // Find a constant string binding.
      Key<String> stringKey = key.ofType(STRING_TYPE);
      BindingImpl<String> stringBinding = state.getExplicitBinding(stringKey);
      if (stringBinding == null || !stringBinding.isConstant()) {
         return null;
      }

      // We can't call getProvider().get() because this InstanceBinding may not have been inintialized
      // yet (because we may have been called during InternalInjectorCreator.initializeStatically and
      // instance binding validation hasn't happened yet.)
      @SuppressWarnings("unchecked")
      String stringValue = ((InstanceBinding<String>) stringBinding).getInstance();
      Object source = stringBinding.getSource();

      // Find a matching type converter.
      TypeLiteral<T> type = key.getTypeLiteral();
      TypeConverterBinding typeConverterBinding =
              state.getConverter(stringValue, type, errors, source);

      if (typeConverterBinding == null) {
         // No converter can handle the given type.
         return null;
      }

      // Try to convert the string. A failed conversion results in an error.
      try {
         @SuppressWarnings("unchecked") // This cast is safe because we double check below.
                 T converted = (T) typeConverterBinding.getTypeConverter().convert(stringValue, type);

         if (converted == null) {
            throw errors
                    .converterReturnedNull(stringValue, source, type, typeConverterBinding)
                    .toException();
         }

         if (!type.getRawType().isInstance(converted)) {
            throw errors
                    .conversionTypeError(stringValue, source, type, typeConverterBinding, converted)
                    .toException();
         }

         return new ConvertedConstantBindingImpl<T>(
                 this, key, converted, stringBinding, typeConverterBinding);
      } catch (ErrorsException e) {
         throw e;
      } catch (RuntimeException e) {
         throw errors
                 .conversionError(stringValue, source, type, typeConverterBinding, e)
                 .toException();
      }
   }

   private static class ConvertedConstantBindingImpl<T> extends BindingImpl<T>
           implements ConvertedConstantBinding<T> {
      final T value;
      final Provider<T> provider;
      final Binding<String> originalBinding;
      final TypeConverterBinding typeConverterBinding;

      ConvertedConstantBindingImpl(
              InjectorImpl injector,
              Key<T> key,
              T value,
              Binding<String> originalBinding,
              TypeConverterBinding typeConverterBinding) {
         super(
                 injector,
                 key,
                 originalBinding.getSource(),
                 new ConstantFactory<T>(Initializables.of(value)),
                 Scoping.UNSCOPED);
         this.value = value;
         provider = Providers.of(value);
         this.originalBinding = originalBinding;
         this.typeConverterBinding = typeConverterBinding;
      }

      @Override
      public Provider<T> getProvider() {
         return provider;
      }

      @Override
      public <V> V acceptTargetVisitor(BindingTargetVisitor<? super T, V> visitor) {
         return visitor.visit(this);
      }

      @Override
      public T getValue() {
         return value;
      }

      @Override
      public TypeConverterBinding getTypeConverterBinding() {
         return typeConverterBinding;
      }

      @Override
      public Key<String> getSourceKey() {
         return originalBinding.getKey();
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
         return ImmutableSet.<Dependency<?>>of(Dependency.get(getSourceKey()));
      }

      @Override
      public void applyTo(Binder binder) {
         throw new UnsupportedOperationException("This element represents a synthetic binding.");
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(ConvertedConstantBinding.class)
                 .add("key", getKey())
                 .add("sourceKey", getSourceKey())
                 .add("value", value)
                 .toString();
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof ConvertedConstantBindingImpl) {
            ConvertedConstantBindingImpl<?> o = (ConvertedConstantBindingImpl<?>) obj;
            return getKey().equals(o.getKey())
                    && getScoping().equals(o.getScoping())
                    && Objects.equal(value, o.value);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(getKey(), getScoping(), value);
      }
   }

   <T> void initializeBinding(BindingImpl<T> binding, Errors errors) throws ErrorsException {
      if (binding instanceof DelayedInitialize) {
         ((DelayedInitialize) binding).initialize(this, errors);
      }
   }

   <T> void initializeJitBinding(BindingImpl<T> binding, Errors errors) throws ErrorsException {
      // Put the partially constructed binding in the map a little early. This enables us to handle
      // circular dependencies. Example: FooImpl -> BarImpl -> FooImpl.
      // Note: We don't need to synchronize on state.lock() during injector creation.
      if (binding instanceof DelayedInitialize) {
         Key<T> key = binding.getKey();
         jitBindings.put(key, binding);
         boolean successful = false;
         DelayedInitialize delayed = (DelayedInitialize) binding;
         try {
            delayed.initialize(this, errors);
            successful = true;
         } finally {
            if (!successful) {
               // We do not pass cb.getInternalConstructor as the second parameter
               // so that cached exceptions while constructing it get stored.
               // See TypeListenerTest#testTypeListenerThrows
               removeFailedJitBinding(binding, null);
               cleanup(binding, new HashSet<Key>());
            }
         }
      }
   }

   /**
    * Iterates through the binding's dependencies to clean up any stray bindings that were leftover
    * from a failed JIT binding. This is required because the bindings are eagerly & optimistically
    * added to allow circular dependency support, so dependencies may pass where they should have
    * failed.
    */
   private boolean cleanup(BindingImpl<?> binding, Set<Key> encountered) {
      boolean bindingFailed = false;
      Set<Dependency<?>> deps = getInternalDependencies(binding);
      for (Dependency dep : deps) {
         Key<?> depKey = dep.getKey();
         InjectionPoint ip = dep.getInjectionPoint();
         if (encountered.add(depKey)) { // only check if we haven't looked at this key yet
            BindingImpl depBinding = jitBindings.get(depKey);
            if (depBinding != null) { // if the binding still exists, validate
               boolean failed = cleanup(depBinding, encountered); // if children fail, we fail
               if (depBinding instanceof ConstructorBindingImpl) {
                  ConstructorBindingImpl ctorBinding = (ConstructorBindingImpl) depBinding;
                  ip = ctorBinding.getInternalConstructor();
                  if (!ctorBinding.isInitialized()) {
                     failed = true;
                  }
               }
               if (failed) {
                  removeFailedJitBinding(depBinding, ip);
                  bindingFailed = true;
               }
            } else if (state.getExplicitBinding(depKey) == null) {
               // ignore keys if they were explicitly bound, but if neither JIT
               // nor explicit, it's also invalid & should let parent know.
               bindingFailed = true;
            }
         }
      }
      return bindingFailed;
   }

   /** Cleans up any state that may have been cached when constructing the JIT binding. */
   private void removeFailedJitBinding(Binding<?> binding, InjectionPoint ip) {
      failedJitBindings.add(binding.getKey());
      jitBindings.remove(binding.getKey());
      membersInjectorStore.remove(binding.getKey().getTypeLiteral());
      provisionListenerStore.remove(binding);
      if (ip != null) {
         constructors.remove(ip);
      }
   }

   /** Safely gets the dependencies of possibly not initialized bindings. */
   @SuppressWarnings("unchecked")
   private Set<Dependency<?>> getInternalDependencies(BindingImpl<?> binding) {
      if (binding instanceof ConstructorBindingImpl) {
         return ((ConstructorBindingImpl) binding).getInternalDependencies();
      } else if (binding instanceof HasDependencies) {
         return ((HasDependencies) binding).getDependencies();
      } else {
         return ImmutableSet.of();
      }
   }

   /**
    * Creates a binding for an injectable type with the given scope. Looks for a scope on the type if
    * none is specified.
    */
   <T> BindingImpl<T> createUninitializedBinding(
           Key<T> key, Scoping scoping, Object source, Errors errors, boolean jitBinding)
           throws ErrorsException {
      Class<?> rawType = key.getTypeLiteral().getRawType();

      ImplementedBy implementedBy = rawType.getAnnotation(ImplementedBy.class);

      // Don't try to inject arrays or enums annotated with @ImplementedBy.
      if (rawType.isArray() || (rawType.isEnum() && implementedBy != null)) {
         throw errors.missingImplementationWithHint(key, this).toException();
      }

      // Handle TypeLiteral<T> by binding the inner type
      if (rawType == TypeLiteral.class) {
         @SuppressWarnings("unchecked") // we have to fudge the inner type as Object
                 BindingImpl<T> binding =
                 (BindingImpl<T>) createTypeLiteralBinding((Key<TypeLiteral<Object>>) key, errors);
         return binding;
      }

      // Handle @ImplementedBy
      if (implementedBy != null) {
         Annotations.checkForMisplacedScopeAnnotations(rawType, source, errors);
         return createImplementedByBinding(key, scoping, implementedBy, errors);
      }

      // Handle @ProvidedBy.
      ProvidedBy providedBy = rawType.getAnnotation(ProvidedBy.class);
      if (providedBy != null) {
         Annotations.checkForMisplacedScopeAnnotations(rawType, source, errors);
         return createProvidedByBinding(key, scoping, providedBy, errors);
      }

      return ConstructorBindingImpl.create(
              this,
              key,
              null, /* use default constructor */
              source,
              scoping,
              errors,
              jitBinding && options.jitDisabled,
              options.atInjectRequired);
   }

   /**
    * Converts a binding for a {@code Key<TypeLiteral<T>>} to the value {@code TypeLiteral<T>}. It's
    * a bit awkward because we have to pull out the inner type in the type literal.
    */
   private <T> BindingImpl<TypeLiteral<T>> createTypeLiteralBinding(
           Key<TypeLiteral<T>> key, Errors errors) throws ErrorsException {
      Type typeLiteralType = key.getTypeLiteral().getType();
      if (!(typeLiteralType instanceof ParameterizedType)) {
         throw errors.cannotInjectRawTypeLiteral().toException();
      }

      ParameterizedType parameterizedType = (ParameterizedType) typeLiteralType;
      Type innerType = parameterizedType.getActualTypeArguments()[0];

      // this is unforunate. We don't support building TypeLiterals for type variable like 'T'. If
      // this proves problematic, we can probably fix TypeLiteral to support type variables
      if (!(innerType instanceof Class)
              && !(innerType instanceof GenericArrayType)
              && !(innerType instanceof ParameterizedType)) {
         throw errors.cannotInjectTypeLiteralOf(innerType).toException();
      }

      @SuppressWarnings("unchecked") // by definition, innerType == T, so this is safe
              TypeLiteral<T> value = (TypeLiteral<T>) TypeLiteral.get(innerType);
      InternalFactory<TypeLiteral<T>> factory =
              new ConstantFactory<TypeLiteral<T>>(Initializables.of(value));
      return new InstanceBindingImpl<TypeLiteral<T>>(
              this,
              key,
              SourceProvider.UNKNOWN_SOURCE,
              factory,
              ImmutableSet.<InjectionPoint>of(),
              value);
   }

   /** Creates a binding for a type annotated with @ProvidedBy. */
   <T> BindingImpl<T> createProvidedByBinding(
           Key<T> key, Scoping scoping, ProvidedBy providedBy, Errors errors) throws ErrorsException {
      Class<?> rawType = key.getTypeLiteral().getRawType();
      Class<? extends javax.inject.Provider<?>> providerType = providedBy.value();

      // Make sure it's not the same type. TODO: Can we check for deeper loops?
      if (providerType == rawType) {
         throw errors.recursiveProviderType().toException();
      }

      // Assume the provider provides an appropriate type. We double check at runtime.
      @SuppressWarnings("unchecked")
      Key<? extends Provider<T>> providerKey = (Key<? extends Provider<T>>) Key.get(providerType);
      ProvidedByInternalFactory<T> internalFactory =
              new ProvidedByInternalFactory<T>(rawType, providerType, providerKey);
      Object source = rawType;
      BindingImpl<T> binding =
              LinkedProviderBindingImpl.createWithInitializer(
                      this,
                      key,
                      source,
                      Scoping.<T>scope(key, this, internalFactory, source, scoping),
                      scoping,
                      providerKey,
                      internalFactory);
      internalFactory.setProvisionListenerCallback(provisionListenerStore.get(binding));
      return binding;
   }

   /** Creates a binding for a type annotated with @ImplementedBy. */
   private <T> BindingImpl<T> createImplementedByBinding(
           Key<T> key, Scoping scoping, ImplementedBy implementedBy, Errors errors)
           throws ErrorsException {
      Class<?> rawType = key.getTypeLiteral().getRawType();
      Class<?> implementationType = implementedBy.value();

      // Make sure it's not the same type. TODO: Can we check for deeper cycles?
      if (implementationType == rawType) {
         throw errors.recursiveImplementationType().toException();
      }

      // Make sure implementationType extends type.
      if (!rawType.isAssignableFrom(implementationType)) {
         throw errors.notASubtype(implementationType, rawType).toException();
      }

      @SuppressWarnings("unchecked") // After the preceding check, this cast is safe.
              Class<? extends T> subclass = (Class<? extends T>) implementationType;

      // Look up the target binding.
      final Key<? extends T> targetKey = Key.get(subclass);
      Object source = rawType;
      FactoryProxy<T> factory = new FactoryProxy<>(this, key, targetKey, source);
      factory.notify(errors); // causes the factory to initialize itself internally
      return new LinkedBindingImpl<T>(
              this,
              key,
              source,
              Scoping.<T>scope(key, this, factory, source, scoping),
              scoping,
              targetKey);
   }

   /**
    * Attempts to create a just-in-time binding for {@code key} in the root injector, falling back to
    * other ancestor injectors until this injector is tried.
    */
   private <T> BindingImpl<T> createJustInTimeBindingRecursive(
           Key<T> key, Errors errors, boolean jitDisabled, JitLimitation jitType)
           throws ErrorsException {
      // ask the parent to create the JIT binding
      if (parent != null) {
         if (jitType == JitLimitation.NEW_OR_EXISTING_JIT
                 && jitDisabled
                 && !parent.options.jitDisabled) {
            // If the binding would be forbidden here but allowed in a parent, report an error instead
            throw errors.jitDisabledInParent(key).toException();
         }

         try {
            return parent.createJustInTimeBindingRecursive(
                    key,
                    new Errors(),
                    jitDisabled,
                    parent.options.jitDisabled ? JitLimitation.NO_JIT : jitType);
         } catch (ErrorsException ignored) {
         }
      }

      // Retrieve the sources before checking for blacklisting to guard against sources becoming null
      // due to a full GC happening after calling state.isBlacklisted and
      // state.getSourcesForBlacklistedKey.
      // TODO(user): Consolidate these two APIs.
      Set<Object> sources = state.getSourcesForBlacklistedKey(key);
      if (state.isBlacklisted(key)) {
         throw errors.childBindingAlreadySet(key, sources).toException();
      }

      key = MoreTypes.canonicalizeKey(key); // before storing the key long-term, canonicalize it.
      BindingImpl<T> binding = createJustInTimeBinding(key, errors, jitDisabled, jitType);
      state.parent().blacklist(key, state, binding.getSource());
      jitBindings.put(key, binding);
      return binding;
   }

   /**
    * Returns a new just-in-time binding created by resolving {@code key}. The strategies used to
    * create just-in-time bindings are:
    *
    * <ol>
    * <li>Internalizing Providers. If the requested binding is for {@code Provider<T>}, we delegate
    *     to the binding for {@code T}.
    * <li>Converting constants.
    * <li>ImplementedBy and ProvidedBy annotations. Only for unannotated keys.
    * <li>The constructor of the raw type. Only for unannotated keys.
    * </ol>
    *
    * @throws ErrorsException if the binding cannot be created.
    */
   private <T> BindingImpl<T> createJustInTimeBinding(
           Key<T> key, Errors errors, boolean jitDisabled, JitLimitation jitType)
           throws ErrorsException {
      int numErrorsBefore = errors.size();

      // Retrieve the sources before checking for blacklisting to guard against sources becoming null
      // due to a full GC happening after calling state.isBlacklisted and
      // state.getSourcesForBlacklistedKey.
      // TODO(user): Consolidate these two APIs.
      Set<Object> sources = state.getSourcesForBlacklistedKey(key);
      if (state.isBlacklisted(key)) {
         throw errors.childBindingAlreadySet(key, sources).toException();
      }

      // Handle cases where T is a Provider<?>.
      if (isProvider(key)) {
         // These casts are safe. We know T extends Provider<X> and that given Key<Provider<X>>,
         // createProviderBinding() will return BindingImpl<Provider<X>>.
         @SuppressWarnings({"unchecked", "cast"})
         BindingImpl<T> binding = (BindingImpl<T>) createProviderBinding((Key) key, errors);
         return binding;
      }

      // Handle cases where T is a MembersInjector<?>
      if (isMembersInjector(key)) {
         // These casts are safe. T extends MembersInjector<X> and that given Key<MembersInjector<X>>,
         // createMembersInjectorBinding() will return BindingImpl<MembersInjector<X>>.
         @SuppressWarnings({"unchecked", "cast"})
         BindingImpl<T> binding = (BindingImpl<T>) createMembersInjectorBinding((Key) key, errors);
         return binding;
      }

      // Try to convert a constant string binding to the requested type.
      BindingImpl<T> convertedBinding = convertConstantStringBinding(key, errors);
      if (convertedBinding != null) {
         return convertedBinding;
      }

      if (!isTypeLiteral(key) && jitDisabled && jitType != JitLimitation.NEW_OR_EXISTING_JIT) {
         throw errors.jitDisabled(key).toException();
      }

      // If the key has an annotation...
      if (key.getAnnotationType() != null) {
         // Look for a binding without annotation attributes or return null.
         if (key.hasAttributes() && !options.exactBindingAnnotationsRequired) {
            try {
               Errors ignored = new Errors();
               return getBindingOrThrow(key.withoutAttributes(), ignored, JitLimitation.NO_JIT);
            } catch (ErrorsException ignored) {
               // throw with a more appropriate message below
            }
         }
         throw errors.missingImplementationWithHint(key, this).toException();
      }

      Object source = key.getTypeLiteral().getRawType();
      BindingImpl<T> binding =
              createUninitializedBinding(key, Scoping.UNSCOPED, source, errors, true);
      errors.throwIfNewErrors(numErrorsBefore);
      initializeJitBinding(binding, errors);
      return binding;
   }

   <T> InternalFactory<? extends T> getInternalFactory(
           Key<T> key, Errors errors, JitLimitation jitType) throws ErrorsException {
      return getBindingOrThrow(key, errors, jitType).getInternalFactory();
   }

   @Override
   public Map<Key<?>, Binding<?>> getBindings() {
      return state.getExplicitBindingsThisLevel();
   }

   @Override
   public Map<Key<?>, Binding<?>> getAllBindings() {
      synchronized (state.lock()) {
         return new ImmutableMap.Builder<Key<?>, Binding<?>>()
                 .putAll(state.getExplicitBindingsThisLevel())
                 .putAll(jitBindings)
                 .build();
      }
   }

   @Override
   public Map<Class<? extends Annotation>, Scope> getScopeBindings() {
      return ImmutableMap.copyOf(state.getScopes());
   }

   @Override
   public Set<TypeConverterBinding> getTypeConverterBindings() {
      return ImmutableSet.copyOf(state.getConvertersThisLevel());
   }

   /** Returns parameter injectors, or {@code null} if there are no parameters. */
   SingleParameterInjector<?>[] getParametersInjectors(List<Dependency<?>> parameters, Errors errors)
           throws ErrorsException {
      if (parameters.isEmpty()) {
         return null;
      }

      int numErrorsBefore = errors.size();
      SingleParameterInjector<?>[] result = new SingleParameterInjector<?>[parameters.size()];
      int i = 0;
      for (Dependency<?> parameter : parameters) {
         try {
            result[i++] = createParameterInjector(parameter, errors.withSource(parameter));
         } catch (ErrorsException rethrownBelow) {
            // rethrown below
         }
      }

      errors.throwIfNewErrors(numErrorsBefore);
      return result;
   }

   <T> SingleParameterInjector<T> createParameterInjector(
           final Dependency<T> dependency, final Errors errors) throws ErrorsException {
      BindingImpl<? extends T> binding =
              getBindingOrThrow(dependency.getKey(), errors, JitLimitation.NO_JIT);
      return new SingleParameterInjector<T>(dependency, binding);
   }

   /** Invokes a method. */
   interface MethodInvoker {
      Object invoke(Object target, Object... parameters)
              throws IllegalAccessException, InvocationTargetException;
   }

   /** Cached constructor injectors for each type */
   final ConstructorInjectorStore constructors = new ConstructorInjectorStore(this);

   /** Cached field and method injectors for each type. */
   MembersInjectorStore membersInjectorStore;

   /** Cached provision listener callbacks for each key. */
   ProvisionListenerCallbackStore provisionListenerStore;

   @Override
   @SuppressWarnings("unchecked") // the members injector type is consistent with instance's type
   public void injectMembers(Object instance) {
      MembersInjector membersInjector = getMembersInjector(instance.getClass());
      membersInjector.injectMembers(instance);
   }

   @Override
   public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
      Errors errors = new Errors(typeLiteral);
      try {
         return membersInjectorStore.get(typeLiteral, errors);
      } catch (ErrorsException e) {
         throw new ConfigurationException(errors.merge(e.getErrors()).getMessages());
      }
   }

   @Override
   public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
      return getMembersInjector(TypeLiteral.get(type));
   }

   @Override
   public <T> Provider<T> getProvider(Class<T> type) {
      return getProvider(Key.get(type));
   }

   <T> Provider<T> getProviderOrThrow(final com.google.inject.spi.Dependency<T> dependency, Errors errors)
           throws ErrorsException {
      Key<T> key = dependency.getKey();
      BindingImpl<? extends T> binding = getBindingOrThrow(key, errors, JitLimitation.NO_JIT);
      final InternalFactory<? extends T> internalFactory = binding.getInternalFactory();
      final Object source = binding.getSource();

      return new Provider<T>() {
         @Override
         public T get() {
            InternalContext currentContext = enterContext();
            com.google.inject.spi.Dependency previous = currentContext.pushDependency(dependency, source);
            try {
               T t = internalFactory.get(currentContext, dependency, false);
               return t;
            } catch (InternalProvisionException e) {
               throw e.addSource(dependency).toProvisionException();
            } finally {
               currentContext.popStateAndSetDependency(previous);
               currentContext.close();
            }
         }

         @Override
         public String toString() {
            return internalFactory.toString();
         }
      };
   }

   @Override
   public <T> Provider<T> getProvider(final Key<T> key) {
      Errors errors = new Errors(key);
      try {
         Provider<T> result = getProviderOrThrow(com.google.inject.spi.Dependency.get(key), errors);
         errors.throwIfNewErrors(0);
         return result;
      } catch (ErrorsException e) {
         throw new ConfigurationException(errors.merge(e.getErrors()).getMessages());
      }
   }

   @Override
   public <T> T getInstance(Key<T> key) {
      return getProvider(key).get();
   }

   @Override
   public <T> T getInstance(Class<T> type) {
      return getProvider(type).get();
   }

   /**
    * Holds Object[] as a mutable wrapper, rather than InternalContext, since array operations are
    * faster than ThreadLocal.set() / .get() operations.
    *
    * <p>Holds Object[] rather than InternalContext[], since localContext never gets cleaned up at
    * any point. This could lead to problems when, for example, an OSGI application is reloaded, the
    * InjectorImpl is destroyed, but the thread that the injector runs on is kept alive. In such a
    * case, ThreadLocal itself would hold on to a reference to localContext, which would hold on to
    * the old InternalContext.class object, which would hold on to the old classloader that loaded
    * that class, and so on.
    */
   private final ThreadLocal<Object[]> localContext;

   /** Only to be called by the {@link SingletonScope} provider. */
   InternalContext getLocalContext() {
      return (InternalContext) localContext.get()[0];
   }

   /**
    * Looks up thread local context and {@link InternalContext#enter() enters} it or creates a new
    * context if necessary.
    *
    * <p>All callers of this are responsible for calling {@link InternalContext#close()}. Typical
    * usage should look like:
    *
    * <pre>{@code
    * InternalContext ctx = injector.enterContext();
    * try {
    *   ... use ctx ...
    * } finally {
    *   ctx.close();
    * }
    * }</pre>
    */
   InternalContext enterContext() {
      Object[] reference = localContext.get();
      if (reference == null) {
         reference = new Object[1];
         localContext.set(reference);
      }
      InternalContext ctx = (InternalContext) reference[0];
      if (ctx == null) {
         reference[0] = ctx = new InternalContext(options, reference);
      } else {
         ctx.enter();
      }
      return ctx;
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(Injector.class)
              .add("bindings", state.getExplicitBindingsThisLevel().values())
              .toString();
   }
}
