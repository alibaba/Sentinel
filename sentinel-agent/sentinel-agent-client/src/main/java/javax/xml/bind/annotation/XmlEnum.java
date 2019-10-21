/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind.annotation;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * <p>
 * Maps an enum type {@link Enum} to XML representation.
 *
 * <p>This annotation, together with {@link XmlEnumValue} provides a
 * mapping of enum type to XML representation.
 *
 * <p> <b>Usage</b> </p>
 * <p>
 * The <tt>@XmlEnum</tt> annotation can be used with the
 * following program elements: 
 * <ul> 
 *   <li>enum type</li>
 * </ul>
 *
 * <p> The usage is subject to the following constraints:
 * <ul> 
 *   <li> This annotation can be used the following other annotations: 
 *         {@link XmlType},
 *         {@link XmlRootElement} </li>
 * </ul>
 * <p>See "Package Specification" in javax.xml.bind.package javadoc for
 * additional common information </p>
 *
 * <p>An enum type is mapped to a schema simple type with enumeration
 * facets. The schema type is derived from the Java type to which 
 * <tt>@XmlEnum.value()</tt>. Each enum constant <tt>@XmlEnumValue</tt>
 * must have a valid lexical representation for the type 
 * <tt>@XmlEnum.value()</tt> .
 *
 * <p><b>Examples:</b> See examples in {@link XmlEnumValue}
 *
 * @since JAXB2.0
 */

@Retention(RUNTIME) @Target({TYPE})
public @interface XmlEnum {
    /**
     * Java type that is mapped to a XML simple type.
     *
     */
    Class<?> value() default String.class;
}
