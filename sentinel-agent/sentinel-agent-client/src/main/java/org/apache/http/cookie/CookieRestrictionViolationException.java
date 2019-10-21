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

/**
 * Signals that a cookie violates a restriction imposed by the cookie
 * specification.
 *
 * @since 4.1
 */
public class CookieRestrictionViolationException extends MalformedCookieException {

    private static final long serialVersionUID = 7371235577078589013L;

    /**
     * Creates a new CookeFormatViolationException with a {@code null} detail
     * message.
     */
    public CookieRestrictionViolationException() {
        super();
    }

    /**
     * Creates a new CookeRestrictionViolationException with a specified
     * message string.
     *
     * @param message The exception detail message
     */
    public CookieRestrictionViolationException(final String message) {
        super(message);
    }

}
