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

package org.apache.http.cookie;

import java.util.Date;

import org.apache.http.annotation.Obsolete;

/**
 * This interface represents a {@code Set-Cookie} response header sent by the
 * origin server to the HTTP agent in order to maintain a conversational state.
 * <p>
 * Please do not use methods marked as @Obsolete. They have been rendered
 * obsolete by RFC 6265
 *
 * @since 4.0
 */
public interface SetCookie extends Cookie {

    void setValue(String value);

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described using this comment.
     *
     * @param comment
     *
     * @see #getComment()
     */
    @Obsolete
    void setComment(String comment);

    /**
     * Sets expiration date.
     * <p><strong>Note:</strong> the object returned by this method is considered
     * immutable. Changing it (e.g. using setTime()) could result in undefined
     * behaviour. Do so at your peril.</p>
     *
     * @param expiryDate the {@link Date} after which this cookie is no longer valid.
     *
     * @see Cookie#getExpiryDate
     *
     */
    void setExpiryDate (Date expiryDate);

    /**
     * Sets the domain attribute.
     *
     * @param domain The value of the domain attribute
     *
     * @see Cookie#getDomain
     */
    void setDomain(String domain);

    /**
     * Sets the path attribute.
     *
     * @param path The value of the path attribute
     *
     * @see Cookie#getPath
     *
     */
    void setPath(String path);

    /**
     * Sets the secure attribute of the cookie.
     * <p>
     * When {@code true} the cookie should only be sent
     * using a secure protocol (https).  This should only be set when
     * the cookie's originating server used a secure protocol to set the
     * cookie's value.
     *
     * @param secure The value of the secure attribute
     *
     * @see #isSecure()
     */
    void setSecure (boolean secure);

    /**
     * Sets the version of the cookie specification to which this
     * cookie conforms.
     *
     * @param version the version of the cookie.
     *
     * @see Cookie#getVersion
     */
    @Obsolete
    void setVersion(int version);

}

