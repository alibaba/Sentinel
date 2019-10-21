package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to define ordering (possibly partial) to use
 * when serializing object properties. Properties included in annotation
 * declaration will be serialized first (in defined order), followed by
 * any properties not included in the definition.
 * Annotation definition will override any implicit orderings (such as
 * guarantee that Creator-properties are serialized before non-creator
 * properties)
 *<p>
 * Examples:
 *<pre>
 *  // ensure that "id" and "name" are output before other properties
 *  <div>@</div>JsonPropertyOrder({ "id", "name" })
 *  // order any properties that don't have explicit setting using alphabetic order
 *  <div>@</div>JsonPropertyOrder(alphabetic=true)
 *</pre>
 *<p>
 * This annotation has no effect on deserialization.
 * 
 * @since 1.4
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonPropertyOrder
{
    /**
     * Order in which properties of annotated object are to be serialized in.
     */
    public String[] value() default { };

    /**
     * Property that defines what to do regarding ordering of properties
     * not explicitly included in annotation instance. If set to true,
     * they will be alphabetically ordered; if false, order is
     * undefined (default setting)
     */
    public boolean alphabetic() default false;
}
