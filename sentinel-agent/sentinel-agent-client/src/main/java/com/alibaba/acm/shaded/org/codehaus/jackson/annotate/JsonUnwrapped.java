package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that a property should be serialized
 * "unwrapped"; that is, if it would be serialized as JSON Object, its
 * properties are instead included as properties of its containing
 * Object. For example, consider case of POJO like:
 * 
 *<pre>
 *  public class Parent {
 *    public int age;
 *    public Name name;
 *  }
 *  public class Name {
 *    public String first, last;
 *  }
 *</pre>  
 * which would normally be serialized as follows (assuming @JsonUnwrapped
 * had no effect):
 *<pre>
 *  {
 *    "age" : 18,
 *    "name" : {
 *      "first" : "Joey",
 *      "last" : "Sixpack"
 *    }
 *  }
 *</pre>
 * can be changed to this:
 *<pre>
 *  {
 *    "age" : 18,
 *    "first" : "Joey",
 *    "last" : "Sixpack"
 *  }
 *</pre>
 * by changing Parent class to:
 *<pre>
 *  public class Parent {
 *    public int age;
 *    \@JsonUnwrapped
 *    public Name name;
 *  }
 *</pre>
 * Annotation can only be added to properties, and not classes, as it is contextual.
 *<p>
 * Also note that annotation only applies if
 *<ul>
 * <li>Value is serialized as JSON Object
 *   </li>
 * <li>Serialization is done using <code>BeanSerializer</code>, not a custom serializer
 *   </li>
 * <li>No type information is added; if type information needs to be added, structure can
 *   not be altered regardless of inclusion strategy; so annotation is basically ignored.
 *   </li>
 * </ul>
 * 
 * @since 1.9
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonUnwrapped
{
    /**
     * Property that is usually only used when overriding (masking) annotations,
     * using mix-in annotations. Otherwise default value of 'true' is fine, and
     * value need not be explicitly included.
     */
    boolean enabled() default true;
}
