/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.awt.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import javax.xml.transform.Source;

/**
 * Associates the MIME type that controls the XML representation of the property.
 *
 * <p>
 * This annotation is used in conjunction with datatypes such as
 * {@link Image} or {@link Source} that are bound to base64-encoded binary in XML.
 *
 * <p>
 * If a property that has this annotation has a sibling property bound to
 * the xmime:contentType attribute, and if in the instance the property has a value,
 * the value of the attribute takes precedence and that will control the marshalling. 
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB2.0
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD,PARAMETER})
public @interface XmlMimeType {
    /**
     * The textual representation of the MIME type,
     * such as "image/jpeg" "image/*", "text/xml; charset=iso-8859-1" and so on.
     */
    String value();
}
