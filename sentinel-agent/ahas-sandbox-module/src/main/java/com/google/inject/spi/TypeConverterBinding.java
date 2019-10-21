package com.google.inject.spi;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;

public final class TypeConverterBinding implements Element {
   private final Object source;
   private final com.google.inject.matcher.Matcher<? super TypeLiteral<?>> typeMatcher;
   private final TypeConverter typeConverter;

   public TypeConverterBinding(Object source, com.google.inject.matcher.Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter typeConverter) {
      this.source = Preconditions.checkNotNull(source, "source");
      this.typeMatcher = (com.google.inject.matcher.Matcher)Preconditions.checkNotNull(typeMatcher, "typeMatcher");
      this.typeConverter = (TypeConverter)Preconditions.checkNotNull(typeConverter, "typeConverter");
   }

   public Object getSource() {
      return this.source;
   }

   public com.google.inject.matcher.Matcher<? super TypeLiteral<?>> getTypeMatcher() {
      return this.typeMatcher;
   }

   public TypeConverter getTypeConverter() {
      return this.typeConverter;
   }

   public <T> T acceptVisitor(ElementVisitor<T> visitor) {
      return visitor.visit(this);
   }

   public void applyTo(Binder binder) {
      binder.withSource(this.getSource()).convertToTypes(this.typeMatcher, this.typeConverter);
   }

   public String toString() {
      return this.typeConverter + " which matches " + this.typeMatcher + " (bound at " + Errors.convert(this.source) + ")";
   }
}
