package com.google.inject.internal;

interface Initializable<T> {
   T get() throws InternalProvisionException;
}
