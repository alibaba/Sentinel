package com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JacksonAnnotation;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ValueInstantiator;

/**
 * Annotation that can be used to indicate a {@link ValueInstantiator} to use
 * for creating instances of specified type.
 * 
 * @since 1.9
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonValueInstantiator
{
    /**
     * @return  {@link ValueInstantiator} to use for annotated type
     */
    public Class<? extends ValueInstantiator> value();
}
