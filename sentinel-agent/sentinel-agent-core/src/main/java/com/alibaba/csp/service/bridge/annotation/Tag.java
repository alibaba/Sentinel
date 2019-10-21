package com.alibaba.csp.service.bridge.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Tag {
   String[] value() default {};
}
