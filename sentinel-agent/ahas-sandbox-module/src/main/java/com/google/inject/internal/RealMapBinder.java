
package com.google.inject.internal;

import static com.google.inject.internal.Element.Type.MAPBINDER;
import static com.google.inject.internal.Errors.checkConfiguration;
import static com.google.inject.internal.Errors.checkNotNull;
import static com.google.inject.internal.RealMultibinder.setOf;
import static com.google.inject.util.Types.newParameterizedType;
import static com.google.inject.util.Types.newParameterizedTypeWithOwner;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.internal.InternalProviderInstanceBindingImpl.InitializationTiming;
import com.google.inject.multibindings.MapBinderBinding;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.MultibindingsTargetVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Element;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The actual mapbinder plays several roles:
 *
 * <p>As a MapBinder, it acts as a factory for LinkedBindingBuilders for each of the map's values.
 * It delegates to a {@link Multibinder} of entries (keys to value providers).
 *
 * <p>As a Module, it installs the binding to the map itself, as well as to a corresponding map
 * whose values are providers.
 *
 * <p>As a module, this implements equals() and hashcode() in order to trick Guice into executing
 * its configure() method only once. That makes it so that multiple mapbinders can be created for
 * the same target map, but only one is bound. Since the list of bindings is retrieved from the
 * injector itself (and not the mapbinder), each mapbinder has access to all contributions from all
 * equivalent mapbinders.
 *
 * <p>Rather than binding a single Map.Entry&lt;K, V&gt;, the map binder binds keys and values
 * independently. This allows the values to be properly scoped.
 */
public final class RealMapBinder<K, V> implements Module {

   /**
    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a {@link
    * Map} that is itself bound with no binding annotation.
    */
   public static <K, V> RealMapBinder<K, V> newMapRealBinder(
           Binder binder, TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      binder = binder.skipSources(RealMapBinder.class);
      return newRealMapBinder(
              binder,
              keyType,
              valueType,
              Key.get(mapOf(keyType, valueType)),
              RealMultibinder.newRealSetBinder(binder, Key.get(entryOfProviderOf(keyType, valueType))));
   }

   /**
    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a {@link
    * Map} that is itself bound with {@code annotation}.
    */
   public static <K, V> RealMapBinder<K, V> newRealMapBinder(
           Binder binder, TypeLiteral<K> keyType, TypeLiteral<V> valueType, Annotation annotation) {
      binder = binder.skipSources(RealMapBinder.class);
      return newRealMapBinder(
              binder,
              keyType,
              valueType,
              Key.get(mapOf(keyType, valueType), annotation),
              RealMultibinder.newRealSetBinder(
                      binder, Key.get(entryOfProviderOf(keyType, valueType), annotation)));
   }

   /**
    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a {@link
    * Map} that is itself bound with {@code annotationType}.
    */
   public static <K, V> RealMapBinder<K, V> newRealMapBinder(
           Binder binder,
           TypeLiteral<K> keyType,
           TypeLiteral<V> valueType,
           Class<? extends Annotation> annotationType) {
      binder = binder.skipSources(RealMapBinder.class);
      return newRealMapBinder(
              binder,
              keyType,
              valueType,
              Key.get(mapOf(keyType, valueType), annotationType),
              RealMultibinder.newRealSetBinder(
                      binder, Key.get(entryOfProviderOf(keyType, valueType), annotationType)));
   }

