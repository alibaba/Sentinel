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

package com.sun.xml.txw2;

import com.sun.xml.txw2.output.XmlSerializer;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.txw2.annotation.XmlNamespace;

import javax.xml.namespace.QName;

/**
 * Entry point to TXW.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TXW {
    private TXW() {}    // no instanciation please


    /*package*/ static QName getTagName( Class<?> c ) {
        String localName="";
        String nsUri="##default";

        XmlElement xe = c.getAnnotation(XmlElement.class);
        if(xe!=null) {
            localName = xe.value();
            nsUri = xe.ns();
        }

        if(localName.length()==0) {
            localName = c.getName();
            int idx = localName.lastIndexOf('.');
            if(idx>=0)
                localName = localName.substring(idx+1);

            localName = Character.toLowerCase(localName.charAt(0))+localName.substring(1);
        }

        if(nsUri.equals("##default")) {
            Package pkg = c.getPackage();
            if(pkg!=null) {
                XmlNamespace xn = pkg.getAnnotation(XmlNamespace.class);
                if(xn!=null)
                    nsUri = xn.value();
            }
        }
        if(nsUri.equals("##default"))
            nsUri = "";

        return new QName(nsUri,localName);
    }

    /**
     * Creates a new {@link TypedXmlWriter} to write a new instance of a document.
     *
     * @param rootElement
     *      The {@link TypedXmlWriter} interface that declares the content model of the root element.
     *      This interface must have {@link XmlElement} annotation on it to designate the tag name
     *      of the root element.
     * @param out
     *      The target of the writing.
     */
    public static <T extends TypedXmlWriter> T create( Class<T> rootElement, XmlSerializer out ) {
        Document doc = new Document(out);
        QName n = getTagName(rootElement);
        return new ContainerElement(doc,null,n.getNamespaceURI(),n.getLocalPart())._cast(rootElement);
    }

    /**
     * Creates a new {@link TypedXmlWriter} to write a new instance of a document.
     *
     * <p>
     * Similar to the other method, but this version allows the caller to set the
     * tag name at the run-time.
     *
     * @param tagName
     *      The tag name of the root document.
     *
     * @see #create(Class,XmlSerializer)
     */
    public static <T extends TypedXmlWriter> T create( QName tagName, Class<T> rootElement, XmlSerializer out ) {
        return new ContainerElement(new Document(out),null,tagName.getNamespaceURI(),tagName.getLocalPart())._cast(rootElement);
    }
}
