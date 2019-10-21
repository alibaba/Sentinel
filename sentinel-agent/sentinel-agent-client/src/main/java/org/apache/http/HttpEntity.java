/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An entity that can be sent or received with an HTTP message.
 * Entities can be found in some
 * {@link HttpEntityEnclosingRequest requests} and in
 * {@link HttpResponse responses}, where they are optional.
 * <p>
 * There are three distinct types of entities in HttpCore,
 * depending on where their {@link #getContent content} originates:
 * <ul>
 * <li><b>streamed</b>: The content is received from a stream, or
 *     generated on the fly. In particular, this category includes
 *     entities being received from a {@link HttpConnection connection}.
 *     {@link #isStreaming Streamed} entities are generally not
 *      {@link #isRepeatable repeatable}.
 *     </li>
 * <li><b>self-contained</b>: The content is in memory or obtained by
 *     means that are independent from a connection or other entity.
 *     Self-contained entities are generally {@link #isRepeatable repeatable}.
 *     </li>
 * <li><b>wrapping</b>: The content is obtained from another entity.
 *     </li>
 * </ul>
 * This distinction is important for connection management with incoming
 * entities. For entities that are created by an application and only sent
 * using the HTTP components framework, the difference between streamed
 * and self-contained is of little importance. In that case, it is suggested
 * to consider non-repeatable entities as streamed, and those that are
 * repeatable (without a huge effort) as self-contained.
 *
 * @since 4.0
 */
public interface HttpEntity {

    /**
     * Tells if the entity is capable of producing its data more than once.
     * A repeatable entity's getContent() and writeTo(OutputStream) methods
     * can be called more than once whereas a non-repeatable entity's can not.
     * @return true if the entity is repeatable, false otherwise.
     */
    boolean isRepeatable();

    /**
     * Tells about chunked encoding for this entity.
     * The primary purpose of this method is to indicate whether
     * chunked encoding should be used when the entity is sent.
     * For entities that are received, it can also indicate whether
     * the entity was received with chunked encoding.
     * <p>
     * The behavior of wrapping entities is implementation dependent,
     * but should respect the primary purpose.
     * </p>
     *
     * @return  {@code true} if chunked encoding is preferred for this
     *          entity, or {@code false} if it is not
     */
    boolean isChunked();

    /**
     * Tells the length of the content, if known.
     *
     * @return  the number of bytes of the content, or
     *          a negative number if unknown. If the content length is known
     *          but exceeds {@link java.lang.Long#MAX_VALUE Long.MAX_VALUE},
     *          a negative number is returned.
     */
    long getContentLength();

    /**
     * Obtains the Content-Type header, if known.
     * This is the header that should be used when sending the entity,
     * or the one that was received with the entity. It can include a
     * charset attribute.
     *
     * @return  the Content-Type header for this entity, or
     *          {@code null} if the content type is unknown
     */
    Header getContentType();

    /**
     * Obtains the Content-Encoding header, if known.
     * This is the header that should be used when sending the entity,
     * or the one that was received with the entity.
     * Wrapping entities that modify the content encoding should
     * adjust this header accordingly.
     *
     * @return  the Content-Encoding header for this entity, or
     *          {@code null} if the content encoding is unknown
     */
    Header getContentEncoding();

    /**
     * Returns a content stream of the entity.
     * {@link #isRepeatable Repeatable} entities are expected
     * to create a new instance of {@link InputStream} for each invocation
     * of this method and therefore can be consumed multiple times.
     * Entities that are not {@link #isRepeatable repeatable} are expected
     * to return the same {@link InputStream} instance and therefore
     * may not be consumed more than once.
     * <p>
     * IMPORTANT: Please note all entity implementations must ensure that
     * all allocated resources are properly deallocated after
     * the {@link InputStream#close()} method is invoked.
     *
     * @return content stream of the entity.
     *
     * @throws IOException if the stream could not be created
     * @throws UnsupportedOperationException
     *  if entity content cannot be represented as {@link java.io.InputStream}.
     *
     * @see #isRepeatable()
     */
    InputStream getContent() throws IOException, UnsupportedOperationException;

    /**
     * Writes the entity content out to the output stream.
     * <p>
     * IMPORTANT: Please note all entity implementations must ensure that
     * all allocated resources are properly deallocated when this method
     * returns.
     *
     * @param outstream the output stream to write entity content to
     *
     * @throws IOException if an I/O error occurs
     */
    void writeTo(OutputStream outstream) throws IOException;

    /**
     * Tells whether this entity depends on an underlying stream.
     * Streamed entities that read data directly from the socket should
     * return {@code true}. Self-contained entities should return
     * {@code false}. Wrapping entities should delegate this call
     * to the wrapped entity.
     *
     * @return  {@code true} if the entity content is streamed,
     *          {@code false} otherwise
     */
    boolean isStreaming(); // don't expect an exception here

    /**
     * This method is deprecated since version 4.1. Please use standard
     * java convention to ensure resource deallocation by calling
     * {@link InputStream#close()} on the input stream returned by
     * {@link #getContent()}
     * <p>
     * This method is called to indicate that the content of this entity
     * is no longer required. All entity implementations are expected to
     * release all allocated resources as a result of this method
     * invocation. Content streaming entities are also expected to
     * dispose of the remaining content, if any. Wrapping entities should
     * delegate this call to the wrapped entity.
     * <p>
     * This method is of particular importance for entities being
     * received from a {@link HttpConnection connection}. The entity
     * needs to be consumed completely in order to re-use the connection
     * with keep-alive.
     *
     * @throws IOException if an I/O error occurs.
     *
     * @deprecated (4.1) Use {@link org.apache.http.util.EntityUtils#consume(HttpEntity)}
     *
     * @see #getContent() and #writeTo(OutputStream)
     */
    @Deprecated
    void consumeContent() throws IOException;

}
