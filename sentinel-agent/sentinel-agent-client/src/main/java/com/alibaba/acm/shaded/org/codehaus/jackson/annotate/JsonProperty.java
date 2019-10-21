package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that can be used to define a non-static
 * method as a "setter" or "getter" for a logical property
 * (depending on its signature),
 * or non-static object field to be used (serialized, deserialized) as
 * a logical property.
 *<p>
 * Default value ("") indicates that the field name is used
 * as the property name without any modifications, but it
 * can be specified to non-empty value to specify different
 * name. Property name refers to name used externally, as
 * the field name in Json objects.
 *<p>
 * NOTE: since version 1.1, annotation has also been applicable
 * to fields (not with 1.0).
 *<p>
 * NOTE: since version 1.2, annotation has also been applicable
 * to (constructor) parameters
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonProperty
{
    /**
     * Defines name of the logical property, i.e. Json object field
     * name to use for the property: if empty String (which is the
     * default), will use name of the field that is annotated.
     */
    String value() default "";
}
