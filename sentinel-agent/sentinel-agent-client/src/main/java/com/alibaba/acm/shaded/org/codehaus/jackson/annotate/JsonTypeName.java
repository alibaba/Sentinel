package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation used for binding logical name that the annotated class
 * has. Used with {@link JsonTypeInfo} (and specifically its
 * {@link JsonTypeInfo#use} property) to establish relationship
 * between type names and types.
 * 
 * @since 1.5
 * 
 * @author tatu
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonTypeName {
    /**
     * Logical type name for annotated type. If missing (or defined as Empty String),
     * defaults to using non-qualified class name as the type.
     */
    public String value() default "";
}
