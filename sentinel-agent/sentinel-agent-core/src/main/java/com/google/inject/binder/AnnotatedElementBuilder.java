package com.google.inject.binder;

import java.lang.annotation.Annotation;

public interface AnnotatedElementBuilder {
   void annotatedWith(Class<? extends Annotation> var1);

   void annotatedWith(Annotation var1);
}
