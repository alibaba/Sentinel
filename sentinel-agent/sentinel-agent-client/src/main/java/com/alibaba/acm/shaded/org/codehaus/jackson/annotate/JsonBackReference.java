package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that associated property is part of
 * two-way linkage between fields; and that its role is "child" (or "back") link.
 * Value type of the property must be a bean: it can not be a Collection, Map,
 * Array or enumeration.
 * Linkage is handled such that the property
 * annotated with this annotation is not serialized; and during deserialization,
 * its value is set to instance that has the "managed" (forward) link.
 *<p>
 * All references have logical name to allow handling multiple linkages; typical case
 * would be that where nodes have both parent/child and sibling linkages. If so,
 * pairs of references should be named differently.
 * It is an error for a class to have multiple back references with same name,
 * even if types pointed are different.
 *<p>
 * Note: only methods and fields can be annotated with this annotation: constructor
 * arguments should NOT be annotated, as they can not be either managed or back
 * references.
 * 
 * @author tatu
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonBackReference
{
    /**
     * Logical have for the reference property pair; used to link managed and
     * back references. Default name can be used if there is just single
     * reference pair (for example, node class that just has parent/child linkage,
     * consisting of one managed reference and matching back reference)
     */
    public String value() default "defaultReference";
}
