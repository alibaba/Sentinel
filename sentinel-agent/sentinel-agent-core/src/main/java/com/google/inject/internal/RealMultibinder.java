package com.google.inject.internal;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.*;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.internal.InternalProviderInstanceBindingImpl.InitializationTiming;
import com.google.inject.multibindings.MultibinderBinding;
import com.google.inject.multibindings.MultibindingsTargetVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.util.Types;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.inject.internal.Element.Type.MULTIBINDER;
import static com.google.inject.internal.Errors.checkConfiguration;
import static com.google.inject.internal.Errors.checkNotNull;
import static com.google.inject.name.Names.named;

/**
 * The actual multibinder plays several roles:
 *
 * <p>As a Multibinder, it acts as a factory for LinkedBindingBuilders for each of the set's
 * elements. Each binding is given an annotation that identifies it as a part of this set.
 *
 * <p>As a Module, it installs the binding to the set itself. As a module, this implements equals()
 * and hashcode() in order to trick Guice into executing its configure() method only once. That
 * makes it so that multiple multibinders can be created for the same target collection, but only
 * one is bound. Since the list of bindings is retrieved from the injector itself (and not the
 * multibinder), each multibinder has access to all contributions from all multibinders.
 *
 * <p>As a Provider, this constructs the set instances.
 *
 * <p>We use a subclass to hide 'implements Module, Provider' from the public API.
 */
public final class RealMultibinder<T> implements Module {

   /** Implementation of newSetBinder. */
   public static <T> RealMultibinder<T> newRealSetBinder(Binder binder, Key<T> key) {
      binder = binder.skipSources(RealMultibinder.class);
      RealMultibinder<T> result = new RealMultibinder<>(binder, key);
      binder.install(result);
      return result;
   }

   @SuppressWarnings("unchecked") // wrapping a T in a Set safely returns a Set<T>
   static <T> TypeLiteral<Set<T>> setOf(TypeLiteral<T> elementType) {
      Type type = Types.setOf(elementType.getType());
      return (TypeLiteral<Set<T>>) TypeLiteral.get(type);
   }

   @SuppressWarnings("unchecked")
   static <T> TypeLiteral<Collection<Provider<T>>> collectionOfProvidersOf(
           TypeLiteral<T> elementType) {
      Type providerType = Types.providerOf(elementType.getType());
      Type type = Types.collectionOf(providerType);
      return (TypeLiteral<Collection<Provider<T>>>) TypeLiteral.get(type);
   }

   @SuppressWarnings("unchecked")
   static <T> TypeLiteral<Collection<javax.inject.Provider<T>>> collectionOfJavaxProvidersOf(
           TypeLiteral<T> elementType) {
      Type providerType =
              Types.newParameterizedType(javax.inject.Provider.class, elementType.getType());
      Type type = Types.collectionOf(providerType);
      return (TypeLiteral<Collection<javax.inject.Provider<T>>>) TypeLiteral.get(type);
   }

   private final BindingSelection<T> bindingSelection;
   private final Binder binder;

   RealMultibinder(Binder binder, Key<T> key) {
      this.binder = checkNotNull(binder, "binder");
      this.bindingSelection = new BindingSelection<>(key);
   }

   @Override
   public void configure(Binder binder) {
      checkConfiguration(!bindingSelection.isInitialized(), "Multibinder was already initialized");
      binder
              .bind(bindingSelection.getSetKey())
              .toProvider(new RealMultibinderProvider<T>(bindingSelection));
      Provider<Collection<Provider<T>>> collectionOfProvidersProvider =
              new RealMultibinderCollectionOfProvidersProvider<T>(bindingSelection);
      binder
              .bind(bindingSelection.getCollectionOfProvidersKey())
              .toProvider(collectionOfProvidersProvider);

      // The collection this exposes is internally an ImmutableList, so it's OK to massage
      // the guice Provider to javax Provider in the value (since the guice Provider implements
      // javax Provider).
      @SuppressWarnings("unchecked")
      Provider<Collection<javax.inject.Provider<T>>> javaxProvider =
              (Provider) collectionOfProvidersProvider;
      binder.bind(bindingSelection.getCollectionOfJavaxProvidersKey()).toProvider(javaxProvider);
   }

