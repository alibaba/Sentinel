package com.google.inject.spi;

import com.google.inject.Binding;
import com.google.inject.Key;

public interface LinkedKeyBinding<T> extends Binding<T> {
   Key<? extends T> getLinkedKey();
}
