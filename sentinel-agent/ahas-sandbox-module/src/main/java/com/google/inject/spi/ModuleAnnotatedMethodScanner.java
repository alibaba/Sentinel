package com.google.inject.spi;

import com.google.inject.Key;
import java.lang.annotation.Annotation;
import java.util.Set;

public abstract class ModuleAnnotatedMethodScanner {
   public abstract Set<? extends Class<? extends Annotation>> annotationClasses();

   public abstract <T> Key<T> prepareMethod(com.google.inject.Binder var1, Annotation var2, Key<T> var3, InjectionPoint var4);
}
