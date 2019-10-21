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

package com.sun.xml.txw2.output;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

/**
 * Factory for producing XmlSerializers for various Result types.
 *
 * @author Ryan.Shoemaker@Sun.COM
 */
public abstract class ResultFactory {

    /**
     * Do not instanciate.
     */
    private ResultFactory() {}

    /**
     * Factory method for producing {@link XmlSerializer) from {@link javax.xml.transform.Result}.
     *
     * This method supports {@link javax.xml.transform.sax.SAXResult},
     * {@link javax.xml.transform.stream.StreamResult}, and {@link javax.xml.transform.dom.DOMResult}.
     *
     * @param result the Result that will receive output from the XmlSerializer 
     * @return an implementation of XmlSerializer that will produce output on the supplied Result
     */
    public static XmlSerializer createSerializer(Result result) {
        if (result instanceof SAXResult)
            return new SaxSerializer((SAXResult) result);
        if (result instanceof DOMResult)
            return new DomSerializer((DOMResult) result);
        if (result instanceof StreamResult)
            return new StreamSerializer((StreamResult) result);

        throw new UnsupportedOperationException("Unsupported Result type: " + result.getClass().getName());
    }

}
