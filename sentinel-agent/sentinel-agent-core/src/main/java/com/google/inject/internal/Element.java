package com.google.inject.internal;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
@interface Element {
   String setName();

   int uniqueId();

   Type type();

   String keyType();

   public static enum Type {
      MAPBINDER,
      MULTIBINDER;
   }
}
