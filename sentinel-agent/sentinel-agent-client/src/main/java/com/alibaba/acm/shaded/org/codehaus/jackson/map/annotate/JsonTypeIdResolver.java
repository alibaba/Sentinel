package com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JacksonAnnotation;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeIdResolver;

/**
 * Annotation that can be used to plug a custom type identifier handler
 * ({@link TypeIdResolver})
 * to be used by
 * {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeSerializer}s
 * and {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer}s
 * for converting between java types and type id included in JSON content.
 * In simplest cases this can be a simple class with static mapping between
 * type names and matching classes.
 * 
 * @author tatu
 * @since 1.5
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonTypeIdResolver
{
    /**
     * Defines implementation class of {@link TypeIdResolver} to use for
     * converting between external type id (type name) and actual
     * type of object.
     */
    public Class<? extends TypeIdResolver> value();
}
