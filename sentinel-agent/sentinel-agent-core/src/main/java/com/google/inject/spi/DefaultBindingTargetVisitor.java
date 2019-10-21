package com.google.inject.spi;

import com.google.inject.Binding;

public abstract class DefaultBindingTargetVisitor<T, V> implements BindingTargetVisitor<T, V> {
   protected V visitOther(Binding<? extends T> binding) {
      return null;
   }

   public V visit(InstanceBinding<? extends T> instanceBinding) {
      return this.visitOther(instanceBinding);
   }

   public V visit(ProviderInstanceBinding<? extends T> providerInstanceBinding) {
      return this.visitOther(providerInstanceBinding);
   }

   public V visit(ProviderKeyBinding<? extends T> providerKeyBinding) {
      return this.visitOther(providerKeyBinding);
   }

   public V visit(LinkedKeyBinding<? extends T> linkedKeyBinding) {
      return this.visitOther(linkedKeyBinding);
   }

   public V visit(ExposedBinding<? extends T> exposedBinding) {
      return this.visitOther(exposedBinding);
   }

   public V visit(UntargettedBinding<? extends T> untargettedBinding) {
      return this.visitOther(untargettedBinding);
   }

   public V visit(ConstructorBinding<? extends T> constructorBinding) {
      return this.visitOther(constructorBinding);
   }

   public V visit(ConvertedConstantBinding<? extends T> convertedConstantBinding) {
      return this.visitOther(convertedConstantBinding);
   }

   public V visit(ProviderBinding<? extends T> providerBinding) {
      return this.visitOther(providerBinding);
   }
}
