package com.google.inject.spi;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;

public final class DisableCircularProxiesOption implements Element {
   private final Object source;

   DisableCircularProxiesOption(Object source) {
      this.source = Preconditions.checkNotNull(source, "source");
   }

   public Object getSource() {
      return this.source;
   }

   public void applyTo(Binder binder) {
      binder.withSource(this.getSource()).disableCircularProxies();
   }

   public <T> T acceptVisitor(ElementVisitor<T> visitor) {
      return visitor.visit(this);
   }
}
