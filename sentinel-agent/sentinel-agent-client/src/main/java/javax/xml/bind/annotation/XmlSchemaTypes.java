/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.PACKAGE;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * A container for multiple @{@link XmlSchemaType} annotations.
 *
 * <p> Multiple annotations of the same type are not allowed on a program
 * element. This annotation therefore serves as a container annotation
 * for multiple &#64;XmlSchemaType annotations as follows:
 *
 * <pre>
 * &#64;XmlSchemaTypes({ @XmlSchemaType(...), @XmlSchemaType(...) })
 * </pre>
 * <p>The <tt>@XmlSchemaTypes</tt> annnotation can be used to
 * define {@link XmlSchemaType} for different types at the
 * package level.
 *
 * <p>See "Package Specification" in javax.xml.bind.package javadoc for
 * additional common information.</p>
 *
 * @author <ul><li>Sekhar Vajjhala, Sun Microsystems, Inc.</li></ul>
 * @see XmlSchemaType
 * @since JAXB2.0
 */
@Retention(RUNTIME) @Target({PACKAGE})
public @interface XmlSchemaTypes {
    /**
     * Collection of @{@link XmlSchemaType} annotations
     */
    XmlSchemaType[] value();
}
