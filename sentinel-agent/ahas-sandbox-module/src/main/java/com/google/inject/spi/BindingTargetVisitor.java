package com.google.inject.spi;

public interface BindingTargetVisitor<T, V> {
   V visit(InstanceBinding<? extends T> var1);

   V visit(ProviderInstanceBinding<? extends T> var1);

   V visit(ProviderKeyBinding<? extends T> var1);

   V visit(LinkedKeyBinding<? extends T> var1);

   V visit(ExposedBinding<? extends T> var1);

   V visit(UntargettedBinding<? extends T> var1);

   V visit(ConstructorBinding<? extends T> var1);

   V visit(ConvertedConstantBinding<? extends T> var1);

   V visit(ProviderBinding<? extends T> var1);
}
