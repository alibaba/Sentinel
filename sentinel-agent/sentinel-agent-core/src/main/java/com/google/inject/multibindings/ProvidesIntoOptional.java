package com.google.inject.multibindings;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProvidesIntoOptional {
   Type value();

   public static enum Type {
      ACTUAL,
      DEFAULT;
   }
}
