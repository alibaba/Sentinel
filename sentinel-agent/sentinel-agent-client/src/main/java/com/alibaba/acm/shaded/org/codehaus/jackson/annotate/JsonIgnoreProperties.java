package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to either suppress serialization of
 * properties (during serialization), or ignore processing of
 * JSON properties read (during deserialization).
 *<p>
 * Example:
 *<pre>
 * // to prevent specified fields from being serialized or deserialized
 * // (i.e. not include in JSON output; or being set even if they were included)
 * \@JsonIgnoreProperties({ "internalId", "secretKey" })
 * // To ignore any unknown properties in JSON input without exception:
 * \@JsonIgnoreProperties(ignoreUnknown=true)
 *</pre>
 *<p>
 * Only applicable to classes, not for properties (getters, setters, fields).
 * 
 * @since 1.4
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonIgnoreProperties
{
    /**
     * Names of properties to ignore.
     */
    public String[] value() default { };

    /**
     * Property that defines whether it is ok to just ignore any
     * unrecognized properties during deserialization.
     * If true, all properties that are unrecognized -- that is,
     * there are no setters or creators that accept them -- are
     * ignored without warnings (although handlers for unknown
     * properties, if any, will still be called) without
     * exception.
     *<p>
     * Does not have any effect on serialization.
     */
    public boolean ignoreUnknown() default false;
}
