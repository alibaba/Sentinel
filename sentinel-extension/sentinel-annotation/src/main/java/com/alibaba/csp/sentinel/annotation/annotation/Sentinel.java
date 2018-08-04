package com.alibaba.csp.sentinel.annotation.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface Sentinel {

	String name();
	
	int count() default 1;
	
	Class<? extends Throwable>[] blockHandler() default {} ;
	
	
}
