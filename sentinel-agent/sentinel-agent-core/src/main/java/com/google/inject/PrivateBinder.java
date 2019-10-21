package com.google.inject;

import com.google.inject.binder.AnnotatedElementBuilder;

public interface PrivateBinder extends Binder {
   void expose(Key<?> var1);

   AnnotatedElementBuilder expose(Class<?> var1);

   AnnotatedElementBuilder expose(TypeLiteral<?> var1);

   PrivateBinder withSource(Object var1);

   PrivateBinder skipSources(Class... var1);
}
