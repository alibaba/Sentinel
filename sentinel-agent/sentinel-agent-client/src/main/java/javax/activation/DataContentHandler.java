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
 * @(#)DataContentHandler.java	1.17 07/05/14
 */

package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.DataSource;

/**
 * The DataContentHandler interface is implemented by objects that can
 * be used to extend the capabilities of the DataHandler's implementation
 * of the Transferable interface. Through <code>DataContentHandlers</code>
 * the framework can be extended to convert streams in to objects, and
 * to write objects to streams. <p>
 *
 * Applications don't generally call the methods in DataContentHandlers
 * directly. Instead, an application calls the equivalent methods in
 * DataHandler. The DataHandler will attempt to find an appropriate
 * DataContentHandler that corresponds to its MIME type using the
 * current DataContentHandlerFactory. The DataHandler then calls
 * through to the methods in the DataContentHandler.
 */

public interface DataContentHandler {
    /**
     * Returns an array of DataFlavor objects indicating the flavors the
     * data can be provided in. The array should be ordered according to
     * preference for providing the data (from most richly descriptive to
     * least descriptive).
     *
     * @return The DataFlavors.
     */
    public DataFlavor[] getTransferDataFlavors();

    /**
     * Returns an object which represents the data to be transferred.
     * The class of the object returned is defined by the representation class
     * of the flavor.
     *
     * @param df The DataFlavor representing the requested type.
     * @param ds The DataSource representing the data to be converted.
     * @return The constructed Object.
     * @exception UnsupportedFlavorException	if the handler doesn't
     *						support the requested flavor
     * @exception IOException	if the data can't be accessed
     */
    public Object getTransferData(DataFlavor df, DataSource ds)
				throws UnsupportedFlavorException, IOException;

    /**
     * Return an object representing the data in its most preferred form.
     * Generally this will be the form described by the first DataFlavor
     * returned by the <code>getTransferDataFlavors</code> method.
     *
     * @param ds The DataSource representing the data to be converted.
     * @return The constructed Object.
     * @exception IOException	if the data can't be accessed
     */
    public Object getContent(DataSource ds) throws IOException;

    /**
     * Convert the object to a byte stream of the specified MIME type
     * and write it to the output stream.
     *
     * @param obj	The object to be converted.
     * @param mimeType	The requested MIME type of the resulting byte stream.
     * @param os	The output stream into which to write the converted
     *			byte stream.
     * @exception IOException	errors writing to the stream
     */
    public void writeTo(Object obj, String mimeType, OutputStream os)
	                                               throws IOException;
}
