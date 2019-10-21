package com.google.inject.internal;

interface ConstructionProxyFactory<T> {
   com.google.inject.internal.ConstructionProxy<T> create() throws ErrorsException;
}
