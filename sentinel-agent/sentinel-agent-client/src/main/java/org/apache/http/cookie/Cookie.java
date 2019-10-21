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
 * Cookie interface represents a token or short packet of state information
 * (also referred to as "magic-cookie") that the HTTP agent and the target
 * server can exchange to maintain a session. In its simples form an HTTP
 * cookie is merely a name / value pair.
 * <p>
 * Please do not use attributes marked as @Obsolete. They have been rendered
 * obsolete by RFC 6265.
 *
 * @since 4.0
 */
public interface Cookie {

    /**
     * Returns the name.
     *
     * @return String name The name
     */
    String getName();

    /**
     * Returns the value.
     *
     * @return String value The current value.
     */
    String getValue();

    /**
     * Returns the comment describing the purpose of this cookie, or
     * {@code null} if no such comment has been defined.
     *
     * @return comment
     */
    @Obsolete
    String getComment();

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described by the information at this URL.
     */
    @Obsolete
    String getCommentURL();

    /**
     * Returns the expiration {@link Date} of the cookie, or {@code null}
     * if none exists.
     * <p><strong>Note:</strong> the object returned by this method is
     * considered immutable. Changing it (e.g. using setTime()) could result
     * in undefined behaviour. Do so at your peril. </p>
     * @return Expiration {@link Date}, or {@code null}.
     */
    Date getExpiryDate();

    /**
     * Returns {@code false} if the cookie should be discarded at the end
     * of the "session"; {@code true} otherwise.
     *
     * @return {@code false} if the cookie should be discarded at the end
     *         of the "session"; {@code true} otherwise
     */
    boolean isPersistent();

    /**
     * Returns domain attribute of the cookie. The value of the Domain
     * attribute specifies the domain for which the cookie is valid.
     *
     * @return the value of the domain attribute.
     */
    String getDomain();

    /**
     * Returns the path attribute of the cookie. The value of the Path
     * attribute specifies the subset of URLs on the origin server to which
     * this cookie applies.
     *
     * @return The value of the path attribute.
     */
    String getPath();

    /**
     * Get the Port attribute. It restricts the ports to which a cookie
     * may be returned in a Cookie request header.
     */
    @Obsolete
    int[] getPorts();

    /**
     * Indicates whether this cookie requires a secure connection.
     *
     * @return  {@code true} if this cookie should only be sent
     *          over secure connections, {@code false} otherwise.
     */
    boolean isSecure();

    /**
     * Returns the version of the cookie specification to which this
     * cookie conforms.
     *
     * @return the version of the cookie.
     */
    @Obsolete
    int getVersion();

    /**
     * Returns true if this cookie has expired.
     * @param date Current time
     *
     * @return {@code true} if the cookie has expired.
     */
    boolean isExpired(final Date date);

    //TODO: RFC 6265 requires cookies to track their creation time; add #getCreationDate()

}

