package com.google.inject.internal;

interface InternalFactory<T> {
   T get(InternalContext var1, com.google.inject.spi.Dependency<?> var2, boolean var3) throws InternalProvisionException;
}
