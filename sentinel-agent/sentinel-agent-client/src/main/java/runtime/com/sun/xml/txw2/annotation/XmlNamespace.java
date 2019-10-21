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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.PACKAGE;
import com.sun.xml.txw2.TypedXmlWriter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Declares the namespace URI of the {@link TypedXmlWriter}s
 * in a package.
 *
 * <p>
 * This annotation is placed on a package. When specified,
 * it sets the default value of the namespace URI for
 * all the elements ({@link XmlElement}s) in the given package.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({PACKAGE})
public @interface XmlNamespace {
    /**
     * The namespace URI.
     */
    String value();
}
