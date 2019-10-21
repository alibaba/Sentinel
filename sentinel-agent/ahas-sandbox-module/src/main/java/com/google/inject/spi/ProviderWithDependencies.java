package com.google.inject.spi;

import com.google.inject.Provider;

public interface ProviderWithDependencies<T> extends Provider<T>, HasDependencies {
}