   public void permitDuplicates() {
      binder.install(new PermitDuplicatesModule(bindingSelection.getPermitDuplicatesKey()));
   }

   /** Adds a new entry to the set and returns the key for it. */
   Key<T> getKeyForNewItem() {
      checkConfiguration(!bindingSelection.isInitialized(), "Multibinder was already initialized");
      return Key.get(
              bindingSelection.getElementTypeLiteral(),
              new RealElement(bindingSelection.getSetName(), MULTIBINDER, ""));
   }

   public LinkedBindingBuilder<T> addBinding() {
      return binder.bind(getKeyForNewItem());
   }

   // These methods are used by RealMapBinder

   Key<Set<T>> getSetKey() {
      return bindingSelection.getSetKey();
   }

   TypeLiteral<T> getElementTypeLiteral() {
      return bindingSelection.getElementTypeLiteral();
   }

   String getSetName() {
      return bindingSelection.getSetName();
   }

   boolean permitsDuplicates(Injector injector) {
      return bindingSelection.permitsDuplicates(injector);
   }

   boolean containsElement(com.google.inject.spi.Element element) {
      return bindingSelection.containsElement(element);
   }

   private static final class RealMultibinderProvider<T>
           extends InternalProviderInstanceBindingImpl.Factory<Set<T>>
           implements ProviderWithExtensionVisitor<Set<T>>, MultibinderBinding<Set<T>> {
      private final BindingSelection<T> bindingSelection;
      private List<Binding<T>> bindings;
      private SingleParameterInjector<T>[] injectors;
      private boolean permitDuplicates;

      RealMultibinderProvider(BindingSelection<T> bindingSelection) {
         // While Multibinders only depend on bindings created in modules so we could theoretically
         // initialize eagerly, they also depend on
         // 1. findBindingsByType returning results
         // 2. being able to call BindingImpl.acceptTargetVisitor
         // neither of those is available during eager initialization, so we use DELAYED
         super(InitializationTiming.DELAYED);
         this.bindingSelection = bindingSelection;
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
         return bindingSelection.getDependencies();
      }

      @Override
      void initialize(InjectorImpl injector, Errors errors) throws ErrorsException {
         bindingSelection.initialize(injector, errors);
         this.bindings = bindingSelection.getBindings();
         this.injectors = bindingSelection.getParameterInjectors();
         this.permitDuplicates = bindingSelection.permitsDuplicates();
      }

      @Override
      protected Set<T> doProvision(InternalContext context, Dependency<?> dependency)
              throws InternalProvisionException {
         SingleParameterInjector<T>[] localInjectors = injectors;
         if (localInjectors == null) {
            // if localInjectors == null, then we have no bindings so return the empty set.
            return ImmutableSet.of();
         }
         // Ideally we would just add to an ImmutableSet.Builder, but if we did that and there were
         // duplicates we wouldn't be able to tell which one was the duplicate.  So to manage this we
         // first put everything into an array and then construct the set.  This way if something gets
         // dropped we can figure out what it is.
         @SuppressWarnings("unchecked")
         T[] values = (T[]) new Object[localInjectors.length];
         for (int i = 0; i < localInjectors.length; i++) {
            SingleParameterInjector<T> parameterInjector = localInjectors[i];
            T newValue = parameterInjector.inject(context);
            if (newValue == null) {
               throw newNullEntryException(i);
            }
            values[i] = newValue;
         }
         ImmutableSet<T> set = ImmutableSet.copyOf(values);
         // There are fewer items in the set than the array.  Figure out which one got dropped.
         if (!permitDuplicates && set.size() < values.length) {
            throw newDuplicateValuesException(set, values);
         }
         return set;
      }

      private InternalProvisionException newNullEntryException(int i) {
         return InternalProvisionException.create(
                 "Set injection failed due to null element bound at: %s", bindings.get(i).getSource());
      }

      @SuppressWarnings("unchecked")
      @Override
      public <B, V> V acceptExtensionVisitor(
              BindingTargetVisitor<B, V> visitor, ProviderInstanceBinding<? extends B> binding) {
         if (visitor instanceof MultibindingsTargetVisitor) {
            return ((MultibindingsTargetVisitor<Set<T>, V>) visitor).visit(this);
         } else {
            return visitor.visit(binding);
         }
      }

      private InternalProvisionException newDuplicateValuesException(
              ImmutableSet<T> set, T[] values) {
         // TODO(lukes): consider reporting all duplicate values, the easiest way would be to rebuild
         // a new set and detect dupes as we go
         // Find the duplicate binding
         // To do this we take advantage of the fact that set, values and bindings all have the same
         // ordering for a non-empty prefix of the set.
         // First we scan for the first item dropped from the set.
         int newBindingIndex = 0;
         for (T item : set) {
            if (item != values[newBindingIndex]) {
               break;
            }
            newBindingIndex++;
         }
         // once we exit the loop newBindingIndex will point at the first item in values that was
         // dropped.

         Binding<T> newBinding = bindings.get(newBindingIndex);
         T newValue = values[newBindingIndex];
         // Now we scan again to find the index of the value, we are guaranteed to find it.
         int oldBindingIndex = set.asList().indexOf(newValue);
         T oldValue = values[oldBindingIndex];
         Binding<T> duplicateBinding = bindings.get(oldBindingIndex);
         String oldString = oldValue.toString();
         String newString = newValue.toString();
         if (Objects.equal(oldString, newString)) {
            // When the value strings match, just show the source of the bindings
            return InternalProvisionException.create(
                    "Set injection failed due to duplicated element \"%s\""
                            + "\n    Bound at %s\n    Bound at %s",
                    newValue, duplicateBinding.getSource(), newBinding.getSource());
         } else {
            // When the value strings don't match, include them both as they may be useful for debugging
            return InternalProvisionException.create(
                    "Set injection failed due to multiple elements comparing equal:"
                            + "\n    \"%s\"\n        bound at %s"
                            + "\n    \"%s\"\n        bound at %s",
                    oldValue, duplicateBinding.getSource(), newValue, newBinding.getSource());
         }
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof RealMultibinderProvider
                 && bindingSelection.equals(((RealMultibinderProvider<?>) obj).bindingSelection);
      }

      @Override
      public int hashCode() {
         return bindingSelection.hashCode();
      }

      @Override
      public Key<Set<T>> getSetKey() {
         return bindingSelection.getSetKey();
      }

      @Override
      public TypeLiteral<?> getElementTypeLiteral() {
         return bindingSelection.getElementTypeLiteral();
      }

      @Override
      public List<Binding<?>> getElements() {
         return bindingSelection.getElements();
      }

      @Override
      public boolean permitsDuplicates() {
         return bindingSelection.permitsDuplicates();
      }

      @Override
      public boolean containsElement(com.google.inject.spi.Element element) {
         return bindingSelection.containsElement(element);
      }
   }

