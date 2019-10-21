package com.google.inject.spi;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.ConfigurationException;
import com.google.inject.TypeLiteral;
import java.util.Set;

public final class InjectionRequest<T> implements Element {
   private final Object source;
   private final TypeLiteral<T> type;
   private final T instance;

   public InjectionRequest(Object source, TypeLiteral<T> type, T instance) {
      this.source = Preconditions.checkNotNull(source, "source");
      this.type = (TypeLiteral)Preconditions.checkNotNull(type, "type");
      this.instance = Preconditions.checkNotNull(instance, "instance");
   }

   public Object getSource() {
      return this.source;
   }

   public T getInstance() {
      return this.instance;
   }

   public TypeLiteral<T> getType() {
      return this.type;
   }

   public Set<InjectionPoint> getInjectionPoints() throws ConfigurationException {
      return InjectionPoint.forInstanceMethodsAndFields(this.instance.getClass());
   }

   public <R> R acceptVisitor(ElementVisitor<R> visitor) {
      return visitor.visit(this);
   }

   public void applyTo(Binder binder) {
      binder.withSource(this.getSource()).requestInjection(this.type, this.instance);
   }
}
