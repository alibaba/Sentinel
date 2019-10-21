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
 * @(#)ActivationDataFlavor.java	1.16 07/07/13
 */

package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import javax.activation.MimeType;

/**
 * The ActivationDataFlavor class is a special subclass of
 * <code>java.awt.datatransfer.DataFlavor</code>. It allows the JAF to
 * set all three values stored by the DataFlavor class via a new
 * constructor. It also contains improved MIME parsing in the <code>equals
 * </code> method. Except for the improved parsing, its semantics are
 * identical to that of the JDK's DataFlavor class.
 */

public class ActivationDataFlavor extends DataFlavor {

    /*
     * Raison d'etre:
     *
     * The DataFlavor class included in JDK 1.1 has several limitations
     * including piss poor MIME type parsing, and the limitation of
     * only supporting serialized objects and InputStreams as
     * representation objects. This class 'fixes' that.
     */

    // I think for now I'll keep copies of all the variables and
    // then later I may choose try to better coexist with the base
    // class *sigh*
    private String mimeType = null;
    private MimeType mimeObject = null;
    private String humanPresentableName = null;
    private Class representationClass = null;

    /**
     * Construct a DataFlavor that represents an arbitrary
     * Java object. This constructor is an extension of the
     * JDK's DataFlavor in that it allows the explicit setting
     * of all three DataFlavor attributes.
     * <p>
     * The returned DataFlavor will have the following characteristics:
     * <p>
     * representationClass = representationClass<br>
     * mimeType            = mimeType<br>
     * humanName           = humanName
     * <p>
     *
     * @param representationClass the class used in this DataFlavor
     * @param mimeType the MIME type of the data represented by this class
     * @param humanPresentableName the human presentable name of the flavor
     */
    public ActivationDataFlavor(Class representationClass,
		      String mimeType, String humanPresentableName) {
	super(mimeType, humanPresentableName); // need to call super

	// init private variables:
	this.mimeType = mimeType;
	this.humanPresentableName = humanPresentableName;
	this.representationClass = representationClass;
    }

    /**
     * Construct a DataFlavor that represents a MimeType.
     * <p>
     * The returned DataFlavor will have the following characteristics:
     * <p>
     * If the mimeType is "application/x-java-serialized-object;
     * class=", the result is the same as calling new
     * DataFlavor(Class.forName()) as above.
     * <p>
     * otherwise:
     * <p>
     * representationClass = InputStream<p>
     * mimeType = mimeType<p>
     *
     * @param representationClass the class used in this DataFlavor
     * @param humanPresentableName the human presentable name of the flavor
     */
    public ActivationDataFlavor(Class representationClass,
				String humanPresentableName) {
	super(representationClass, humanPresentableName);
	this.mimeType = super.getMimeType();
	this.representationClass = representationClass;
      	this.humanPresentableName = humanPresentableName;
    }

    /**
     * Construct a DataFlavor that represents a MimeType.
     * <p>
     * The returned DataFlavor will have the following characteristics:
     * <p>
     * If the mimeType is "application/x-java-serialized-object; class=",
     * the result is the same as calling new DataFlavor(Class.forName()) as
     * above, otherwise:
     * <p>
     * representationClass = InputStream<p>
     * mimeType = mimeType
     *
     * @param mimeType the MIME type of the data represented by this class
     * @param humanPresentableName the human presentable name of the flavor
     */
    public ActivationDataFlavor(String mimeType, String humanPresentableName) {
	super(mimeType, humanPresentableName);
	this.mimeType = mimeType;
	try {
	    this.representationClass = Class.forName("java.io.InputStream");
	} catch (ClassNotFoundException ex) {
	    // XXX - should never happen, ignore it
	}
      	this.humanPresentableName = humanPresentableName;
    }

    /**
     * Return the MIME type for this DataFlavor.
     *
     * @return	the MIME type
     */
    public String getMimeType() {
	return mimeType;
    }

    /**
     * Return the representation class.
     *
     * @return	the representation class
     */
    public Class getRepresentationClass() {
	return representationClass;
    }

    /**
     * Return the Human Presentable name.
     *
     * @return	the human presentable name
     */
    public String getHumanPresentableName() {
	return humanPresentableName;
    }

    /**
     * Set the human presentable name.
     *
     * @param humanPresentableName	the name to set
     */
    public void setHumanPresentableName(String humanPresentableName) {
	this.humanPresentableName = humanPresentableName;
    }

    /**
     * Compares the DataFlavor passed in with this DataFlavor; calls
     * the <code>isMimeTypeEqual</code> method.
     *
     * @param dataFlavor	the DataFlavor to compare with
     * @return			true if the MIME type and representation class
     *				are the same
     */
    public boolean equals(DataFlavor dataFlavor) {
	return (isMimeTypeEqual(dataFlavor) &&
	 	dataFlavor.getRepresentationClass() == representationClass);
    }

    /**
     * Is the string representation of the MIME type passed in equivalent
     * to the MIME type of this DataFlavor. <p>
     *
     * ActivationDataFlavor delegates the comparison of MIME types to
     * the MimeType class included as part of the JavaBeans Activation
     * Framework. This provides a more robust comparison than is normally
     * available in the DataFlavor class.
     *
     * @param mimeType	the MIME type
     * @return		true if the same MIME type
     */
    public boolean isMimeTypeEqual(String mimeType) {
	MimeType mt = null;
	try {
	    if (mimeObject == null)
		mimeObject = new MimeType(this.mimeType);
	    mt = new MimeType(mimeType);
	} catch (MimeTypeParseException e) {
	    // something didn't parse, do a crude comparison
	    return this.mimeType.equalsIgnoreCase(mimeType);
	}

	return mimeObject.match(mt);
    }

    /**
     * Called on DataFlavor for every MIME Type parameter to allow DataFlavor
     * subclasses to handle special parameters like the text/plain charset
     * parameters, whose values are case insensitive.  (MIME type parameter
     * values are supposed to be case sensitive).
     * <p>
     * This method is called for each parameter name/value pair and should
     * return the normalized representation of the parameterValue.
     * This method is never invoked by this implementation.
     *
     * @param parameterName	the parameter name
     * @param parameterValue	the parameter value
     * @return			the normalized parameter value
     * @deprecated
     */
    protected String normalizeMimeTypeParameter(String parameterName,
						String parameterValue) {
	return parameterValue;
    }

    /**
     * Called for each MIME type string to give DataFlavor subtypes the
     * opportunity to change how the normalization of MIME types is
     * accomplished.
     * One possible use would be to add default parameter/value pairs in cases
     * where none are present in the MIME type string passed in.
     * This method is never invoked by this implementation.
     *
     * @param mimeType	the MIME type
     * @return		the normalized MIME type
     * @deprecated
     */
    protected String normalizeMimeType(String mimeType) {
	return mimeType;
    }
}
