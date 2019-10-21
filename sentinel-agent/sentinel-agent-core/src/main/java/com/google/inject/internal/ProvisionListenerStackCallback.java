package com.google.inject.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Binding;
import com.google.inject.spi.DependencyAndSource;
import com.google.inject.spi.ProvisionListener;

import java.util.List;
import java.util.Set;

final class ProvisionListenerStackCallback<T> {
   private static final ProvisionListener[] EMPTY_LISTENER = new ProvisionListener[0];
   private static final ProvisionListenerStackCallback<?> EMPTY_CALLBACK = new ProvisionListenerStackCallback((Binding)null, ImmutableList.of());
   private final ProvisionListener[] listeners;
   private final Binding<T> binding;

   public static <T> ProvisionListenerStackCallback<T> emptyListener() {
      return (ProvisionListenerStackCallback<T>) EMPTY_CALLBACK;
   }

   public ProvisionListenerStackCallback(Binding<T> binding, List<ProvisionListener> listeners) {
      this.binding = binding;
      if (listeners.isEmpty()) {
         this.listeners = EMPTY_LISTENER;
      } else {
         Set<ProvisionListener> deDuplicated = Sets.newLinkedHashSet(listeners);
         this.listeners = (ProvisionListener[])deDuplicated.toArray(new ProvisionListener[deDuplicated.size()]);
      }

   }

   public boolean hasListeners() {
      return this.listeners.length > 0;
   }

   public T provision(InternalContext context, ProvisionCallback<T> callable) throws InternalProvisionException {
      Provision provision = new Provision(context, callable);
      RuntimeException caught = null;

      try {
         provision.provision();
      } catch (RuntimeException var6) {
         caught = var6;
      }

      if (provision.exceptionDuringProvision != null) {
         throw provision.exceptionDuringProvision;
      } else if (caught != null) {
         Object listener = provision.erredListener != null ? provision.erredListener.getClass() : "(unknown)";
         throw InternalProvisionException.errorInUserCode(caught, "Error notifying ProvisionListener %s of %s.%n Reason: %s", listener, this.binding.getKey(), caught);
      } else {
         return provision.result;
      }
   }

   private class Provision extends ProvisionListener.ProvisionInvocation<T> {
      final InternalContext context;
      final ProvisionCallback<T> callable;
      int index = -1;
      T result;
      InternalProvisionException exceptionDuringProvision;
      ProvisionListener erredListener;

      public Provision(InternalContext context, ProvisionCallback<T> callable) {
         this.callable = callable;
         this.context = context;
      }

      public T provision() {
         ++this.index;
         if (this.index == ProvisionListenerStackCallback.this.listeners.length) {
            try {
               this.result = this.callable.call();
            } catch (InternalProvisionException var4) {
               this.exceptionDuringProvision = var4;
               throw var4.toProvisionException();
            }
         } else {
            if (this.index >= ProvisionListenerStackCallback.this.listeners.length) {
               throw new IllegalStateException("Already provisioned in this listener.");
            }

            int currentIdx = this.index;

            try {
               ProvisionListenerStackCallback.this.listeners[this.index].onProvision(this);
            } catch (RuntimeException var3) {
               this.erredListener = ProvisionListenerStackCallback.this.listeners[currentIdx];
               throw var3;
            }

            if (currentIdx == this.index) {
               this.provision();
            }
         }

         return this.result;
      }

      public Binding<T> getBinding() {
         return ProvisionListenerStackCallback.this.binding;
      }

      /** @deprecated */
      @Deprecated
      public List<DependencyAndSource> getDependencyChain() {
         return this.context.getDependencyChain();
      }
   }

   public interface ProvisionCallback<T> {
      T call() throws InternalProvisionException;
   }
}
