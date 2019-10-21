package com.google.inject.spi;

import com.google.inject.Scope;
import java.lang.annotation.Annotation;

public interface BindingScopingVisitor<V> {
   V visitEagerSingleton();

   V visitScope(Scope var1);

   V visitScopeAnnotation(Class<? extends Annotation> var1);

   V visitNoScoping();
}
