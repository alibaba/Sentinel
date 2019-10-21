package com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JacksonAnnotation;

/**
 * Annotation similar to {@link javax.xml.bind.annotation.XmlRootElement},
 * used to indicate name to use for root-level wrapping, if wrapping is
 * enabled. Annotation itself does not indicate that wrapping should
 * be used; but if it is, name used for serialization should be name
 * specified here, and deserializer will expect the name as well.
 * 
 * @since 1.9.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonRootName
{
    /**
     * Root name to use if root-level wrapping is enabled.
     */
    public String value();

}
