package com.google.inject.internal;

import java.lang.annotation.Annotation;

public class Nullability {
   private Nullability() {
   }

   public static boolean allowsNull(Annotation[] annotations) {
      Annotation[] arr$ = annotations;
      int len$ = annotations.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Annotation a = arr$[i$];
         Class<? extends Annotation> type = a.annotationType();
         if ("Nullable".equals(type.getSimpleName())) {
            return true;
         }
      }

      return false;
   }
}
