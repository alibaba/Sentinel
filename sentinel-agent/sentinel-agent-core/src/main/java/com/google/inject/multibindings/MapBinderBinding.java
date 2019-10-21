package com.google.inject.multibindings;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Element;

import java.util.List;
import java.util.Map.Entry;

public interface MapBinderBinding<T> {
   Key<T> getMapKey();

   TypeLiteral<?> getKeyTypeLiteral();

   TypeLiteral<?> getValueTypeLiteral();

   List<Entry<?, Binding<?>>> getEntries();

   List<Entry<?, Binding<?>>> getEntries(Iterable<? extends Element> var1);

   boolean permitsDuplicates();

   boolean containsElement(Element var1);
}
