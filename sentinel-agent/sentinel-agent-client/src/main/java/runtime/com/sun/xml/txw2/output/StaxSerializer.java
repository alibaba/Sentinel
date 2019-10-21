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

import com.sun.xml.txw2.TxwException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML serializer for StAX XMLStreamWriter.
 *
 * TODO: add support for XMLEventWriter (if it makes sense)
 *
 * @author Ryan.Shoemaker@Sun.COM
 */

public class StaxSerializer implements XmlSerializer {
    private final XMLStreamWriter out;

    public StaxSerializer(XMLStreamWriter writer) {
        this.out = writer;
    }

    public void startDocument() {
        try {
            out.writeStartDocument();
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void beginStartTag(String uri, String localName, String prefix) {
        try {
            out.writeStartElement(prefix, localName, uri);
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void writeAttribute(String uri, String localName, String prefix, StringBuilder value) {
        try {
            out.writeAttribute(prefix, uri, localName, value.toString());
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void writeXmlns(String prefix, String uri) {
        try {
            if (prefix.length() == 0) {
                out.setDefaultNamespace(uri);
            } else {
                out.setPrefix(prefix, uri);
            }

            // this method handles "", null, and "xmlns" prefixes properly
            out.writeNamespace(prefix, uri);
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void endStartTag(String uri, String localName, String prefix) {
        // NO-OP
    }

    public void endTag() {
        try {
            out.writeEndElement();
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void text(StringBuilder text) {
        try {
            out.writeCharacters(text.toString());
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void cdata(StringBuilder text) {
        try {
            out.writeCData(text.toString());
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void comment(StringBuilder comment) {
        try {
            out.writeComment(comment.toString());
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void endDocument() {
        try {
            out.writeEndDocument();
            out.flush();
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }

    public void flush() {
        try {
            out.flush();
        } catch (XMLStreamException e) {
            throw new TxwException(e);
        }
    }
}
