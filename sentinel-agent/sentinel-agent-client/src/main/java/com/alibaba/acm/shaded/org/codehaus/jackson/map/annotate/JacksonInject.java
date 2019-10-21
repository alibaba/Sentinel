package com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JacksonAnnotation;

/**
 * Jackson-specific annotation used for indicating that value of
 * annotated property will be "injected", i.e. set based on value
 * configured by <code>ObjectMapper</code> (usually on per-call basis).
 * Usually property is not deserialized from JSON, although it possible
 * to have injected value as default and still allow optional override
 * from JSON.
 * 
 * @since 1.9
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JacksonInject
{
    /**
     * Logical id of the value to inject; if not specified (or specified
     * as empty String), will use id based on declared type of property.
     */
    public String value() default "";
}
