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

import org.apache.http.util.CharArrayBuffer;

/**
 * An HTTP header which is already formatted.
 * For example when headers are received, the original formatting
 * can be preserved. This allows for the header to be sent without
 * another formatting step.
 *
 * @since 4.0
 */
public interface FormattedHeader extends Header {

    /**
     * Obtains the buffer with the formatted header.
     * The returned buffer MUST NOT be modified.
     *
     * @return  the formatted header, in a buffer that must not be modified
     */
    CharArrayBuffer getBuffer();

    /**
     * Obtains the start of the header value in the {@link #getBuffer buffer}.
     * By accessing the value in the buffer, creation of a temporary string
     * can be avoided.
     *
     * @return  index of the first character of the header value
     *          in the buffer returned by {@link #getBuffer getBuffer}.
     */
    int getValuePos();

}
