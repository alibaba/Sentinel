package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that can be used to define a non-static,
 * no-argument method or member field as something of a reverse of
 * {@link JsonAnySetter} method; basically being used like a
 * getter but such that contents of the returned Map (type <b>must</b> be
 * {@link java.util.Map}) are serialized as if they were actual properties
 * of the bean that contains method/field with this annotations.
 * As with {@link JsonAnySetter}, only one property should be annotated
 * with this annotation.
 * 
 * @since 1.6
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonAnyGetter
{
}
