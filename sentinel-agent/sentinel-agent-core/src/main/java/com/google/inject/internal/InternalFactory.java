package com.google.inject.internal;

import com.google.inject.spi.Dependency;

interface InternalFactory<T> {
   T get(InternalContext var1, Dependency<?> var2, boolean var3) throws InternalProvisionException;
}
