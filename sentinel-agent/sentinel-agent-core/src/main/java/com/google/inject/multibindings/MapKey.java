package com.google.inject.multibindings;

import java.lang.annotation.*;

@Documented
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MapKey {
   boolean unwrapValue() default true;
}
