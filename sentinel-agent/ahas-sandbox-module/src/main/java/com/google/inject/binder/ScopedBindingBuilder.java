package com.google.inject.binder;

import com.google.inject.Scope;
import java.lang.annotation.Annotation;

public interface ScopedBindingBuilder {
   void in(Class<? extends Annotation> var1);

   void in(Scope var1);

   void asEagerSingleton();
}
