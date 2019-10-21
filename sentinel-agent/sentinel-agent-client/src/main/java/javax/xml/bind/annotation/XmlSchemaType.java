/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Maps a Java type to a simple schema built-in type.
 *
 * <p> <b>Usage</b> </p>
 * <p>
 * <tt>@XmlSchemaType</tt> annotation can be used with the following program
 * elements: 
 * <ul> 
 *   <li> a JavaBean property </li>
 *   <li> field </li>
 *   <li> package</li>
 * </ul>
 *
 * <p> <tt>@XmlSchemaType</tt> annotation defined for Java type
 * applies to all references to the Java type from a property/field. 
 * A <tt>@XmlSchemaType</tt> annotation specified on the
 * property/field overrides the <tt>@XmlSchemaType</tt> annotation
 * specified at the package level.
 *
 * <p> This annotation can be used with the following annotations:
 * {@link XmlElement},  {@link XmlAttribute}.
 * <p>
 * <b>Example 1: </b> Customize mapping of XMLGregorianCalendar on the
 *  field.
 * 
 * <pre>
 *     //Example: Code fragment
 *     public class USPrice {
 *         &#64;XmlElement
 *         &#64;XmlSchemaType(name="date")
 *         public XMLGregorianCalendar date;
 *     }
 * 
 *     &lt;!-- Example: Local XML Schema element -->
 *     &lt;xs:complexType name="USPrice"/>
 *       &lt;xs:sequence>
 *         &lt;xs:element name="date" type="xs:date"/>
 *       &lt;/sequence>
 *     &lt;/xs:complexType>
 * </pre>
 *
 * <p> <b> Example 2: </b> Customize mapping of XMLGregorianCalendar at package
 *     level </p>
 * <pre>
 *     package foo;
 *     &#64;javax.xml.bind.annotation.XmlSchemaType(
 *          name="date", type=javax.xml.datatype.XMLGregorianCalendar.class)
 *     }
 * </pre>
 * 
 * @since JAXB2.0
 */

@Retention(RUNTIME) @Target({FIELD,METHOD,PACKAGE})        
public @interface XmlSchemaType {
    String name();
    String namespace() default "http://www.w3.org/2001/XMLSchema";
    /**
     * If this annotation is used at the package level, then value of
     * the type() must be specified.
     */

    Class type() default DEFAULT.class;

    /**
     * Used in {@link XmlSchemaType#type()} to
     * signal that the type be inferred from the signature
     * of the property.
     */

    static final class DEFAULT {}

}



