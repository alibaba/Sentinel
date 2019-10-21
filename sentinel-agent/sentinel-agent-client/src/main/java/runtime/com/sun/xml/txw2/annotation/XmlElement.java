/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.txw2.annotation;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.XmlSerializer;

import javax.xml.namespace.QName;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Specifies the name of the XML element.
 *
 * <h2>Used on method</h2>
 * <p>
 * When used on methods declared on interfaces that derive
 * from {@link TypedXmlWriter}, it specifies that the invocation
 * of the method will produce an element of the specified name.
 *
 * <p>
 * The method signature has to match one of the following patterns.
 *
 * <dl>
 *  <dt>Child writer: <tt>TW foo()</tt></dt>
 *  <dd>TW must be an interface derived from {@link TypedXmlWriter}.
 *      When this method is called, a new child element is started,
 *      and its content can be written by using the returned <tt>TW</tt>
 *      object. This child element will be ended when its _commit method
 *      is called.
 *  <dt>Leaf element: <tt>void foo(DT1,DT2,...)</tt></dt>
 *  <dd>DTi must be datatype objects.
 *      When this method is called, a new child element is started,
 *      followed by the whitespace-separated text data from each of
 *      the datatype objects, followed by the end tag.
 * </dl>
 *
 * <h2>Used on interface</h2>
 * <p>
 * When used on interfaces that derive from {@link TypedXmlWriter},
 * it associates an element name with that interface. This name is
 * used in a few places, such as in {@link TXW#create(Class,XmlSerializer)}
 * and {@link TypedXmlWriter#_element(Class)}.
 *
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD,TYPE})
public @interface XmlElement {
    /**
     * The local name of the element.
     */
    String value() default "";

    /**
     * The namespace URI of this element.
     *
     * <p>
     * If the annotation is on an interface and this paramter is left unspecified,
     * then the namespace URI is taken from {@link XmlNamespace} annotation on
     * the package that the interface is in. If {@link XmlNamespace} annotation
     * doesn't exist, the namespace URI will be "".
     *
     * <p>
     * If the annotation is on a method and this parameter is left unspecified,
     * then the namespace URI is the same as the namespace URI of the writer interface.
     */
    String ns() default "##default";
}
