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

import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.txw2.output.XmlSerializer;

import javax.xml.namespace.QName;

/**
 * Defines common operations for all typed XML writers&#x2E;
 * Root of all typed XML writer interfaces.
 *
 * <p>
 * This interface defines a series of methods to allow client applications
 * to write arbitrary well-formed documents.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypedXmlWriter {
    /**
     * Commits this element (and all its descendants) to the output.
     *
     * <p>
     * Short for <tt>_commit(true)</tt>.
     */
    void commit();

    /**
     * Commits this element (and all its descendants) to the output.
     *
     * <p>
     * Once a writer is committed, nothing can be added to it further.
     * Committing allows TXW to output a part of the document even
     * if the rest has not yet been written.
     *
     * @param includingAllPredecessors
     *      if false, this operation will _commit this writer and all its
     *      descendants writers. If true, in addition to those writers,
     *      this operation will close all the writers before this writer
     *      in the document order.
     */
    void commit(boolean includingAllPredecessors);

    /**
     * Blocks the writing of the start tag so that
     * new attributes can be added even after child
     * elements are appended.
     *
     * <p>
     * This blocks the output at the token before the start tag until
     * the {@link #commit()} method is called to _commit this element.
     *
     * <p>
     * For more information, see the TXW documentation.
     */
    void block();

    /**
     * Gets the {@link Document} object that this writer is writing to.
     *
     * @return
     *      always non-null.
     */
    Document getDocument();

    /**
     * Adds an attribute of the given name and the value.
     *
     * <p>
     * Short for <pre>_attribute("",localName,value);</pre>
     *
     * @see #_attribute(String, String, Object)
     */
    void _attribute( String localName, Object value );

    /**
     * Adds an attribute of the given name and the value.
     *
     * <p>
     * Short for <pre>_attribute(new QName(nsUri,localName),value);</pre>
     *
     * @see #_attribute(QName, Object)
     */
    void _attribute( String nsUri, String localName, Object value );

    /**
     * Adds an attribute of the given name and the value.
     *
     * @param attributeName
     *      must not be null.
     * @param value
     *      value of the attribute.
     *      must not be null.
     *      See the documentation for the conversion rules.
     */
    void _attribute( QName attributeName, Object value );

    /**
     * Declares a new namespace URI on this element.
     *
     * <p>
     * The runtime system will assign an unique prefix for the URI.
     *
     * @param uri
     *      can be empty, but must not be null.
     */
    void _namespace( String uri );

    /**
     * Declares a new namespace URI on this element to
     * a specific prefix.
     *
     * @param uri
     *      can be empty, but must not be null.
     * @param prefix
     *      If non-empty, this prefix is bound to the URI
     *      on this element. If empty, then the runtime will still try to
     *      use the URI as the default namespace, but it may fail to do so
     *      because of the constraints in the XML.
     *
     * @throws IllegalArgumentException
     *      if the same prefix is already declared on this element.
     */
    void _namespace( String uri, String prefix );

    /**
     * Declares a new namespace URI on this element.
     *
     * <p>
     * The runtime system will assign an unique prefix for the URI.
     *
     * @param uri
     *      can be empty, but must not be null.
     * @param requirePrefix
     *      if false, this method behaves just like {@link #_namespace(String)}.
     *      if true, this guarantees that the URI is bound to a non empty prefix.
     */
    void _namespace( String uri, boolean requirePrefix );

    /**
     * Appends text data.
     *
     * @param value
     *      must not be null.
     *      See the documentation for the conversion rules.
     */
    void _pcdata( Object value );

    /**
     * Appends CDATA section.
     *
     * @param value
     *      must not be null.
     *      See the documentation for the conversion rules.
     */
    void _cdata( Object value );

    /**
     * Appends a comment.
     *
     * @param value
     *      must not be null.
     *      See the documentation for the conversion rules.
     *
     * @throws UnsupportedOperationException
     *      if the underlying {@link XmlSerializer} does not support
     *      writing comments, this exception can be thrown.
     */
    void _comment( Object value ) throws UnsupportedOperationException;

    /**
     * Appends a new child element.
     *
     * <p>
     * Short for <pre>_element(<i>URI of this element</i>,localName,contentModel);</pre>
     *
     * <p>
     * The namespace URI will be inherited from the parent element.
     *
     * @see #_element(String, String, Class)
     */
    <T extends TypedXmlWriter> T _element( String localName, Class<T> contentModel );

    /**
     * Appends a new child element.
     *
     * <p>
     * The newly created child element is appended at the end of the children.
     *
     * @param nsUri
     *      The namespace URI of the newly created element.
     * @param localName
     *      The local name of the newly created element.
     * @param contentModel
     *      The typed XML writer interface used to write the children of
     *      the new child element.
     *
     * @return
     *      always return non-null {@link TypedXmlWriter} that can be used
     *      to write the contents of the newly created child element.
     */
    <T extends TypedXmlWriter> T _element( String nsUri, String localName, Class<T> contentModel );

    /**
     * Appends a new child element.
     *
     * <p>
     * Short for <pre>_element(tagName.getNamespaceURI(),tagName.getLocalPart(),contentModel);</pre>
     *
     * @see #_element(String, String, Class)
     */
    <T extends TypedXmlWriter> T _element( QName tagName, Class<T> contentModel );

    /**
     * Appends a new child element.
     *
     * <p>
     * This version of the _element method requires the <i>T</i> class to be
     * annotated with {@link XmlElement} annotation. The element name will be
     * taken from there.
     *
     * @see #_element(String, String, Class)
     */
    <T extends TypedXmlWriter> T _element( Class<T> contentModel );

    /**
     * Returns a different interface for this typed XML Writer.
     *
     * <p>
     * Semantically, this operation is a 'cast' --- it returns the same underlying
     * writer in a different interface. The returned new writer and the current writer
     * will write to the same element.
     *
     * <p>
     * But this is different from Java's ordinary cast because the returned object
     * is not always the same as the current object.
     *
     * @return
     *      always return non-null.
     */
    <T extends TypedXmlWriter> T _cast( Class<T> targetInterface );
}
