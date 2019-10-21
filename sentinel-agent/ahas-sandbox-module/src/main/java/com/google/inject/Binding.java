package com.google.inject;

import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Element;

public interface Binding<T> extends Element {
   Key<T> getKey();

   Provider<T> getProvider();

   <V> V acceptTargetVisitor(BindingTargetVisitor<? super T, V> var1);

   <V> V acceptScopingVisitor(BindingScopingVisitor<V> var1);
}
