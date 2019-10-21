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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that the invocation of the method will produce an attribute.
 *
 * <p>
 * The method signature has to match the form <tt>R foo(DT1,DT2,..)</tt>
 *
 * <p>
 * R is either <tt>void</tt> or the type to which the interface that declares
 * this method is assignable. In the case of the latter, the method will return
 * <tt>this</tt> object, allowing you to chain the multiple attribute method
 * invocations like {@link StringBuffer}.
 *
 * <p>
 * DTi must be datatype objects.
 *
 * <p>
 * When this method is called, a new attribute is added to the current element,
 * whose value is whitespace-separated text from each of the datatype objects.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface XmlAttribute {
    /**
     * The local name of the attribute.
     *
     * <p>
     * If left unspecified, the method name is used as the attribute name.
     *
     */
    String value() default "";

    /**
     * The namespace URI of the attribute.
     */
    String ns() default "";
}
