package com.google.inject.multibindings;

import com.google.inject.spi.BindingTargetVisitor;

public interface MultibindingsTargetVisitor<T, V> extends BindingTargetVisitor<T, V> {
   V visit(MultibinderBinding<? extends T> var1);

   V visit(MapBinderBinding<? extends T> var1);

   V visit(OptionalBinderBinding<? extends T> var1);
}
