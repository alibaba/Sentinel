package com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JacksonAnnotation;

/**
 * Marker interface used to indicate implementation classes
 * (serializers, deserializers etc) that are standard ones Jackson
 * uses; not custom ones that application has added. It can be
 * added in cases where certain optimizations can be made if
 * default instances are uses; for example when handling conversions
 * of "natural" JSON types like Strings, booleans and numbers.
 * 
 * @since 1.6
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JacksonStdImpl {

}
