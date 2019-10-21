package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that can be used to define a non-static,
 * no-argument value-returning (non-void) method to be used as a "getter"
 * for a logical property,
 * as an alternative to recommended
 * {@link JsonProperty} annotation (which was introduced in version 1.1).
 *<p>
 * Getter means that when serializing Object instance of class that has
 * this method (possibly inherited from a super class), a call is made
 * through the method, and return value will be serialized as value of
 * the property.
 * 
 * @deprecated Use {@link JsonProperty} instead (deprecated since version 1.5)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
@Deprecated
public @interface JsonGetter
{
    /**
     * Defines name of the logical property this
     * method is used to access ("get"); empty String means that
     * name should be derived from the underlying method (using
     * standard Bean name detection rules)
     */
    String value() default "";
}
