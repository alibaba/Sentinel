package com.google.inject.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.ConfigurationException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.TypeListener;
import com.google.inject.spi.TypeListenerBinding;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

final class MembersInjectorStore {
   private final InjectorImpl injector;
   private final ImmutableList<TypeListenerBinding> typeListenerBindings;
   private final FailableCache<TypeLiteral<?>, MembersInjectorImpl<?>> cache = new FailableCache<TypeLiteral<?>, MembersInjectorImpl<?>>() {
      protected MembersInjectorImpl<?> create(TypeLiteral<?> type, Errors errors) throws ErrorsException {
         return MembersInjectorStore.this.createWithListeners(type, errors);
      }
   };

   MembersInjectorStore(InjectorImpl injector, List<TypeListenerBinding> typeListenerBindings) {
      this.injector = injector;
      this.typeListenerBindings = ImmutableList.copyOf(typeListenerBindings);
   }

   public boolean hasTypeListeners() {
      return !this.typeListenerBindings.isEmpty();
   }

   public <T> MembersInjectorImpl<T> get(TypeLiteral<T> key, Errors errors) throws ErrorsException {
      return (MembersInjectorImpl)this.cache.get(key, errors);
   }

   boolean remove(TypeLiteral<?> type) {
      return this.cache.remove(type);
   }

   private <T> MembersInjectorImpl<T> createWithListeners(TypeLiteral<T> type, Errors errors) throws ErrorsException {
      int numErrorsBefore = errors.size();

      Set injectionPoints;
      try {
         injectionPoints = InjectionPoint.forInstanceMethodsAndFields(type);
      } catch (ConfigurationException var13) {
         errors.merge(var13.getErrorMessages());
         injectionPoints = (Set)var13.getPartialValue();
      }

      ImmutableList<SingleMemberInjector> injectors = this.getInjectors(injectionPoints, errors);
      errors.throwIfNewErrors(numErrorsBefore);
      EncounterImpl<T> encounter = new EncounterImpl(errors, this.injector.lookups);
      Set<TypeListener> alreadySeenListeners = Sets.newHashSet();
      Iterator i$ = this.typeListenerBindings.iterator();

      while(i$.hasNext()) {
         TypeListenerBinding binding = (TypeListenerBinding)i$.next();
         TypeListener typeListener = binding.getListener();
         if (!alreadySeenListeners.contains(typeListener) && binding.getTypeMatcher().matches(type)) {
            alreadySeenListeners.add(typeListener);

            try {
               typeListener.hear(type, encounter);
            } catch (RuntimeException var12) {
               errors.errorNotifyingTypeListener(binding, type, var12);
            }
         }
      }

      encounter.invalidate();
      errors.throwIfNewErrors(numErrorsBefore);
      return new MembersInjectorImpl(this.injector, type, encounter, injectors);
   }

   ImmutableList<SingleMemberInjector> getInjectors(Set<InjectionPoint> injectionPoints, Errors errors) {
      List<SingleMemberInjector> injectors = Lists.newArrayList();
      Iterator i$ = injectionPoints.iterator();

      while(i$.hasNext()) {
         InjectionPoint injectionPoint = (InjectionPoint)i$.next();

         try {
            Errors errorsForMember = injectionPoint.isOptional() ? new Errors(injectionPoint) : errors.withSource(injectionPoint);
            SingleMemberInjector injector = injectionPoint.getMember() instanceof Field ? new SingleFieldInjector(this.injector, injectionPoint, errorsForMember) : new SingleMethodInjector(this.injector, injectionPoint, errorsForMember);
            injectors.add(injector);
         } catch (ErrorsException var8) {
         }
      }

      return ImmutableList.copyOf(injectors);
   }
}
