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

import org.apache.http.annotation.Obsolete;

/**
 * This interface represents a {@code Set-Cookie2} response header sent by the
 * origin server to the HTTP agent in order to maintain a conversational state.
 * <p>
 * Please do not use methods marked as @Obsolete. They have been rendered
 * obsolete by RFC 6265
 *
 * @since 4.0
 */
public interface SetCookie2 extends SetCookie {

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described by the information at this URL.
     */
    @Obsolete
    void setCommentURL(String commentURL);

    /**
     * Sets the Port attribute. It restricts the ports to which a cookie
     * may be returned in a Cookie request header.
     */
    @Obsolete
    void setPorts(int[] ports);

    /**
     * Set the Discard attribute.
     *
     * Note: {@code Discard} attribute overrides {@code Max-age}.
     *
     * @see #isPersistent()
     */
    @Obsolete
    void setDiscard(boolean discard);

}