   private static final class BindingSelection<T> {
      // prior to initialization we declare just a dependency on the injector, but as soon as we are
      // initialized we swap to dependencies on the elements.
      private static final ImmutableSet<Dependency<?>> MODULE_DEPENDENCIES =
              ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(Injector.class)));
      private final TypeLiteral<T> elementType;
      private final Key<Set<T>> setKey;

      // these are all lazily allocated
      private String setName;
      private Key<Collection<Provider<T>>> collectionOfProvidersKey;
      private Key<Collection<javax.inject.Provider<T>>> collectionOfJavaxProvidersKey;
      private Key<Boolean> permitDuplicatesKey;

      private boolean isInitialized;
      /* a binding for each element in the set. null until initialization, non-null afterwards */
      private ImmutableList<Binding<T>> bindings;

      // Starts out as Injector and gets set up properly after initialization
      private ImmutableSet<Dependency<?>> dependencies = MODULE_DEPENDENCIES;
      private ImmutableSet<Dependency<?>> providerDependencies = MODULE_DEPENDENCIES;

      /** whether duplicates are allowed. Possibly configured by a different instance */
      private boolean permitDuplicates;

      private SingleParameterInjector<T>[] parameterinjectors;

      BindingSelection(Key<T> key) {
         this.setKey = key.ofType(setOf(key.getTypeLiteral()));
         this.elementType = key.getTypeLiteral();
      }

      void initialize(InjectorImpl injector, Errors errors) throws ErrorsException {
         // This will be called multiple times, once by each Factory. We only want
         // to do the work to initialize everything once, so guard this code with
         // isInitialized.
         if (isInitialized) {
            return;
         }
         List<Binding<T>> bindings = Lists.newArrayList();
         Set<Indexer.IndexedBinding> index = Sets.newHashSet();
         Indexer indexer = new Indexer(injector);
         List<Dependency<?>> dependencies = Lists.newArrayList();
         List<Dependency<?>> providerDependencies = Lists.newArrayList();
         for (Binding<?> entry : injector.findBindingsByType(elementType)) {
            if (keyMatches(entry.getKey())) {
               @SuppressWarnings("unchecked") // protected by findBindingsByType()
                       Binding<T> binding = (Binding<T>) entry;
               if (index.add(binding.acceptTargetVisitor(indexer))) {
                  // TODO(lukes): most of these are linked bindings since user bindings are linked to
                  // a user binding through the @Element annotation.  Since this is an implementation
                  // detail we could 'dereference' the @Element if it is a LinkedBinding and avoid
                  // provisioning through the FactoryProxy at runtime.
                  // Ditto for OptionalBinder/MapBinder
                  bindings.add(binding);
                  Key<T> key = binding.getKey();
                  // TODO(lukes): we should mark this as a non-nullable dependency since we don't accept
                  // null.
                  // Add a dependency on Key<T>
                  dependencies.add(Dependency.get(key));
                  // and add a dependency on Key<Provider<T>>
                  providerDependencies.add(
                          Dependency.get(key.ofType(Types.providerOf(key.getTypeLiteral().getType()))));
               }
            }
         }

         this.bindings = ImmutableList.copyOf(bindings);
         this.dependencies = ImmutableSet.copyOf(dependencies);
         this.providerDependencies = ImmutableSet.copyOf(providerDependencies);
         this.permitDuplicates = permitsDuplicates(injector);
         // This is safe because all our dependencies are assignable to T and we never assign to
         // elements of this array.
         @SuppressWarnings("unchecked")
         SingleParameterInjector<T>[] typed =
                 (SingleParameterInjector<T>[]) injector.getParametersInjectors(dependencies, errors);
         this.parameterinjectors = typed;
         isInitialized = true;
      }

      boolean permitsDuplicates(Injector injector) {
         return injector.getBindings().containsKey(getPermitDuplicatesKey());
      }

      ImmutableList<Binding<T>> getBindings() {
         checkConfiguration(isInitialized, "not initialized");
         return bindings;
      }

      SingleParameterInjector<T>[] getParameterInjectors() {
         checkConfiguration(isInitialized, "not initialized");
         return parameterinjectors;
      }

      ImmutableSet<Dependency<?>> getDependencies() {
         return dependencies;
      }

      ImmutableSet<Dependency<?>> getProviderDependencies() {
         return providerDependencies;
      }

      String getSetName() {
         // lazily initialized since most selectors don't survive module installation.
         if (setName == null) {
            setName = Annotations.nameOf(setKey);
         }
         return setName;
      }

      Key<Boolean> getPermitDuplicatesKey() {
         Key<Boolean> local = permitDuplicatesKey;
         if (local == null) {
            local =
                    permitDuplicatesKey = Key.get(Boolean.class, named(toString() + " permits duplicates"));
         }
         return local;
      }

      Key<Collection<Provider<T>>> getCollectionOfProvidersKey() {
         Key<Collection<Provider<T>>> local = collectionOfProvidersKey;
         if (local == null) {
            local = collectionOfProvidersKey = setKey.ofType(collectionOfProvidersOf(elementType));
         }
         return local;
      }

      Key<Collection<javax.inject.Provider<T>>> getCollectionOfJavaxProvidersKey() {
         Key<Collection<javax.inject.Provider<T>>> local = collectionOfJavaxProvidersKey;
         if (local == null) {
            local =
                    collectionOfJavaxProvidersKey =
                            setKey.ofType(collectionOfJavaxProvidersOf(elementType));
         }
         return local;
      }

      boolean isInitialized() {
         return isInitialized;
      }

      // MultibinderBinding API methods

      TypeLiteral<T> getElementTypeLiteral() {
         return elementType;
      }

      Key<Set<T>> getSetKey() {
         return setKey;
      }

      @SuppressWarnings("unchecked")
      List<Binding<?>> getElements() {
         if (isInitialized()) {
            return (List<Binding<?>>) (List<?>) bindings; // safe because bindings is immutable.
         } else {
            throw new UnsupportedOperationException("getElements() not supported for module bindings");
         }
      }

      boolean permitsDuplicates() {
         if (isInitialized()) {
            return permitDuplicates;
         } else {
            throw new UnsupportedOperationException(
                    "permitsDuplicates() not supported for module bindings");
         }
      }

      boolean containsElement(com.google.inject.spi.Element element) {
         if (element instanceof Binding) {
            Binding<?> binding = (Binding<?>) element;
            return keyMatches(binding.getKey())
                    || binding.getKey().equals(getPermitDuplicatesKey())
                    || binding.getKey().equals(setKey)
                    || binding.getKey().equals(collectionOfProvidersKey)
                    || binding.getKey().equals(collectionOfJavaxProvidersKey);
         } else {
            return false;
         }
      }

      private boolean keyMatches(Key<?> key) {
         return key.getTypeLiteral().equals(elementType)
                 && key.getAnnotation() instanceof Element
                 && ((Element) key.getAnnotation()).setName().equals(getSetName())
                 && ((Element) key.getAnnotation()).type() == MULTIBINDER;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof BindingSelection) {
            return setKey.equals(((BindingSelection<?>) obj).setKey);
         }
         return false;
      }

      @Override
      public int hashCode() {
         return setKey.hashCode();
      }

      @Override
      public String toString() {
         return (getSetName().isEmpty() ? "" : getSetName() + " ")
                 + "Multibinder<"
                 + elementType
                 + ">";
      }
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof RealMultibinder
              && ((RealMultibinder<?>) o).bindingSelection.equals(bindingSelection);
   }

   @Override
   public int hashCode() {
      return bindingSelection.hashCode();
   }

   private static final class RealMultibinderCollectionOfProvidersProvider<T>
           extends InternalProviderInstanceBindingImpl.Factory<Collection<Provider<T>>> {

      private final BindingSelection<T> bindingSelection;
      private ImmutableList<Provider<T>> collectionOfProviders;

      RealMultibinderCollectionOfProvidersProvider(BindingSelection<T> bindingSelection) {
         super(InitializationTiming.DELAYED); // See comment in RealMultibinderProvider
         this.bindingSelection = bindingSelection;
      }

      @Override
      void initialize(InjectorImpl injector, Errors errors) throws ErrorsException {
         bindingSelection.initialize(injector, errors);
         ImmutableList.Builder<Provider<T>> providers = ImmutableList.builder();
         for (Binding<T> binding : bindingSelection.getBindings()) {
            providers.add(binding.getProvider());
         }
         this.collectionOfProviders = providers.build();
      }

      @Override
      protected Collection<Provider<T>> doProvision(
              InternalContext context, Dependency<?> dependency) {
         return collectionOfProviders;
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
         return bindingSelection.getProviderDependencies();
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof RealMultibinderCollectionOfProvidersProvider
                 && bindingSelection.equals(
                 ((RealMultibinderCollectionOfProvidersProvider<?>) obj).bindingSelection);
      }

      @Override
      public int hashCode() {
         return bindingSelection.hashCode();
      }
   }

   /**
    * We install the permit duplicates configuration as its own binding, all by itself. This way, if
    * only one of a multibinder's users remember to call permitDuplicates(), they're still permitted.
    *
    * <p>This is like setting a global variable in the injector so that each instance of the
    * multibinder will have the same value for permitDuplicates, even if it is only set on one of
    * them.
    */
   private static class PermitDuplicatesModule extends AbstractModule {
      private final Key<Boolean> key;

      PermitDuplicatesModule(Key<Boolean> key) {
         this.key = key;
      }

      @Override
      protected void configure() {
         bind(key).toInstance(true);
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof PermitDuplicatesModule && ((PermitDuplicatesModule) o).key.equals(key);
      }

      @Override
      public int hashCode() {
         return getClass().hashCode() ^ key.hashCode();
      }
   }
}
