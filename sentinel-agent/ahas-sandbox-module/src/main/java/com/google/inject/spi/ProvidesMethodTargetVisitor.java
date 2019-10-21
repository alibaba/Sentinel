package com.google.inject.spi;

public interface ProvidesMethodTargetVisitor<T, V> extends BindingTargetVisitor<T, V> {
   V visit(ProvidesMethodBinding<? extends T> var1);
}
