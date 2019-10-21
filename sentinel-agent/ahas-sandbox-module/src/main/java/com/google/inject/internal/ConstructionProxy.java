package com.google.inject.internal;

import com.google.inject.spi.InjectionPoint;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

interface ConstructionProxy<T> {
   T newInstance(Object... var1) throws InvocationTargetException;

   InjectionPoint getInjectionPoint();

   Constructor<T> getConstructor();
}
