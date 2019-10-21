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
 * @(#)FileDataSource.java	1.10 07/05/14
 */

package javax.activation;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.sun.activation.registries.MimeTypeFile;

/**
 * The FileDataSource class implements a simple DataSource object
 * that encapsulates a file. It provides data typing services via
 * a FileTypeMap object. <p>
 *
 * <b>FileDataSource Typing Semantics</b><p>
 *
 * The FileDataSource class delegates data typing of files
 * to an object subclassed from the FileTypeMap class.
 * The <code>setFileTypeMap</code> method can be used to explicitly
 * set the FileTypeMap for an instance of FileDataSource. If no
 * FileTypeMap is set, the FileDataSource will call the FileTypeMap's
 * getDefaultFileTypeMap method to get the System's default FileTypeMap.
 *
 * @see javax.activation.DataSource
 * @see javax.activation.FileTypeMap
 * @see javax.activation.MimetypesFileTypeMap
 */
public class FileDataSource implements DataSource {

    // keep track of original 'ref' passed in, non-null
    // one indicated which was passed in:
    private File _file = null;
    private FileTypeMap typeMap = null;

    /**
     * Creates a FileDataSource from a File object. <i>Note:
     * The file will not actually be opened until a method is
     * called that requires the file to be opened.</i>
     *
     * @param file the file
     */
    public FileDataSource(File file) {
	_file = file;	// save the file Object...
    }

    /**
     * Creates a FileDataSource from
     * the specified path name. <i>Note:
     * The file will not actually be opened until a method is
     * called that requires the file to be opened.</i>
     *
     * @param name the system-dependent file name.
     */
    public FileDataSource(String name) {
	this(new File(name));	// use the file constructor
    }

    /**
     * This method will return an InputStream representing the
     * the data and will throw an IOException if it can
     * not do so. This method will return a new
     * instance of InputStream with each invocation.
     *
     * @return an InputStream
     */
    public InputStream getInputStream() throws IOException {
	return new FileInputStream(_file);
    }

    /**
     * This method will return an OutputStream representing the
     * the data and will throw an IOException if it can
     * not do so. This method will return a new instance of
     * OutputStream with each invocation.
     *
     * @return an OutputStream
     */
    public OutputStream getOutputStream() throws IOException {
	return new FileOutputStream(_file);
    }

    /**
     * This method returns the MIME type of the data in the form of a
     * string. This method uses the currently installed FileTypeMap. If
     * there is no FileTypeMap explictly set, the FileDataSource will
     * call the <code>getDefaultFileTypeMap</code> method on
     * FileTypeMap to acquire a default FileTypeMap. <i>Note: By
     * default, the FileTypeMap used will be a MimetypesFileTypeMap.</i>
     *
     * @return the MIME Type
     * @see javax.activation.FileTypeMap#getDefaultFileTypeMap
     */
    public String getContentType() {
	// check to see if the type map is null?
	if (typeMap == null)
	    return FileTypeMap.getDefaultFileTypeMap().getContentType(_file);
	else
	    return typeMap.getContentType(_file);
    }

    /**
     * Return the <i>name</i> of this object. The FileDataSource
     * will return the file name of the object.
     *
     * @return the name of the object.
     * @see javax.activation.DataSource
     */
    public String getName() {
	return _file.getName();
    }

    /**
     * Return the File object that corresponds to this FileDataSource.
     * @return the File object for the file represented by this object.
     */
    public File getFile() {
	return _file;
    }

    /**
     * Set the FileTypeMap to use with this FileDataSource
     *
     * @param map The FileTypeMap for this object.
     */
    public void setFileTypeMap(FileTypeMap map) {
	typeMap = map;
    }
}
