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

import com.sun.xml.txw2.TypedXmlWriter;


/**
 * Low-level typeless XML writer driven from {@link TypedXmlWriter}.
 *
 * <p>
 * Applications can use one of the predefined implementations to
 * send TXW output to the desired location/format, or they can
 * choose to implement this interface for custom output.
 *
 * <p>
 * One {@link XmlSerializer} instance is responsible for writing
 * one XML document.
 *
 * <h2>Call Sequence</h2>
 * TXW calls methods on this interface in the following order:
 *
 * <pre>
 * WHOLE_SEQUENCE := startDocument ELEMENT endDocument
 * ELEMENT := beginStartTag writeXmlns* writeAttribute* endStartTag CONTENT endTag
 * CONTENT := (text|ELEMENT)*
 * </pre>
 *
 * <p>
 * TXW maintains all the in-scope namespace bindings and prefix allocation.
 * The {@link XmlSerializer} implementation should just use the prefix
 * specified.
 * </p>
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XmlSerializer {
    /**
     * The first method to be called.
     */
    void startDocument();

    /**
     * Begins writing a start tag.
     *
     * @param uri
     *      the namespace URI of the element. Can be empty but never be null.
     * @param prefix
     *      the prefix that should be used for this element. Can be empty,
     *      but never null.
     */
    void beginStartTag(String uri,String localName,String prefix);

    /**
     * Writes an attribute.
     *
     * @param value
     *      The value of the attribute. It's the callee's responsibility to
     *      escape special characters (such as &lt;, &gt;, and &amp;) in this buffer.
     *
     * @param uri
     *      the namespace URI of the attribute. Can be empty but never be null.
     * @param prefix
     *      the prefix that should be used for this attribute. Can be empty,
     *      but never null.
     */
    void writeAttribute(String uri,String localName,String prefix,StringBuilder value);

    /**
     * Writes a namespace declaration.
     *
     * @param uri
     *      the namespace URI to be declared. Can be empty but never be null.
     * @param prefix
     *      the prefix that is allocated. Can be empty but never be null.
     */
    void writeXmlns(String prefix,String uri);

    /**
     * Completes the start tag.
     *
     * @param uri
     *      the namespace URI of the element. Can be empty but never be null.
     * @param prefix
     *      the prefix that should be used for this element. Can be empty,
     *      but never null.
     */
    void endStartTag(String uri,String localName,String prefix);

    /**
     * Writes an end tag.
     */
    void endTag();

    /**
     * Writes PCDATA.
     *
     * @param text
     *      The character data to be written. It's the callee's responsibility to
     *      escape special characters (such as &lt;, &gt;, and &amp;) in this buffer.
     */
    void text(StringBuilder text);

    /**
     * Writes CDATA.
     */
    void cdata(StringBuilder text);

    /**
     * Writes a comment.
     *
     * @throws UnsupportedOperationException
     *      if the writer doesn't support writing a comment, it can throw this exception.
     */
    void comment(StringBuilder comment);

    /**
     * The last method to be called.
     */
    void endDocument();

    /**
     * Flush the buffer.
     *
     * This method is called when applications invoke {@link TypedXmlWriter#commit(boolean)}
     * method. If the implementation performs any buffering, it should flush the buffer.
     */
    void flush();
}
