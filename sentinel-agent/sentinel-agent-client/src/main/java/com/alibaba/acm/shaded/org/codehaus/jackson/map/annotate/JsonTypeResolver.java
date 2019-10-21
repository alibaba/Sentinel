package com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate;

import java.lang.annotation.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JacksonAnnotation;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.TypeResolverBuilder;

/**
 * Annotation that can be used to explicitly define custom resolver
 * used for handling serialization and deserialization of type information,
 * needed for handling of polymorphic types (or sometimes just for linking
 * abstract types to concrete types)
 * 
 * @since 1.5
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonTypeResolver
{
    /**
     * Defines implementation class of {@link TypeResolverBuilder} which is used to construct
     * actual {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer} and {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.TypeDeserializer}
     * instances that handle reading and writing addition type information needed to support polymorphic
     * deserialization.
     */
    public Class<? extends TypeResolverBuilder<?>> value();
}
