package com.alibaba.csp.service.bridge.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {
   Option value() default Option.SINGLETON;

   public static enum Option {
      SINGLETON,
      PROTOTYPE;
   }
}
