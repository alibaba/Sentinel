package com.google.inject.binder;

import java.lang.annotation.Annotation;

public interface AnnotatedConstantBindingBuilder {
   ConstantBindingBuilder annotatedWith(Class<? extends Annotation> var1);

   ConstantBindingBuilder annotatedWith(Annotation var1);
}