   @SuppressWarnings("unchecked") // a map of <K, V> is safely a Map<K, V>
   static <K, V> TypeLiteral<Map<K, V>> mapOf(TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map<K, V>>)
              TypeLiteral.get(Types.mapOf(keyType.getType(), valueType.getType()));
   }

   @SuppressWarnings("unchecked") // a provider map <K, V> is safely a Map<K, Provider<V>>
   static <K, V> TypeLiteral<Map<K, Provider<V>>> mapOfProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map<K, Provider<V>>>)
              TypeLiteral.get(Types.mapOf(keyType.getType(), Types.providerOf(valueType.getType())));
   }

   // provider map <K, V> is safely a Map<K, javax.inject.Provider<V>>>
   @SuppressWarnings("unchecked")
   static <K, V> TypeLiteral<Map<K, javax.inject.Provider<V>>> mapOfJavaxProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map<K, javax.inject.Provider<V>>>)
              TypeLiteral.get(
                      Types.mapOf(
                              keyType.getType(),
                              newParameterizedType(javax.inject.Provider.class, valueType.getType())));
   }

   @SuppressWarnings("unchecked") // a provider map <K, Set<V>> is safely a Map<K, Set<Provider<V>>>
   static <K, V> TypeLiteral<Map<K, Set<Provider<V>>>> mapOfSetOfProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map<K, Set<Provider<V>>>>)
              TypeLiteral.get(
                      Types.mapOf(keyType.getType(), Types.setOf(Types.providerOf(valueType.getType()))));
   }

   @SuppressWarnings("unchecked") // a provider map <K, Set<V>> is safely a Map<K, Set<Provider<V>>>
   static <K, V> TypeLiteral<Map<K, Set<javax.inject.Provider<V>>>> mapOfSetOfJavaxProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map<K, Set<javax.inject.Provider<V>>>>)
              TypeLiteral.get(
                      Types.mapOf(
                              keyType.getType(), Types.setOf(Types.javaxProviderOf(valueType.getType()))));
   }

   @SuppressWarnings("unchecked") // a provider map <K, Set<V>> is safely a Map<K, Set<Provider<V>>>
   static <K, V> TypeLiteral<Map<K, Collection<Provider<V>>>> mapOfCollectionOfProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map<K, Collection<Provider<V>>>>)
              TypeLiteral.get(
                      Types.mapOf(
                              keyType.getType(), Types.collectionOf(Types.providerOf(valueType.getType()))));
   }

   @SuppressWarnings("unchecked") // a provider map <K, Set<V>> is safely a Map<K, Set<Provider<V>>>
   static <K, V>
   TypeLiteral<Map<K, Collection<javax.inject.Provider<V>>>> mapOfCollectionOfJavaxProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map<K, Collection<javax.inject.Provider<V>>>>)
              TypeLiteral.get(
                      Types.mapOf(
                              keyType.getType(), Types.collectionOf(Types.javaxProviderOf(valueType.getType()))));
   }

   @SuppressWarnings("unchecked") // a provider entry <K, V> is safely a Map.Entry<K, Provider<V>>
   static <K, V> TypeLiteral<Map.Entry<K, Provider<V>>> entryOfProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map.Entry<K, Provider<V>>>)
              TypeLiteral.get(
                      newParameterizedTypeWithOwner(
                              Map.class,
                              Map.Entry.class,
                              keyType.getType(),
                              Types.providerOf(valueType.getType())));
   }

   @SuppressWarnings("unchecked") // a provider entry <K, V> is safely a Map.Entry<K, Provider<V>>
   static <K, V> TypeLiteral<Map.Entry<K, Provider<V>>> entryOfJavaxProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Map.Entry<K, Provider<V>>>)
              TypeLiteral.get(
                      newParameterizedTypeWithOwner(
                              Map.class,
                              Map.Entry.class,
                              keyType.getType(),
                              Types.javaxProviderOf(valueType.getType())));
   }

   @SuppressWarnings("unchecked") // a provider entry <K, V> is safely a Map.Entry<K, Provider<V>>
   static <K, V>
   TypeLiteral<Set<Map.Entry<K, javax.inject.Provider<V>>>> setOfEntryOfJavaxProviderOf(
           TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
      return (TypeLiteral<Set<Map.Entry<K, javax.inject.Provider<V>>>>)
              TypeLiteral.get(Types.setOf(entryOfJavaxProviderOf(keyType, valueType).getType()));
   }

   /** Given a Key<T> will return a Key<Provider<T>> */
   @SuppressWarnings("unchecked")
   private static <T> Key<Provider<T>> getKeyOfProvider(Key<T> valueKey) {
      return (Key<Provider<T>>)
              valueKey.ofType(Types.providerOf(valueKey.getTypeLiteral().getType()));
   }

   // Note: We use valueTypeAndAnnotation effectively as a Pair<TypeLiteral, Annotation|Class>
   // since it's an easy way to group a type and an optional annotation type or instance.
   static <K, V> RealMapBinder<K, V> newRealMapBinder(
           Binder binder, TypeLiteral<K> keyType, Key<V> valueTypeAndAnnotation) {
      binder = binder.skipSources(RealMapBinder.class);
      TypeLiteral<V> valueType = valueTypeAndAnnotation.getTypeLiteral();
      return newRealMapBinder(
              binder,
              keyType,
              valueType,
              valueTypeAndAnnotation.ofType(mapOf(keyType, valueType)),
              RealMultibinder.newRealSetBinder(
                      binder, valueTypeAndAnnotation.ofType(entryOfProviderOf(keyType, valueType))));
   }

   private static <K, V> RealMapBinder<K, V> newRealMapBinder(
           Binder binder,
           TypeLiteral<K> keyType,
           TypeLiteral<V> valueType,
           Key<Map<K, V>> mapKey,
           RealMultibinder<Map.Entry<K, Provider<V>>> entrySetBinder) {
      RealMapBinder<K, V> mapBinder =
              new RealMapBinder<K, V>(binder, keyType, valueType, mapKey, entrySetBinder);
      binder.install(mapBinder);
      return mapBinder;
   }

   // Until the injector initializes us, we don't know what our dependencies are,
   // so initialize to the whole Injector.
   private static final ImmutableSet<Dependency<?>> MODULE_DEPENDENCIES =
           ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(Injector.class)));

   private final BindingSelection<K, V> bindingSelection;
   private final Binder binder;

   private final RealMultibinder<Map.Entry<K, Provider<V>>> entrySetBinder;

   private RealMapBinder(
           Binder binder,
           TypeLiteral<K> keyType,
           TypeLiteral<V> valueType,
           Key<Map<K, V>> mapKey,
           RealMultibinder<Map.Entry<K, Provider<V>>> entrySetBinder) {
      this.bindingSelection = new BindingSelection<>(keyType, valueType, mapKey, entrySetBinder);
      this.binder = binder;
      this.entrySetBinder = entrySetBinder;
   }

   public void permitDuplicates() {
      checkConfiguration(!bindingSelection.isInitialized(), "MapBinder was already initialized");
      entrySetBinder.permitDuplicates();
      binder.install(new MultimapBinder<K, V>(bindingSelection));
   }

   /** Adds a binding to the map for the given key. */
   Key<V> getKeyForNewValue(K key) {
      checkNotNull(key, "key");
      checkConfiguration(!bindingSelection.isInitialized(), "MapBinder was already initialized");
      RealMultibinder<Map.Entry<K, Provider<V>>> entrySetBinder =
              bindingSelection.getEntrySetBinder();

      Key<V> valueKey =
              Key.get(
                      bindingSelection.getValueType(),
                      new RealElement(
                              entrySetBinder.getSetName(), MAPBINDER, bindingSelection.getKeyType().toString()));
      entrySetBinder.addBinding().toProvider(new ProviderMapEntry<K, V>(key, valueKey));
      return valueKey;
   }

   /**
    * This creates two bindings. One for the {@code Map.Entry<K, Provider<V>>} and another for {@code
    * V}.
    */
   public LinkedBindingBuilder<V> addBinding(K key) {
      return binder.bind(getKeyForNewValue(key));
   }

   @Override
   public void configure(Binder binder) {
      checkConfiguration(!bindingSelection.isInitialized(), "MapBinder was already initialized");

      // Binds a Map<K, Provider<V>>
      RealProviderMapProvider<K, V> providerMapProvider =
              new RealProviderMapProvider<K, V>(bindingSelection);
      binder.bind(bindingSelection.getProviderMapKey()).toProvider(providerMapProvider);

      // The map this exposes is internally an ImmutableMap, so it's OK to massage
      // the guice Provider to javax Provider in the value (since Guice provider
      // implements javax Provider).
      @SuppressWarnings({"unchecked", "rawtypes"})
      Provider<Map<K, javax.inject.Provider<V>>> javaxProviderMapProvider =
              (Provider) providerMapProvider;
      binder.bind(bindingSelection.getJavaxProviderMapKey()).toProvider(javaxProviderMapProvider);

      RealMapProvider<K, V> mapProvider = new RealMapProvider<>(bindingSelection);
      binder.bind(bindingSelection.getMapKey()).toProvider(mapProvider);

      // The Map.Entries are all ProviderMapEntry instances which do not allow setValue, so it is
      // safe to massage the return type like this
      @SuppressWarnings({"unchecked", "rawtypes"})
      Key<Set<Map.Entry<K, javax.inject.Provider<V>>>> massagedEntrySetProviderKey =
              (Key) bindingSelection.getEntrySetBinder().getSetKey();
      binder.bind(bindingSelection.getEntrySetJavaxProviderKey()).to(massagedEntrySetProviderKey);
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof RealMapBinder
              && ((RealMapBinder<?, ?>) o).bindingSelection.equals(bindingSelection);
   }

   @Override
   public int hashCode() {
      return bindingSelection.hashCode();
   }

   /**
    * The BindingSelection contains some of the core state and logic for the MapBinder.
    *
    * <p>It lazily computes the value for keys for various permutations of Maps that are provided by
    * this module. It also builds up maps from {@code K} to {@code Binding<V>}, which is used by all
    * of the internal factories to actually provide the desired maps.
    *
    * <p>During initialization time there is only one BindingSelection. It is possible that multiple
    * different BindingSelections are constructed. Specifically, in the case of two different modules
    * each adding bindings to the same MapBinder. If that happens, we define the BindingSelection
    * held by the {@link RealMapProvider} to be the authoritative one. The logic for this exists in
    * {@link RealMultimapBinderProviderWithDependencies}. This is done to avoid confusion because the
    * BindingSelection contains mutable state.
    */
   private static final class BindingSelection<K, V> {
      private enum InitializationState {
         UNINITIALIZED,
         INITIALIZED,
         HAS_ERRORS;
      }

      private final TypeLiteral<K> keyType;
      private final TypeLiteral<V> valueType;
      private final Key<Map<K, V>> mapKey;

      // Lazily computed
      private Key<Map<K, javax.inject.Provider<V>>> javaxProviderMapKey;
      private Key<Map<K, Provider<V>>> providerMapKey;
      private Key<Map<K, Set<V>>> multimapKey;
      private Key<Map<K, Set<Provider<V>>>> providerSetMultimapKey;
      private Key<Map<K, Set<javax.inject.Provider<V>>>> javaxProviderSetMultimapKey;
      private Key<Map<K, Collection<Provider<V>>>> providerCollectionMultimapKey;
      private Key<Map<K, Collection<javax.inject.Provider<V>>>> javaxProviderCollectionMultimapKey;
      private Key<Set<Map.Entry<K, javax.inject.Provider<V>>>> entrySetJavaxProviderKey;

      private final RealMultibinder<Map.Entry<K, Provider<V>>> entrySetBinder;

      private InitializationState initializationState;

      /**
       * These are built during initialization and used by all factories to actually provide the
       * relevant maps. These contain all of the necessary information about the map binder.
       */
      private ImmutableMap<K, Binding<V>> mapBindings;

      private ImmutableMap<K, Set<Binding<V>>> multimapBindings;
      private ImmutableList<Map.Entry<K, Binding<V>>> entries;

      /**
       * Indicates if this Map permits duplicates. It is initialized during initialization by querying
       * the injector. This is done because multiple different modules can contribute to a MapBinder,
       * and any one could set permitDuplicates.
       */
      private boolean permitsDuplicates;

      private BindingSelection(
              TypeLiteral<K> keyType,
              TypeLiteral<V> valueType,
              Key<Map<K, V>> mapKey,
              RealMultibinder<Map.Entry<K, Provider<V>>> entrySetBinder) {
         this.keyType = keyType;
         this.valueType = valueType;
         this.mapKey = mapKey;
         this.entrySetBinder = entrySetBinder;
         this.initializationState = InitializationState.UNINITIALIZED;
      }

      /**
       * Will initialize internal data structures.
       *
       * @return {@code true} if initialization was successful, {@code false} if there were errors
       */
      private boolean tryInitialize(InjectorImpl injector, Errors errors) {
         // Every one of our providers will call this method, so only execute the logic once.
         if (initializationState != InitializationState.UNINITIALIZED) {
            return initializationState != InitializationState.HAS_ERRORS;
         }

         // Multiple different modules can all contribute to the same MapBinder, and if any
         // one of them permits duplicates, then the map binder as a whole will permit duplicates.
         // Since permitDuplicates() may not have been called on this instance, we need to go
         // to the injector to see if permitDuplicates was set.
         permitsDuplicates = entrySetBinder.permitsDuplicates(injector);

         // We now build the Map<K, Set<Binding<V>>> from the entrySetBinder.
         // The entrySetBinder contains all of the ProviderMapEntrys, and once
         // we have those, it's easy to iterate through them to organize them by K.
         Map<K, ImmutableSet.Builder<Binding<V>>> bindingMultimapMutable =
                 new LinkedHashMap<K, ImmutableSet.Builder<Binding<V>>>();
         Map<K, Binding<V>> bindingMapMutable = new LinkedHashMap<>();
         Multimap<K, Indexer.IndexedBinding> index = HashMultimap.create();
         Indexer indexer = new Indexer(injector);
         Multimap<K, Binding<V>> duplicates = null;

         ImmutableList.Builder<Map.Entry<K, Binding<V>>> entriesBuilder = ImmutableList.builder();

         // We get all of the Bindings that were put into the entrySetBinder
         for (Binding<Map.Entry<K, Provider<V>>> binding :
                 injector.findBindingsByType(entrySetBinder.getElementTypeLiteral())) {
            if (entrySetBinder.containsElement(binding)) {

               // Protected by findBindingByType() and the fact that all providers are added by us
               // in addBinding(). It would theoretically be possible for someone to directly
               // add their own binding to the entrySetBinder, but they shouldn't do that.
               @SuppressWarnings({"unchecked", "rawtypes"})
               ProviderInstanceBinding<ProviderMapEntry<K, V>> entryBinding =
                       (ProviderInstanceBinding) binding;

               // We added all these bindings initially, so we know they are ProviderMapEntrys
               @SuppressWarnings({"unchecked", "rawtypes"})
               ProviderMapEntry<K, V> entry = (ProviderMapEntry) entryBinding.getUserSuppliedProvider();
               K key = entry.getKey();

               Key<V> valueKey = entry.getValueKey();
               Binding<V> valueBinding = injector.getExistingBinding(valueKey);

               // Use the indexer to de-dupe user bindings. This is needed because of the
               // uniqueId in RealElement. The uniqueId intentionally circumvents the regular
               // Guice deduplication, so we need to re-implement our own here, ignoring
               // uniqueId.
               if (index.put(key, valueBinding.acceptTargetVisitor(indexer))) {

                  entriesBuilder.add(Maps.immutableEntry(key, valueBinding));

                  Binding<V> previous = bindingMapMutable.put(key, valueBinding);
                  // Check if this is a duplicate binding
                  if (previous != null && !permitsDuplicates) {
                     if (duplicates == null) {
                        // This is linked for both keys and values to maintain order
                        duplicates = LinkedHashMultimap.create();
                     }

                     // We add both the previous and the current value to the duplicates map.
                     // This is because if there are three duplicates, we will only execute this code
                     // for the second and third, but we want all three values to display a helpful
                     // error message. We rely on the multimap to dedupe repeated values.
                     duplicates.put(key, previous);
                     duplicates.put(key, valueBinding);
                  }

                  // Don't do extra work unless we need to
                  if (permitsDuplicates) {
                     // Create a set builder for this key if it's the first time we've seen it
                     if (!bindingMultimapMutable.containsKey(key)) {
                        bindingMultimapMutable.put(key, ImmutableSet.<Binding<V>>builder());
                     }

                     // Add the Binding<V>
                     bindingMultimapMutable.get(key).add(valueBinding);
                  }
               }
            }
         }

         // It is safe to check if duplicates is non-null because if duplicates are allowed,
         // we don't build up this data structure
         if (duplicates != null) {
            initializationState = InitializationState.HAS_ERRORS;
            reportDuplicateKeysError(duplicates, errors);

            return false;
         }

         // Build all of the ImmutableSet.Builders,
         // transforming from Map<K, ImmutableSet.Builder<Binding<V>>> to
         // ImmutableMap<K, Set<Binding<V>>>
         ImmutableMap.Builder<K, Set<Binding<V>>> bindingsMultimapBuilder = ImmutableMap.builder();
         for (Map.Entry<K, ImmutableSet.Builder<Binding<V>>> entry :
                 bindingMultimapMutable.entrySet()) {
            bindingsMultimapBuilder.put(entry.getKey(), entry.getValue().build());
         }
         mapBindings = ImmutableMap.copyOf(bindingMapMutable);
         multimapBindings = bindingsMultimapBuilder.build();

         entries = entriesBuilder.build();

         initializationState = InitializationState.INITIALIZED;

         return true;
      }

      private static <K, V> void reportDuplicateKeysError(
              Multimap<K, Binding<V>> duplicates, Errors errors) {
         StringBuilder sb = new StringBuilder("Map injection failed due to duplicated key ");
         boolean first = true;
         for (Map.Entry<K, Collection<Binding<V>>> entry : duplicates.asMap().entrySet()) {
            K dupKey = entry.getKey();

            if (first) {
               first = false;
               sb.append("\"" + dupKey + "\", from bindings:\n");
            } else {
               sb.append("\n and key: \"" + dupKey + "\", from bindings:\n");
            }

            for (Binding<V> dup : entry.getValue()) {
               sb.append("\t at " + Errors.convert(dup.getSource()) + "\n");
            }
         }

         // TODO(user): Add a different error for every duplicated key
         errors.addMessage(sb.toString());
      }

      private boolean containsElement(Element element) {
         if (entrySetBinder.containsElement(element)) {
            return true;
         }

         Key<?> key;
         if (element instanceof Binding) {
            key = ((Binding<?>) element).getKey();
         } else {
            return false; // cannot match;
         }

         return key.equals(getMapKey())
                 || key.equals(getProviderMapKey())
                 || key.equals(getJavaxProviderMapKey())
                 || key.equals(getMultimapKey())
                 || key.equals(getProviderSetMultimapKey())
                 || key.equals(getJavaxProviderSetMultimapKey())
                 || key.equals(getProviderCollectionMultimapKey())
                 || key.equals(getJavaxProviderCollectionMultimapKey())
                 || key.equals(entrySetBinder.getSetKey())
                 || key.equals(getEntrySetJavaxProviderKey())
                 || matchesValueKey(key);
      }

      /** Returns true if the key indicates this is a value in the map. */
      private boolean matchesValueKey(Key<?> key) {
         return key.getAnnotation() instanceof RealElement
                 && ((RealElement) key.getAnnotation()).setName().equals(entrySetBinder.getSetName())
                 && ((RealElement) key.getAnnotation()).type() == MAPBINDER
                 && ((RealElement) key.getAnnotation()).keyType().equals(keyType.toString())
                 && key.getTypeLiteral().equals(valueType);
      }

      private Key<Map<K, Provider<V>>> getProviderMapKey() {
         Key<Map<K, Provider<V>>> local = providerMapKey;
         if (local == null) {
            local = providerMapKey = mapKey.ofType(mapOfProviderOf(keyType, valueType));
         }
         return local;
      }

      private Key<Map<K, javax.inject.Provider<V>>> getJavaxProviderMapKey() {
         Key<Map<K, javax.inject.Provider<V>>> local = javaxProviderMapKey;
         if (local == null) {
            local = javaxProviderMapKey = mapKey.ofType(mapOfJavaxProviderOf(keyType, valueType));
         }
         return local;
      }

      private Key<Map<K, Set<V>>> getMultimapKey() {
         Key<Map<K, Set<V>>> local = multimapKey;
         if (local == null) {
            local = multimapKey = mapKey.ofType(mapOf(keyType, setOf(valueType)));
         }
         return local;
      }

      private Key<Map<K, Set<Provider<V>>>> getProviderSetMultimapKey() {
         Key<Map<K, Set<Provider<V>>>> local = providerSetMultimapKey;
         if (local == null) {
            local = providerSetMultimapKey = mapKey.ofType(mapOfSetOfProviderOf(keyType, valueType));
         }
         return local;
      }

      private Key<Map<K, Set<javax.inject.Provider<V>>>> getJavaxProviderSetMultimapKey() {
         Key<Map<K, Set<javax.inject.Provider<V>>>> local = javaxProviderSetMultimapKey;
         if (local == null) {
            local =
                    javaxProviderSetMultimapKey =
                            mapKey.ofType(mapOfSetOfJavaxProviderOf(keyType, valueType));
         }
         return local;
      }

      private Key<Map<K, Collection<Provider<V>>>> getProviderCollectionMultimapKey() {
         Key<Map<K, Collection<Provider<V>>>> local = providerCollectionMultimapKey;
         if (local == null) {
            local =
                    providerCollectionMultimapKey =
                            mapKey.ofType(mapOfCollectionOfProviderOf(keyType, valueType));
         }
         return local;
      }

      private Key<Map<K, Collection<javax.inject.Provider<V>>>>
      getJavaxProviderCollectionMultimapKey() {
         Key<Map<K, Collection<javax.inject.Provider<V>>>> local = javaxProviderCollectionMultimapKey;
         if (local == null) {
            local =
                    javaxProviderCollectionMultimapKey =
                            mapKey.ofType(mapOfCollectionOfJavaxProviderOf(keyType, valueType));
         }
         return local;
      }

      private Key<Set<Map.Entry<K, javax.inject.Provider<V>>>> getEntrySetJavaxProviderKey() {
         Key<Set<Map.Entry<K, javax.inject.Provider<V>>>> local = entrySetJavaxProviderKey;
         if (local == null) {
            local =
                    entrySetJavaxProviderKey =
                            mapKey.ofType(setOfEntryOfJavaxProviderOf(keyType, valueType));
         }
         return local;
      }

      private ImmutableMap<K, Binding<V>> getMapBindings() {
         checkConfiguration(isInitialized(), "MapBinder has not yet been initialized");
         return mapBindings;
      }

      private ImmutableMap<K, Set<Binding<V>>> getMultimapBindings() {
         checkConfiguration(isInitialized(), "MapBinder has not yet been initialized");
         return multimapBindings;
      }

      private ImmutableList<Map.Entry<K, Binding<V>>> getEntries() {
         checkConfiguration(isInitialized(), "MapBinder has not yet been initialized");
         return entries;
      }

      private boolean isInitialized() {
         return initializationState == InitializationState.INITIALIZED;
      }

      private TypeLiteral<K> getKeyType() {
         return keyType;
      }

      private TypeLiteral<V> getValueType() {
         return valueType;
      }

      private Key<Map<K, V>> getMapKey() {
         return mapKey;
      }

      private RealMultibinder<Map.Entry<K, Provider<V>>> getEntrySetBinder() {
         return entrySetBinder;
      }

      private boolean permitsDuplicates() {
         if (isInitialized()) {
            return permitsDuplicates;
         } else {
            throw new UnsupportedOperationException(
                    "permitsDuplicates() not supported for module bindings");
         }
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof BindingSelection && ((BindingSelection<?, ?>) o).mapKey.equals(mapKey);
      }

      @Override
      public int hashCode() {
         return mapKey.hashCode();
      }
   }

   private static final class RealProviderMapProvider<K, V>
           extends RealMapBinderProviderWithDependencies<K, V, Map<K, Provider<V>>> {
      private Map<K, Provider<V>> mapOfProviders;
      private Set<Dependency<?>> dependencies = RealMapBinder.MODULE_DEPENDENCIES;

      private RealProviderMapProvider(BindingSelection<K, V> bindingSelection) {
         super(bindingSelection);
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
         return dependencies;
      }

      @Override
      protected void doInitialize(InjectorImpl injector, Errors errors) {
         ImmutableMap.Builder<K, Provider<V>> mapOfProvidersBuilder = ImmutableMap.builder();
         ImmutableSet.Builder<Dependency<?>> dependenciesBuilder = ImmutableSet.builder();
         for (Map.Entry<K, Binding<V>> entry : bindingSelection.getMapBindings().entrySet()) {
            mapOfProvidersBuilder.put(entry.getKey(), entry.getValue().getProvider());
            dependenciesBuilder.add(Dependency.get(getKeyOfProvider(entry.getValue().getKey())));
         }

         mapOfProviders = mapOfProvidersBuilder.build();
         dependencies = dependenciesBuilder.build();
      }

      @Override
      protected Map<K, Provider<V>> doProvision(InternalContext context, Dependency<?> dependency) {
         return mapOfProviders;
      }
   }

   private static final class RealMapProvider<K, V>
           extends RealMapBinderProviderWithDependencies<K, V, Map<K, V>>
           implements ProviderWithExtensionVisitor<Map<K, V>>, MapBinderBinding<Map<K, V>> {
      private Set<Dependency<?>> dependencies = RealMapBinder.MODULE_DEPENDENCIES;

      /**
       * An array of all the injectors.
       *
       * <p>This is parallel to array of keys below
       */
      private SingleParameterInjector<V>[] injectors;

      private K[] keys;

      private RealMapProvider(BindingSelection<K, V> bindingSelection) {
         super(bindingSelection);
      }

      private BindingSelection<K, V> getBindingSelection() {
         return bindingSelection;
      }

      @Override
      protected void doInitialize(InjectorImpl injector, Errors errors) throws ErrorsException {
         @SuppressWarnings("unchecked")
         K[] keysArray = (K[]) new Object[bindingSelection.getMapBindings().size()];
         keys = keysArray;
         ImmutableSet.Builder<Dependency<?>> dependenciesBuilder = ImmutableSet.builder();
         int i = 0;
         for (Map.Entry<K, Binding<V>> entry : bindingSelection.getMapBindings().entrySet()) {
            dependenciesBuilder.add(Dependency.get(entry.getValue().getKey()));
            keys[i] = entry.getKey();
            i++;
         }

         ImmutableSet<Dependency<?>> localDependencies = dependenciesBuilder.build();
         dependencies = localDependencies;

         List<Dependency<?>> dependenciesList = localDependencies.asList();

         // We know the type because we built up our own sets of dependencies, it's just
         // that the interface uses a "?" generic
         @SuppressWarnings("unchecked")
         SingleParameterInjector<V>[] typedInjectors =
                 (SingleParameterInjector<V>[]) injector.getParametersInjectors(dependenciesList, errors);
         injectors = typedInjectors;
      }

      @Override
      protected Map<K, V> doProvision(InternalContext context, Dependency<?> dependency)
              throws InternalProvisionException {
         SingleParameterInjector<V>[] localInjectors = injectors;
         if (localInjectors == null) {
            // if injectors == null, then we have no bindings so return the empty map.
            return ImmutableMap.of();
         }

         ImmutableMap.Builder<K, V> resultBuilder = ImmutableMap.builder();
         K[] localKeys = keys;
         for (int i = 0; i < localInjectors.length; i++) {
            SingleParameterInjector<V> injector = localInjectors[i];
            K key = localKeys[i];

            V value = injector.inject(context);

            if (value == null) {
               throw createNullValueException(key, bindingSelection.getMapBindings().get(key));
            }

            resultBuilder.put(key, value);
         }

         return resultBuilder.build();
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
         return dependencies;
      }

      @Override
      @SuppressWarnings("unchecked")
      public <B, W> W acceptExtensionVisitor(
              BindingTargetVisitor<B, W> visitor, ProviderInstanceBinding<? extends B> binding) {
         if (visitor instanceof MultibindingsTargetVisitor) {
            return ((MultibindingsTargetVisitor<Map<K, V>, W>) visitor).visit(this);
         } else {
            return visitor.visit(binding);
         }
      }

      @Override
      public Key<Map<K, V>> getMapKey() {
         return bindingSelection.getMapKey();
      }

      @Override
      public TypeLiteral<K> getKeyTypeLiteral() {
         return bindingSelection.getKeyType();
      }

      @Override
      public TypeLiteral<V> getValueTypeLiteral() {
         return bindingSelection.getValueType();
      }

      @Override
      @SuppressWarnings("unchecked")
      public List<Map.Entry<?, Binding<?>>> getEntries() {
         if (bindingSelection.isInitialized()) {
            return (List<Map.Entry<?, Binding<?>>>) (List<?>) bindingSelection.getEntries();
         } else {
            throw new UnsupportedOperationException("getEntries() not supported for module bindings");
         }
      }

      @Override
      public List<Map.Entry<?, Binding<?>>> getEntries(Iterable<? extends Element> elements) {
         // Iterate over the elements, building up the below maps
         // This is a preprocessing step allowing us to only iterate over elements
         // once and have O(n) runtime
         ImmutableMultimap.Builder<K, Key<V>> keyToValueKeyBuilder = ImmutableMultimap.builder();
         ImmutableMap.Builder<Key<V>, Binding<V>> valueKeyToBindingBuilder = ImmutableMap.builder();
         ImmutableMap.Builder<Key<V>, K> valueKeyToKeyBuilder = ImmutableMap.builder();
         ImmutableMap.Builder<Key<V>, Binding<Map.Entry<K, Provider<V>>>>
                 valueKeyToEntryBindingBuilder = ImmutableMap.builder();
         for (Element element : elements) {
            if (element instanceof Binding) {
               Binding<?> binding = (Binding<?>) element;
               if (bindingSelection.matchesValueKey(binding.getKey())
                       && binding.getKey().getTypeLiteral().equals(bindingSelection.valueType)) {
                  // Safe because of the check on the type literal above
                  @SuppressWarnings("unchecked")
                  Binding<V> typedBinding = (Binding<V>) binding;
                  Key<V> typedKey = typedBinding.getKey();
                  valueKeyToBindingBuilder.put(typedKey, typedBinding);
               }
            }

            if (element instanceof ProviderInstanceBinding
                    && bindingSelection.getEntrySetBinder().containsElement(element)) {
               // Safe because of the instanceof check, and containsElement() check
               @SuppressWarnings({"unchecked", "rawtypes"})
               ProviderInstanceBinding<Map.Entry<K, Provider<V>>> entryBinding =
                       (ProviderInstanceBinding) element;

               // Safe because of the check for containsElement() above
               @SuppressWarnings("unchecked")
               Provider<Map.Entry<K, Provider<V>>> typedProvider =
                       (Provider<Map.Entry<K, Provider<V>>>) entryBinding.getUserSuppliedProvider();
               Provider<Map.Entry<K, Provider<V>>> userSuppliedProvider = typedProvider;

               if (userSuppliedProvider instanceof ProviderMapEntry) {
                  // Safe because of the instanceof check
                  @SuppressWarnings("unchecked")
                  ProviderMapEntry<K, V> typedUserSuppliedProvider =
                          (ProviderMapEntry<K, V>) userSuppliedProvider;
                  ProviderMapEntry<K, V> entry = typedUserSuppliedProvider;

                  keyToValueKeyBuilder.put(entry.getKey(), entry.getValueKey());
                  valueKeyToEntryBindingBuilder.put(entry.getValueKey(), entryBinding);
                  valueKeyToKeyBuilder.put(entry.getValueKey(), entry.getKey());
               }
            }
         }

         ImmutableMultimap<K, Key<V>> keyToValueKey = keyToValueKeyBuilder.build();
         ImmutableMap<Key<V>, K> valueKeyToKey = valueKeyToKeyBuilder.build();
         ImmutableMap<Key<V>, Binding<V>> valueKeyToBinding = valueKeyToBindingBuilder.build();
         ImmutableMap<Key<V>, Binding<Map.Entry<K, Provider<V>>>> valueKeyToEntryBinding =
                 valueKeyToEntryBindingBuilder.build();

         // Check that there is a 1:1 mapping from keys from the ProviderMapEntrys to the
         // keys from the Bindings.
         Set<Key<V>> keysFromProviderMapEntrys = Sets.newHashSet(keyToValueKey.values());
         Set<Key<V>> keysFromBindings = valueKeyToBinding.keySet();

         if (!keysFromProviderMapEntrys.equals(keysFromBindings)) {
            Set<Key<V>> keysOnlyFromProviderMapEntrys =
                    Sets.difference(keysFromProviderMapEntrys, keysFromBindings);
            Set<Key<V>> keysOnlyFromBindings =
                    Sets.difference(keysFromBindings, keysFromProviderMapEntrys);

            StringBuilder sb = new StringBuilder("Expected a 1:1 mapping from map keys to values.");

            if (!keysOnlyFromBindings.isEmpty()) {
               sb.append(
                       Errors.format("%nFound these Bindings that were missing an associated entry:%n"));
               for (Key<V> key : keysOnlyFromBindings) {
                  sb.append(
                          Errors.format("  %s bound at: %s%n", key, valueKeyToBinding.get(key).getSource()));
               }
            }

            if (!keysOnlyFromProviderMapEntrys.isEmpty()) {
               sb.append(Errors.format("%nFound these map keys without a corresponding value:%n"));
               for (Key<V> key : keysOnlyFromProviderMapEntrys) {
                  sb.append(
                          Errors.format(
                                  "  '%s' bound at: %s%n",
                                  valueKeyToKey.get(key), valueKeyToEntryBinding.get(key).getSource()));
               }
            }

            throw new IllegalArgumentException(sb.toString());
         }

         // Now that we have the two maps, generate the result map
         ImmutableList.Builder<Map.Entry<?, Binding<?>>> resultBuilder = ImmutableList.builder();
         for (Map.Entry<K, Key<V>> entry : keyToValueKey.entries()) {
            Binding<?> binding = valueKeyToBinding.get(entry.getValue());
            // No null check for binding needed because of the above check to make sure all the
            // values in keyToValueKey are present as keys in valueKeyToBinding

            @SuppressWarnings({"unchecked", "rawtypes"})
            Map.Entry<?, Binding<?>> newEntry =
                    (Map.Entry) Maps.immutableEntry(entry.getKey(), binding);
            resultBuilder.add(newEntry);
         }
         return resultBuilder.build();
      }

      @Override
      public boolean permitsDuplicates() {
         if (bindingSelection.isInitialized()) {
            return bindingSelection.permitsDuplicates();
         } else {
            throw new UnsupportedOperationException(
                    "permitsDuplicates() not supported for module bindings");
         }
      }

      @Override
      public boolean containsElement(Element element) {
         return bindingSelection.containsElement(element);
      }
   }

   /**
    * Binds {@code Map<K, Set<V>>} and {{@code Map<K, Set<Provider<V>>>}.
    *
    * <p>This will only exist if permitDuplicates() is called.
    */
   private static final class MultimapBinder<K, V> implements Module {
      private final BindingSelection<K, V> bindingSelection;

      private MultimapBinder(BindingSelection<K, V> bindingSelection) {
         this.bindingSelection = bindingSelection;
      }

      @Override
      public void configure(Binder binder) {
         // Binds a Map<K, Set<Provider<V>>>
         Provider<Map<K, Set<Provider<V>>>> multimapProvider =
                 new RealProviderMultimapProvider<K, V>(bindingSelection.getMapKey());
         binder.bind(bindingSelection.getProviderSetMultimapKey()).toProvider(multimapProvider);

         // Provide links from a few different public keys to the providerMultimapKey.
         // The collection this exposes is internally an ImmutableMap, so it's OK to massage
         // the guice Provider to javax Provider in the value (since the guice Provider implements
         // javax Provider).
         @SuppressWarnings({"unchecked", "rawtypes"})
         Provider<Map<K, Set<javax.inject.Provider<V>>>> javaxProvider = (Provider) multimapProvider;
         binder.bind(bindingSelection.getJavaxProviderSetMultimapKey()).toProvider(javaxProvider);

         @SuppressWarnings({"unchecked", "rawtypes"})
         Provider<Map<K, Collection<Provider<V>>>> collectionProvider = (Provider) multimapProvider;
         binder
                 .bind(bindingSelection.getProviderCollectionMultimapKey())
                 .toProvider(collectionProvider);

         @SuppressWarnings({"unchecked", "rawtypes"})
         Provider<Map<K, Collection<javax.inject.Provider<V>>>> collectionJavaxProvider =
                 (Provider) multimapProvider;
         binder
                 .bind(bindingSelection.getJavaxProviderCollectionMultimapKey())
                 .toProvider(collectionJavaxProvider);

         // Binds a Map<K, Set<V>>
         @SuppressWarnings({"unchecked", "rawtypes"})
         Provider<Map<K, Set<V>>> realMultimapProvider =
                 new RealMultimapProvider(bindingSelection.getMapKey());
         binder.bind(bindingSelection.getMultimapKey()).toProvider(realMultimapProvider);
      }

      @Override
      public int hashCode() {
         return bindingSelection.hashCode();
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof MultimapBinder
                 && ((MultimapBinder<?, ?>) o).bindingSelection.equals(bindingSelection);
      }

      private static final class RealProviderMultimapProvider<K, V>
              extends RealMultimapBinderProviderWithDependencies<K, V, Map<K, Set<Provider<V>>>> {
         private Map<K, Set<Provider<V>>> multimapOfProviders;
         private Set<Dependency<?>> dependencies = RealMapBinder.MODULE_DEPENDENCIES;

         private RealProviderMultimapProvider(Key<Map<K, V>> mapKey) {
            super(mapKey);
         }

         @Override
         public Set<Dependency<?>> getDependencies() {
            return dependencies;
         }

         @Override
         protected void doInitialize(InjectorImpl injector, Errors errors) {
            ImmutableMap.Builder<K, Set<Provider<V>>> multimapOfProvidersBuilder =
                    ImmutableMap.builder();
            ImmutableSet.Builder<Dependency<?>> dependenciesBuilder = ImmutableSet.builder();
            for (Map.Entry<K, Set<Binding<V>>> entry :
                    bindingSelection.getMultimapBindings().entrySet()) {
               ImmutableSet.Builder<Provider<V>> providersBuilder = ImmutableSet.builder();
               for (Binding<V> binding : entry.getValue()) {
                  providersBuilder.add(binding.getProvider());
                  dependenciesBuilder.add(Dependency.get(getKeyOfProvider(binding.getKey())));
               }

               multimapOfProvidersBuilder.put(entry.getKey(), providersBuilder.build());
            }
            multimapOfProviders = multimapOfProvidersBuilder.build();
            dependencies = dependenciesBuilder.build();
         }

         @Override
         protected Map<K, Set<Provider<V>>> doProvision(
                 InternalContext context, Dependency<?> dependency) {
            return multimapOfProviders;
         }
      }

      private static final class RealMultimapProvider<K, V>
              extends RealMultimapBinderProviderWithDependencies<K, V, Map<K, Set<V>>> {

         /**
          * A simple class to hold a key and the associated bindings as an array.
          *
          * <p>Arrays are used for performance.
          */
         private static final class PerKeyData<K, V> {
            private final K key;
            private final Binding<V>[] bindings;
            private final SingleParameterInjector<V>[] injectors;

            private PerKeyData(K key, Binding<V>[] bindings, SingleParameterInjector<V>[] injectors) {
               Preconditions.checkArgument(bindings.length == injectors.length);

               this.key = key;
               this.bindings = bindings;
               this.injectors = injectors;
            }
         }

         private Set<Dependency<?>> dependencies = RealMapBinder.MODULE_DEPENDENCIES;

         private PerKeyData<K, V>[] perKeyDatas;

         private RealMultimapProvider(Key<Map<K, V>> mapKey) {
            super(mapKey);
         }

         @Override
         public Set<Dependency<?>> getDependencies() {
            return dependencies;
         }

         @Override
         protected void doInitialize(InjectorImpl injector, Errors errors) throws ErrorsException {
            @SuppressWarnings({"unchecked", "rawtypes"})
            PerKeyData<K, V>[] typedPerKeyData =
                    new PerKeyData[bindingSelection.getMapBindings().size()];
            perKeyDatas = typedPerKeyData;
            ImmutableSet.Builder<Dependency<?>> dependenciesBuilder = ImmutableSet.builder();
            List<Dependency<?>> dependenciesForKey = Lists.newArrayList();
            int i = 0;
            for (Map.Entry<K, Set<Binding<V>>> entry :
                    bindingSelection.getMultimapBindings().entrySet()) {
               // Clear the list of dependencies because we're reusing it for each different key
               dependenciesForKey.clear();

               Set<Binding<V>> bindings = entry.getValue();
               @SuppressWarnings({"unchecked", "rawtypes"})
               Binding<V>[] typedBindings = new Binding[bindings.size()];
               Binding<V>[] bindingsArray = typedBindings;
               int j = 0;
               for (Binding<V> binding : bindings) {
                  Dependency<V> dependency = Dependency.get(binding.getKey());
                  dependenciesBuilder.add(dependency);
                  dependenciesForKey.add(dependency);
                  bindingsArray[j] = binding;
                  j++;
               }

               @SuppressWarnings("unchecked")
               SingleParameterInjector<V>[] injectors =
                       (SingleParameterInjector<V>[])
                               injector.getParametersInjectors(dependenciesForKey, errors);

               perKeyDatas[i] = new PerKeyData<>(entry.getKey(), bindingsArray, injectors);
               i++;
            }

            dependencies = dependenciesBuilder.build();
         }

         @Override
         protected Map<K, Set<V>> doProvision(InternalContext context, Dependency<?> dependency)
                 throws InternalProvisionException {
            ImmutableMap.Builder<K, Set<V>> resultBuilder = ImmutableMap.builder();

            for (PerKeyData<K, V> perKeyData : perKeyDatas) {
               ImmutableSet.Builder<V> bindingsBuilder = ImmutableSet.builder();
               SingleParameterInjector<V>[] injectors = perKeyData.injectors;
               for (int i = 0; i < injectors.length; i++) {
                  SingleParameterInjector<V> injector = injectors[i];
                  V value = injector.inject(context);

                  if (value == null) {
                     throw createNullValueException(perKeyData.key, perKeyData.bindings[i]);
                  }

                  bindingsBuilder.add(value);
               }

               resultBuilder.put(perKeyData.key, bindingsBuilder.build());
            }

            return resultBuilder.build();
         }
      }
   }

   /** A factory for a {@code Map.Entry<K, Provider<V>>}. */
   //VisibleForTesting
   static final class ProviderMapEntry<K, V>
           extends InternalProviderInstanceBindingImpl.Factory<Map.Entry<K, Provider<V>>> {
      private final K key;
      private final Key<V> valueKey;
      private Map.Entry<K, Provider<V>> entry;

      ProviderMapEntry(K key, Key<V> valueKey) {
         super(InitializationTiming.EAGER);
         this.key = key;
         this.valueKey = valueKey;
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
         // The dependencies are Key<Provider<V>>
         return ImmutableSet.<Dependency<?>>of(Dependency.get(getKeyOfProvider(valueKey)));
      }

      @Override
      void initialize(InjectorImpl injector, Errors errors) {
         Binding<V> valueBinding = injector.getExistingBinding(valueKey);
         entry = Maps.immutableEntry(key, valueBinding.getProvider());
      }

      @Override
      protected Map.Entry<K, Provider<V>> doProvision(
              InternalContext context, Dependency<?> dependency) {
         return entry;
      }

      K getKey() {
         return key;
      }

      Key<V> getValueKey() {
         return valueKey;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof ProviderMapEntry) {
            ProviderMapEntry<?, ?> o = (ProviderMapEntry<?, ?>) obj;
            return key.equals(o.key) && valueKey.equals(o.valueKey);
         }
         return false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(key, valueKey);
      }

      @Override
      public String toString() {
         return "ProviderMapEntry(" + key + ", " + valueKey + ")";
      }
   }

   /** A base class for ProviderWithDependencies that need equality based on a specific object. */
   private abstract static class RealMapBinderProviderWithDependencies<K, V, P>
           extends InternalProviderInstanceBindingImpl.Factory<P> {
      final BindingSelection<K, V> bindingSelection;

      private RealMapBinderProviderWithDependencies(BindingSelection<K, V> bindingSelection) {
         // While MapBinders only depend on bindings created in modules so we could theoretically
         // initialize eagerly, they also depend on
         // 1. findBindingsByType returning results
         // 2. being able to call BindingImpl.acceptTargetVisitor
         // neither of those is available during eager initialization, so we use DELAYED
         super(InitializationTiming.DELAYED);

         this.bindingSelection = bindingSelection;
      }

      @Override
      final void initialize(InjectorImpl injector, Errors errors) throws ErrorsException {
         if (bindingSelection.tryInitialize(injector, errors)) {
            doInitialize(injector, errors);
         }
      }

      /**
       * Initialize the factory. BindingSelection is guaranteed to be initialized at this point and
       * this will be called prior to any provisioning.
       */
      protected abstract void doInitialize(InjectorImpl injector, Errors errors)
              throws ErrorsException;

      @Override
      public boolean equals(Object obj) {
         return obj != null
                 && this.getClass() == obj.getClass()
                 && bindingSelection.equals(
                 ((RealMapBinderProviderWithDependencies<?, ?, ?>) obj).bindingSelection);
      }

      @Override
      public int hashCode() {
         return bindingSelection.hashCode();
      }
   }

   /**
    * A base class for ProviderWithDependencies that need equality based on a specific object.
    *
    * <p>This differs from {@link RealMapBinderProviderWithDependencies} in that it gets the {@code
    * bindingSelection} from the injector at initialization time, rather than in the constructor.
    * This is done to allow all the providers to operate on the same instance of the {@link
    * BindingSelection}.
    */
   private abstract static class RealMultimapBinderProviderWithDependencies<K, V, P>
           extends InternalProviderInstanceBindingImpl.Factory<P> {
      final Key<Map<K, V>> mapKey;
      BindingSelection<K, V> bindingSelection;

      private RealMultimapBinderProviderWithDependencies(Key<Map<K, V>> mapKey) {
         // While MapBinders only depend on bindings created in modules so we could theoretically
         // initialize eagerly, they also depend on
         // 1. findBindingsByType returning results
         // 2. being able to call BindingImpl.acceptTargetVisitor
         // neither of those is available during eager initialization, so we use DELAYED
         super(InitializationTiming.DELAYED);

         this.mapKey = mapKey;
      }

      /**
       * This will get the authoritative {@link BindingSelection} from the map provider. This
       * guarantees that everyone has the same instance of the bindingSelection and sees consistent
       * state.
       */
      @Override
      final void initialize(InjectorImpl injector, Errors errors) throws ErrorsException {
         Binding<Map<K, V>> mapBinding = injector.getExistingBinding(mapKey);
         ProviderInstanceBinding<Map<K, V>> providerInstanceBinding =
                 (ProviderInstanceBinding<Map<K, V>>) mapBinding;
         @SuppressWarnings("unchecked")
         RealMapProvider<K, V> mapProvider =
                 (RealMapProvider<K, V>) providerInstanceBinding.getUserSuppliedProvider();

         this.bindingSelection = mapProvider.getBindingSelection();

         if (bindingSelection.tryInitialize(injector, errors)) {
            doInitialize(injector, errors);
         }
      }

      /**
       * Initialize the factory. BindingSelection is guaranteed to be initialized at this point and
       * this will be called prior to any provisioning.
       */
      abstract void doInitialize(InjectorImpl injector, Errors errors) throws ErrorsException;

      @Override
      public boolean equals(Object obj) {
         return obj != null
                 && this.getClass() == obj.getClass()
                 && mapKey.equals(((RealMultimapBinderProviderWithDependencies<?, ?, ?>) obj).mapKey);
      }

      @Override
      public int hashCode() {
         return mapKey.hashCode();
      }
   }

   private static <K, V> InternalProvisionException createNullValueException(
           K key, Binding<V> binding) {
      return InternalProvisionException.create(
              "Map injection failed due to null value for key \"%s\", bound at: %s",
              key, binding.getSource());
   }
}
