/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind.annotation;

import javax.xml.namespace.QName;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * <p>
 * Maps a JavaBean property to a map of wildcard attributes.
 *
 * <p> <b>Usage</b> </p>
 * <p>
 * The <tt>&#64;XmlAnyAttribute</tt> annotation can be used with the
 * following program elements: 
 * <ul> 
 *   <li> JavaBean property </li>
 *   <li> non static, non transient field </li>
 * </ul>
 *
 * <p>See "Package Specification" in javax.xml.bind.package javadoc for
 * additional common information.</p>
 * 
 * The usage is subject to the following constraints:
 * <ul> 
 *   <li> At most one field or property in a class can be annotated
 *        with <tt>&#64;XmlAnyAttribute</tt>.  </li>
 *   <li> The type of the property or the field must <tt>java.util.Map</tt> </li>
 * </ul>
 *
 * <p>
 * While processing attributes to be unmarshalled into a value class,
 * each attribute that is not statically associated with another 
 * JavaBean property, via {@link XmlAttribute}, is entered into the 
 * wildcard attribute map represented by 
 * {@link Map}&lt;{@link QName},{@link Object}>. The attribute QName is the
 * map's key. The key's value is the String value of the attribute.
 *
 * @author Kohsuke Kawaguchi, Sun Microsystems, Inc.
 * @since JAXB2.0
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlAnyAttribute {
}
