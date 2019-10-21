package com.google.inject.spi;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Binding;

import java.util.List;

public final class ProvisionListenerBinding implements Element {
   private final Object source;
   private final com.google.inject.matcher.Matcher<? super Binding<?>> bindingMatcher;
   private final List<ProvisionListener> listeners;

   ProvisionListenerBinding(Object source, com.google.inject.matcher.Matcher<? super Binding<?>> bindingMatcher, ProvisionListener[] listeners) {
      this.source = source;
      this.bindingMatcher = bindingMatcher;
      this.listeners = ImmutableList.copyOf(listeners);
   }

   public List<ProvisionListener> getListeners() {
      return this.listeners;
   }

   public com.google.inject.matcher.Matcher<? super Binding<?>> getBindingMatcher() {
      return this.bindingMatcher;
   }

   public Object getSource() {
      return this.source;
   }

   public <R> R acceptVisitor(ElementVisitor<R> visitor) {
      return visitor.visit(this);
   }

   public void applyTo(Binder binder) {
      binder.withSource(this.getSource()).bindListener(this.bindingMatcher, (ProvisionListener[])this.listeners.toArray(new ProvisionListener[this.listeners.size()]));
   }
}
