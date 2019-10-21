package com.google.inject.binder;

import java.lang.annotation.Annotation;

public interface AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {
   LinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> var1);

   LinkedBindingBuilder<T> annotatedWith(Annotation var1);
}
