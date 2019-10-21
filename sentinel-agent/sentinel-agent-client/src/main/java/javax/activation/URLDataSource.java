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
 * @(#)URLDataSource.java	1.11 07/05/14
 */

package javax.activation;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * The URLDataSource class provides an object that wraps a <code>URL</code>
 * object in a DataSource interface. URLDataSource simplifies the handling
 * of data described by URLs within the JavaBeans Activation Framework
 * because this class can be used to create new DataHandlers. <i>NOTE: The
 * DataHandler object creates a URLDataSource internally,
 * when it is constructed with a URL.</i>
 *
 * @see javax.activation.DataSource
 * @see javax.activation.DataHandler
 */
public class URLDataSource implements DataSource {
    private URL url = null;
    private URLConnection url_conn = null;

    /**
     * URLDataSource constructor. The URLDataSource class will
     * not open a connection to the URL until a method requiring it
     * to do so is called.
     *
     * @param url The URL to be encapsulated in this object.
     */
    public URLDataSource(URL url) {
	this.url = url;
    }

    /**
     * Returns the value of the URL content-type header field.
     * It calls the URL's <code>URLConnection.getContentType</code> method
     * after retrieving a URLConnection object.
     * <i>Note: this method attempts to call the <code>openConnection</code>
     * method on the URL. If this method fails, or if a content type is not
     * returned from the URLConnection, getContentType returns
     * "application/octet-stream" as the content type.</i>
     *
     * @return the content type.
     */
    public String getContentType() {
	String type = null;

	try {
	    if (url_conn == null)
		url_conn = url.openConnection();
	} catch (IOException e) { }
	
	if (url_conn != null)
	    type = url_conn.getContentType();

	if (type == null)
	    type = "application/octet-stream";
	
	return type;
    }

    /**
     * Calls the <code>getFile</code> method on the URL used to
     * instantiate the object.
     *
     * @return the result of calling the URL's getFile method.
     */
    public String getName() {
	return url.getFile();
    }

    /**
     * The getInputStream method from the URL. Calls the
     * <code>openStream</code> method on the URL.
     *
     * @return the InputStream.
     */
    public InputStream getInputStream() throws IOException {
	return url.openStream();
    }

    /**
     * The getOutputStream method from the URL. First an attempt is
     * made to get the URLConnection object for the URL. If that
     * succeeds, the getOutputStream method on the URLConnection
     * is returned.
     *
     * @return the OutputStream.
     */
    public OutputStream getOutputStream() throws IOException {
	// get the url connection if it is available
	url_conn = url.openConnection();
	
	if (url_conn != null) {
	    url_conn.setDoOutput(true);
	    return url_conn.getOutputStream();
	} else
	    return null;
    }

    /**
     * Return the URL used to create this DataSource.
     *
     * @return The URL.
     */
    public URL getURL() {
	return url;
    }
}
