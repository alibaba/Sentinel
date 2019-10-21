package com.google.inject.internal;

interface ConstructionProxyFactory<T> {
   ConstructionProxy<T> create() throws ErrorsException;
}
