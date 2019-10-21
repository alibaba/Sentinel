package com.google.inject.spi;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;

public final class RequireExactBindingAnnotationsOption implements Element {
   private final Object source;

   RequireExactBindingAnnotationsOption(Object source) {
      this.source = Preconditions.checkNotNull(source, "source");
   }

   public Object getSource() {
      return this.source;
   }

   public void applyTo(Binder binder) {
      binder.withSource(this.getSource()).requireExactBindingAnnotations();
   }

   public <T> T acceptVisitor(ElementVisitor<T> visitor) {
      return visitor.visit(this);
   }
}
