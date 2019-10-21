package com.google.inject.multibindings;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Element;
import java.util.List;

public interface MultibinderBinding<T> {
   Key<T> getSetKey();

   TypeLiteral<?> getElementTypeLiteral();

   List<Binding<?>> getElements();

   boolean permitsDuplicates();

   boolean containsElement(Element var1);
}
