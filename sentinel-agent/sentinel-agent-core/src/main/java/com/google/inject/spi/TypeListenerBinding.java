package com.google.inject.spi;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;

public final class TypeListenerBinding implements Element {
   private final Object source;
   private final Matcher<? super TypeLiteral<?>> typeMatcher;
   private final TypeListener listener;

   TypeListenerBinding(Object source, TypeListener listener, Matcher<? super TypeLiteral<?>> typeMatcher) {
      this.source = source;
      this.listener = listener;
      this.typeMatcher = typeMatcher;
   }

   public TypeListener getListener() {
      return this.listener;
   }

   public Matcher<? super TypeLiteral<?>> getTypeMatcher() {
      return this.typeMatcher;
   }

   public Object getSource() {
      return this.source;
   }

   public <T> T acceptVisitor(ElementVisitor<T> visitor) {
      return visitor.visit(this);
   }

   public void applyTo(Binder binder) {
      binder.withSource(this.getSource()).bindListener(this.typeMatcher, this.listener);
   }
}
