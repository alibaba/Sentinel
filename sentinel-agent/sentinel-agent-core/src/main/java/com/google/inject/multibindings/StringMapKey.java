package com.google.inject.multibindings;

import java.lang.annotation.*;

@MapKey(
   unwrapValue = true
)
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StringMapKey {
   String value();
}
