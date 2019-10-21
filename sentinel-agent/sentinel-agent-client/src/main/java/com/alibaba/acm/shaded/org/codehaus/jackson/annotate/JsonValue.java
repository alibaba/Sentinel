package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation similar to
 * {@link javax.xml.bind.annotation.XmlValue} 
 * that indicates that results of the annotated "getter" method
 * (which means signature must be that of getters; non-void return
 * type, no args) is to be used as the single value to serialize
 * for the instance. Usually value will be of a simple scalar type
 * (String or Number), but it can be any serializable type (Collection,
 * Map or Bean).
 *<p>
 * At most one method of a Class can be annotated with this annotation;
 * if more than one is found, an exception may be thrown.
 * Also, if method signature is not compatible with Getters, an exception
 * may be thrown.
 * Whether exception is thrown or not is an implementation detail (due
 * to filtering during introspection, some annotations may be skipped)
 * and applications should not rely on specific behavior.
 *<p>
 * A typical use case is that of annotating <code>toString()</code>
 * method so that returned String value is Object's Json serialization.
 *<p>
 * Boolean argument is only used so that sub-classes can "disable"
 * annotation if necessary.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonValue
{
    /**
     * Optional argument that defines whether this annotation is active
     * or not. The only use for value 'false' if for overriding purposes.
     * Overriding may be necessary when used
     * with "mix-in annotations" (aka "annotation overrides").
     * For most cases, however, default value of "true" is just fine
     * and should be omitted.
     */
    boolean value() default true;
}
