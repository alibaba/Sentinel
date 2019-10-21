package com.google.inject.internal;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicInteger;

public class UniqueAnnotations {
   private static final AtomicInteger nextUniqueValue = new AtomicInteger(1);

   private UniqueAnnotations() {
   }

   public static Annotation create() {
      return create(nextUniqueValue.getAndIncrement());
   }

   static Annotation create(final int value) {
      return new Internal() {
         public int value() {
            return value;
         }

         public Class<? extends Annotation> annotationType() {
            return Internal.class;
         }

         public String toString() {
            return "@" + Internal.class.getName() + "(value=" + value + ")";
         }

         public boolean equals(Object o) {
            return o instanceof Internal && ((Internal)o).value() == this.value();
         }

         public int hashCode() {
            return 127 * "value".hashCode() ^ value;
         }
      };
   }

   @Retention(RetentionPolicy.RUNTIME)
   @BindingAnnotation
   @interface Internal {
      int value();
   }
}
