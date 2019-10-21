package com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JacksonAnnotation;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.KeyDeserializer;

/**
 * Annotation use for configuring deserialization aspects, by attaching
 * to "setter" methods or fields, or to value classes.
 * When annotating value classes, configuration is used for instances
 * of the value class but can be overridden by more specific annotations
 * (ones that attach to methods or fields).
 *<p>
 * An example annotation would be:
 *<pre>
 *  &#64;JsonDeserialize(using=MySerializer.class,
 *    as=MyHashMap.class,
 *    keyAs=MyHashKey.class,
 *    contentAs=MyHashValue.class
 *  )
 *</pre>
 *<p>
 * NOTE: since version 1.2, annotation has also been applicable
 * to (constructor) parameters
 *
 * @since 1.1
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonDeserialize
{
    // // // Annotations for explicitly specifying deserializer

    /**
     * Deserializer class to use for deserializing associated value.
     * Depending on what is annotated,
     * value is either an instance of annotated class (used globablly
     * anywhere where class deserializer is needed); or only used for
     * deserializing property access via a setter method.
     */
    public Class<? extends JsonDeserializer<?>> using()
        default JsonDeserializer.None.class;

    /**
     * Deserializer class to use for deserializing contents (elements
     * of a Collection/array, values of Maps) of annotated property.
     * Can only be used on instances (methods, fields, constructors),
     * and not value classes themselves.
     *
     * @since 1.3
     */
    public Class<? extends JsonDeserializer<?>> contentUsing()
        default JsonDeserializer.None.class;

    /**
     * Deserializer class to use for deserializing Map keys
     * of annotated property.
     * Can only be used on instances (methods, fields, constructors),
     * and not value classes themselves.
     *
     * @since 1.3
     */
    public Class<? extends KeyDeserializer> keyUsing()
        default KeyDeserializer.None.class;

    // // // Annotations for explicitly specifying deserialization type
    // // // (which is used for choosing deserializer, if not explicitly
    // // // specified

    /**
     * Concrete type to deserialize values as, instead of type otherwise
     * declared. Must be a subtype of declared type; otherwise an
     * exception may be thrown by deserializer.
     *<p>
     * Bogus type {@link NoClass} can be used to indicate that declared
     * type is used as is (i.e. this annotation property has no setting);
     * this since annotation properties are not allowed to have null value.
     *<p>
     * Note: if {@link #using} is also used it has precedence
     * (since it directly specified
     * deserializer, whereas this would only be used to locate the
     * deserializer)
     * and value of this annotation property is ignored.
     */
    public Class<?> as() default NoClass.class;

    /**
     * Concrete type to deserialize keys of {@link java.util.Map} as,
     * instead of type otherwise declared.
     * Must be a subtype of declared type; otherwise an exception may be
     * thrown by deserializer.
     */
    public Class<?> keyAs() default NoClass.class;

    /**
     * Concrete type to deserialize content (elements
     * of a Collection/array, values of Maps) values as,
     * instead of type otherwise declared.
     * Must be a subtype of declared type; otherwise an exception may be
     * thrown by deserializer.
     */
    public Class<?> contentAs() default NoClass.class;
}
