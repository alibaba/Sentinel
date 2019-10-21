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

package org.apache.http.message;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.util.CharArrayBuffer;

/**
 * Interface for formatting elements of a header value.
 * This is the complement to {@link HeaderValueParser}.
 * Instances of this interface are expected to be stateless and thread-safe.
 *
 * <p>
 * All formatting methods accept an optional buffer argument.
 * If a buffer is passed in, the formatted element will be appended
 * and the modified buffer is returned. If no buffer is passed in,
 * a new buffer will be created and filled with the formatted element.
 * In both cases, the caller is allowed to modify the returned buffer.
 * </p>
 *
 * @since 4.0
 */
public interface HeaderValueFormatter {

    /**
     * Formats an array of header elements.
     *
     * @param buffer    the buffer to append to, or
     *                  {@code null} to create a new buffer
     * @param elems     the header elements to format
     * @param quote     {@code true} to always format with quoted values,
     *                  {@code false} to use quotes only when necessary
     *
     * @return  a buffer with the formatted header elements.
     *          If the {@code buffer} argument was not {@code null},
     *          that buffer will be used and returned.
     */
    CharArrayBuffer formatElements(CharArrayBuffer buffer,
                                   HeaderElement[] elems,
                                   boolean quote);

    /**
     * Formats one header element.
     *
     * @param buffer    the buffer to append to, or
     *                  {@code null} to create a new buffer
     * @param elem      the header element to format
     * @param quote     {@code true} to always format with quoted values,
     *                  {@code false} to use quotes only when necessary
     *
     * @return  a buffer with the formatted header element.
     *          If the {@code buffer} argument was not {@code null},
     *          that buffer will be used and returned.
     */
    CharArrayBuffer formatHeaderElement(CharArrayBuffer buffer,
                                        HeaderElement elem,
                                        boolean quote);

    /**
     * Formats the parameters of a header element.
     * That's a list of name-value pairs, to be separated by semicolons.
     * This method will <i>not</i> generate a leading semicolon.
     *
     * @param buffer    the buffer to append to, or
     *                  {@code null} to create a new buffer
     * @param nvps      the parameters (name-value pairs) to format
     * @param quote     {@code true} to always format with quoted values,
     *                  {@code false} to use quotes only when necessary
     *
     * @return  a buffer with the formatted parameters.
     *          If the {@code buffer} argument was not {@code null},
     *          that buffer will be used and returned.
     */
    CharArrayBuffer formatParameters(CharArrayBuffer buffer,
                                     NameValuePair[] nvps,
                                     boolean quote);

    /**
     * Formats one name-value pair, where the value is optional.
     *
     * @param buffer    the buffer to append to, or
     *                  {@code null} to create a new buffer
     * @param nvp       the name-value pair to format
     * @param quote     {@code true} to always format with a quoted value,
     *                  {@code false} to use quotes only when necessary
     *
     * @return  a buffer with the formatted name-value pair.
     *          If the {@code buffer} argument was not {@code null},
     *          that buffer will be used and returned.
     */
    CharArrayBuffer formatNameValuePair(CharArrayBuffer buffer,
                                        NameValuePair nvp,
                                        boolean quote);

}

