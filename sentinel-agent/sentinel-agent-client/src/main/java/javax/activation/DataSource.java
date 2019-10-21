/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)DataSource.java	1.12 07/05/14
 */

package javax.activation;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * The DataSource interface provides the JavaBeans Activation Framework
 * with an abstraction of an arbitrary collection of data.  It
 * provides a type for that data as well as access
 * to it in the form of <code>InputStreams</code> and
 * <code>OutputStreams</code> where appropriate.
 */

public interface DataSource {

    /**
     * This method returns an <code>InputStream</code> representing
     * the data and throws the appropriate exception if it can
     * not do so.  Note that a new <code>InputStream</code> object must be
     * returned each time this method is called, and the stream must be
     * positioned at the beginning of the data.
     *
     * @return an InputStream
     */
    public InputStream getInputStream() throws IOException;

    /**
     * This method returns an <code>OutputStream</code> where the
     * data can be written and throws the appropriate exception if it can
     * not do so.  Note that a new <code>OutputStream</code> object must
     * be returned each time this method is called, and the stream must
     * be positioned at the location the data is to be written.
     *
     * @return an OutputStream
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * This method returns the MIME type of the data in the form of a
     * string. It should always return a valid type. It is suggested
     * that getContentType return "application/octet-stream" if the
     * DataSource implementation can not determine the data type.
     *
     * @return the MIME Type
     */
    public String getContentType();

    /**
     * Return the <i>name</i> of this object where the name of the object
     * is dependant on the nature of the underlying objects. DataSources
     * encapsulating files may choose to return the filename of the object.
     * (Typically this would be the last component of the filename, not an
     * entire pathname.)
     *
     * @return the name of the object.
     */
    public String getName();
}
