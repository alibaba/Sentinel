package com.google.inject.spi;

import com.google.inject.Key;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface ProvidesMethodBinding<T> extends HasDependencies {
   Method getMethod();

   Object getEnclosingInstance();

   Key<T> getKey();

   Annotation getAnnotation();
}
