package com.alibaba.acm.shaded.org.codehaus.jackson.schema;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JacksonAnnotation;

/**
 * Annotation that can be used to define JSON Schema definition for
 * the annotated class.
 *<p>
 * Note that annotation is often not needed: for example, regular
 * Jackson beans that Jackson can introspect can be used without
 * annotations, to produce JSON schema definition.
 * 
 * @author Ryan Heaton
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonSerializableSchema
{
    /**
     * The schema type for this JsonSerializable instance.
     * Possible values: "string", "number", "boolean", "object", "array", "null", "any"
     *
     * @return The schema type for this JsonSerializable instance.
     */
    String schemaType() default "any";

    /**
     * If the schema type is "object", the node that defines the properties of the object.
     *
     * @return The node representing the schema properties, or "##irrelevant" if irrelevant.
     */
    String schemaObjectPropertiesDefinition() default "##irrelevant";

    /**
     * If the schema type if "array", the node that defines the schema for the items in the array.
     *
     * @return The schema for the items in the array, or "##irrelevant" if irrelevant.
     */
    String schemaItemDefinition() default "##irrelevant";
}
