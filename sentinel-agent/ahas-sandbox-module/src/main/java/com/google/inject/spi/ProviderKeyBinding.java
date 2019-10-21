package com.google.inject.spi;

import com.google.inject.Binding;
import com.google.inject.Key;
import javax.inject.Provider;

public interface ProviderKeyBinding<T> extends Binding<T> {
   Key<? extends Provider<? extends T>> getProviderKey();
}
