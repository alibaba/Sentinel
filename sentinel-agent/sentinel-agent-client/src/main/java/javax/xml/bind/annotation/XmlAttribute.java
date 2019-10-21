/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * <p>
 * Maps a JavaBean property to a XML attribute. 
 *
 * <p> <b>Usage</b> </p>
 * <p>
 * The <tt>@XmlAttribute</tt> annotation can be used with the
 * following program elements: 
 * <ul> 
 *   <li> JavaBean property </li>
 *   <li> field </li>
 * </ul>
 *
 * <p> A static final field is mapped to a XML fixed attribute.
 *
 * <p>See "Package Specification" in javax.xml.bind.package javadoc for
 * additional common information.</p>
 *
 * The usage is subject to the following constraints:
 * <ul>
 *   <li> If type of the field or the property is a collection
 *        type, then the collection item type must be mapped to schema
 *        simple type.
 * <pre>
 *     // Examples
 *     &#64;XmlAttribute List&lt;Integer> items; //legal
 *     &#64;XmlAttribute List&lt;Bar> foo; // illegal if Bar does not map to a schema simple type
 * </pre> 
 *   </li>
 *   <li> If the type of the field or the property is a non
 *         collection type, then the type of the property or field
 *         must map to a simple schema type.
 * <pre>
 *     // Examples
 *     &#64;XmlAttribute int foo; // legal
 *     &#64;XmlAttribute Foo foo; // illegal if Foo does not map to a schema simple type
 * </pre>
 *   </li>
 *   <li> This annotation can be used with the following annotations:
 *            {@link XmlID}, 
 *            {@link XmlIDREF},
 *            {@link XmlList},
 *            {@link XmlSchemaType},
 *            {@link XmlValue},
 *            {@link XmlAttachmentRef},
 *            {@link XmlMimeType},
 *            {@link XmlInlineBinaryData},
 *            {@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter}</li>.
 * </ul>
 * </p>
 *
 * <p> <b>Example 1: </b>Map a JavaBean property to an XML attribute.</p>
 * <pre>
 *     //Example: Code fragment
 *     public class USPrice { 
 *         &#64;XmlAttribute
 *         public java.math.BigDecimal getPrice() {...} ;
 *         public void setPrice(java.math.BigDecimal ) {...};
 *     }
 *
 *     &lt;!-- Example: XML Schema fragment -->
 *     &lt;xs:complexType name="USPrice">
 *       &lt;xs:sequence>
 *       &lt;/xs:sequence>
 *       &lt;xs:attribute name="price" type="xs:decimal"/>
 *     &lt;/xs:complexType>
 * </pre>
 *
 * <p> <b>Example 2: </b>Map a JavaBean property to an XML attribute with anonymous type.</p>
 * See Example 7 in @{@link XmlType}.
 *
 * <p> <b>Example 3: </b>Map a JavaBean collection property to an XML attribute.</p>
 * <pre>
 *     // Example: Code fragment
 *     class Foo {
 *         ...
 *         &#64;XmlAttribute List&lt;Integer> items;
 *     } 
 *
 *     &lt;!-- Example: XML Schema fragment -->
 *     &lt;xs:complexType name="foo">
 *     	 ...
 *       &lt;xs:attribute name="items">
 *         &lt;xs:simpleType>
 *           &lt;xs:list itemType="xs:int"/>
 *         &lt;/xs:simpleType>
 *     &lt;/xs:complexType>
 *
 * </pre>
 * @author Sekhar Vajjhala, Sun Microsystems, Inc.
 * @version $Revision: 1.14 $
 * @see XmlType
 * @since JAXB2.0
 */

@Retention(RUNTIME) @Target({FIELD, METHOD})
public @interface XmlAttribute {
    /**
     * Name of the XML Schema attribute. By default, the XML Schema
     * attribute name is derived from the JavaBean property name.
     *
     */
    String name() default "##default";
 
    /**
     * Specifies if the XML Schema attribute is optional or
     * required. If true, then the JavaBean property is mapped to a
     * XML Schema attribute that is required. Otherwise it is mapped
     * to a XML Schema attribute that is optional.
     *
     */
     boolean required() default false;

    /**
     * Specifies the XML target namespace of the XML Schema
     * attribute.
     * 
     */
    String namespace() default "##default" ;
}
