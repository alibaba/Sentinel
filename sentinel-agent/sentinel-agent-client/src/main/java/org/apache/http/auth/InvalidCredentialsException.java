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
package org.apache.http.auth;

/**
 * Authentication credentials required to respond to a authentication
 * challenge are invalid
 *
 *
 * @since 4.0
 */
public class InvalidCredentialsException extends AuthenticationException {

    private static final long serialVersionUID = -4834003835215460648L;

    /**
     * Creates a new InvalidCredentialsException with a {@code null} detail message.
     */
    public InvalidCredentialsException() {
        super();
    }

    /**
     * Creates a new InvalidCredentialsException with the specified message.
     *
     * @param message the exception detail message
     */
    public InvalidCredentialsException(final String message) {
        super(message);
    }

    /**
     * Creates a new InvalidCredentialsException with the specified detail message and cause.
     *
     * @param message the exception detail message
     * @param cause the {@code Throwable} that caused this exception, or {@code null}
     * if the cause is unavailable, unknown, or not a {@code Throwable}
     */
    public InvalidCredentialsException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
