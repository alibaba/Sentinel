package com.google.inject.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.InjectionPoint;
import java.util.Iterator;

final class MembersInjectorImpl<T> implements MembersInjector<T> {
   private final TypeLiteral<T> typeLiteral;
   private final InjectorImpl injector;
   private final ImmutableList<SingleMemberInjector> memberInjectors;
   private final ImmutableList<MembersInjector<? super T>> userMembersInjectors;
   private final ImmutableList<InjectionListener<? super T>> injectionListeners;

   MembersInjectorImpl(InjectorImpl injector, TypeLiteral<T> typeLiteral, EncounterImpl<T> encounter, ImmutableList<SingleMemberInjector> memberInjectors) {
      this.injector = injector;
      this.typeLiteral = typeLiteral;
      this.memberInjectors = memberInjectors.isEmpty() ? null : memberInjectors;
      this.userMembersInjectors = encounter.getMembersInjectors().isEmpty() ? null : encounter.getMembersInjectors().asList();
      this.injectionListeners = encounter.getInjectionListeners().isEmpty() ? null : encounter.getInjectionListeners().asList();
   }

   public ImmutableList<SingleMemberInjector> getMemberInjectors() {
      return this.memberInjectors == null ? ImmutableList.of() : this.memberInjectors;
   }

   public void injectMembers(T instance) {
      TypeLiteral localTypeLiteral = this.typeLiteral;

      try {
         this.injectAndNotify(instance, (Key)null, (ProvisionListenerStackCallback)null, localTypeLiteral, false);
      } catch (InternalProvisionException var4) {
         throw var4.addSource(localTypeLiteral).toProvisionException();
      }
   }

   void injectAndNotify(final T instance, Key<T> key, ProvisionListenerStackCallback<T> provisionCallback, Object source, final boolean toolableOnly) throws InternalProvisionException {
      if (instance != null) {
         final InternalContext context = this.injector.enterContext();
         context.pushState(key, source);

         try {
            if (provisionCallback != null && provisionCallback.hasListeners()) {
               provisionCallback.provision(context, new ProvisionListenerStackCallback.ProvisionCallback<T>() {
                  public T call() throws InternalProvisionException {
                     MembersInjectorImpl.this.injectMembers(instance, context, toolableOnly);
                     return instance;
                  }
               });
            } else {
               this.injectMembers(instance, context, toolableOnly);
            }
         } finally {
            context.popState();
            context.close();
         }

         if (!toolableOnly) {
            this.notifyListeners(instance);
         }

      }
   }

   void notifyListeners(T instance) throws InternalProvisionException {
      ImmutableList<InjectionListener<? super T>> localInjectionListeners = this.injectionListeners;
      if (localInjectionListeners != null) {
         for(int i = 0; i < localInjectionListeners.size(); ++i) {
            InjectionListener injectionListener = (InjectionListener)localInjectionListeners.get(i);

            try {
               injectionListener.afterInjection(instance);
            } catch (RuntimeException var6) {
               throw InternalProvisionException.errorNotifyingInjectionListener(injectionListener, this.typeLiteral, var6);
            }
         }

      }
   }

   void injectMembers(T t, InternalContext context, boolean toolableOnly) throws InternalProvisionException {
      ImmutableList<SingleMemberInjector> localMembersInjectors = memberInjectors;
      if (localMembersInjectors != null) {
         // optimization: use manual for/each to save allocating an iterator here
         for (int i = 0, size = localMembersInjectors.size(); i < size; i++) {
            SingleMemberInjector injector = localMembersInjectors.get(i);
            if (!toolableOnly || injector.getInjectionPoint().isToolable()) {
               injector.inject(context, t);
            }
         }
      }

      // TODO: There's no way to know if a user's MembersInjector wants toolable injections.
      if (!toolableOnly) {
         ImmutableList<MembersInjector<? super T>> localUsersMembersInjectors = userMembersInjectors;
         if (localUsersMembersInjectors != null) {
            // optimization: use manual for/each to save allocating an iterator here
            for (int i = 0; i < localUsersMembersInjectors.size(); i++) {
               MembersInjector<? super T> userMembersInjector = localUsersMembersInjectors.get(i);
               try {
                  userMembersInjector.injectMembers(t);
               } catch (RuntimeException e) {
                  throw InternalProvisionException.errorInUserInjector(
                          userMembersInjector, typeLiteral, e);
               }
            }
         }
      }

   }

   public String toString() {
      return "MembersInjector<" + this.typeLiteral + ">";
   }

   public ImmutableSet<InjectionPoint> getInjectionPoints() {
      ImmutableList<SingleMemberInjector> localMemberInjectors = this.memberInjectors;
      if (localMemberInjectors == null) {
         return ImmutableSet.of();
      } else {
         Builder<InjectionPoint> builder = ImmutableSet.builder();
         Iterator i$ = localMemberInjectors.iterator();

         while(i$.hasNext()) {
            SingleMemberInjector memberInjector = (SingleMemberInjector)i$.next();
            builder.add(memberInjector.getInjectionPoint());
         }

         return builder.build();
      }
   }
}
