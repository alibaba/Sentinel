package com.google.inject.spi;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.ConfigurationException;
import java.util.Set;

public final class StaticInjectionRequest implements Element {
   private final Object source;
   private final Class<?> type;

   StaticInjectionRequest(Object source, Class<?> type) {
      this.source = Preconditions.checkNotNull(source, "source");
      this.type = (Class)Preconditions.checkNotNull(type, "type");
   }

   public Object getSource() {
      return this.source;
   }

   public Class<?> getType() {
      return this.type;
   }

   public Set<InjectionPoint> getInjectionPoints() throws ConfigurationException {
      return InjectionPoint.forStaticMethodsAndFields(this.type);
   }

   public void applyTo(Binder binder) {
      binder.withSource(this.getSource()).requestStaticInjection(this.type);
   }

   public <T> T acceptVisitor(ElementVisitor<T> visitor) {
      return visitor.visit(this);
   }
}
