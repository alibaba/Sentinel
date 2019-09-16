package com.alibaba.csp.sentinel.dashboard.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
public @interface AuthAction {

    AuthService.PrivilegeType value();

    String targetName() default "app";

    String message() default "No privilege";
}
