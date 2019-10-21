package com.google.inject.multibindings;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.Element;

public interface OptionalBinderBinding<T> {
   Key<T> getKey();

   Binding<?> getDefaultBinding();

   Binding<?> getActualBinding();

   boolean containsElement(Element var1);
}
