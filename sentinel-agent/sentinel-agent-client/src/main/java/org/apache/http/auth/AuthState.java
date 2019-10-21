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

import java.util.Queue;

import org.apache.http.util.Args;

/**
 * This class provides detailed information about the state of the authentication process.
 *
 * @since 4.0
 */
public class AuthState {

    /** Actual state of authentication protocol */
    private AuthProtocolState state;

    /** Actual authentication scheme */
    private AuthScheme authScheme;

    /** Actual authentication scope */
    private AuthScope authScope;

    /** Credentials selected for authentication */
    private Credentials credentials;

    /** Available auth options */
    private Queue<AuthOption> authOptions;

    public AuthState() {
        super();
        this.state = AuthProtocolState.UNCHALLENGED;
    }

    /**
     * Resets the auth state.
     *
     * @since 4.2
     */
    public void reset() {
        this.state = AuthProtocolState.UNCHALLENGED;
        this.authOptions = null;
        this.authScheme = null;
        this.authScope = null;
        this.credentials = null;
    }

    /**
     * @since 4.2
     */
    public AuthProtocolState getState() {
        return this.state;
    }

    /**
     * @since 4.2
     */
    public void setState(final AuthProtocolState state) {
        this.state = state != null ? state : AuthProtocolState.UNCHALLENGED;
    }

    /**
     * Returns actual {@link AuthScheme}. May be null.
     */
    public AuthScheme getAuthScheme() {
        return this.authScheme;
    }

    /**
     * Returns actual {@link Credentials}. May be null.
     */
    public Credentials getCredentials() {
        return this.credentials;
    }

    /**
     * Updates the auth state with {@link AuthScheme} and {@link Credentials}.
     *
     * @param authScheme auth scheme. May not be null.
     * @param credentials user crednetials. May not be null.
     *
     * @since 4.2
     */
    public void update(final AuthScheme authScheme, final Credentials credentials) {
        Args.notNull(authScheme, "Auth scheme");
        Args.notNull(credentials, "Credentials");
        this.authScheme = authScheme;
        this.credentials = credentials;
        this.authOptions = null;
    }

    /**
     * Returns available {@link AuthOption}s. May be null.
     *
     * @since 4.2
     */
    public Queue<AuthOption> getAuthOptions() {
        return this.authOptions;
    }

    /**
     * Returns {@code true} if {@link AuthOption}s are available, {@code false}
     * otherwise.
     *
     * @since 4.2
     */
    public boolean hasAuthOptions() {
        return this.authOptions != null && !this.authOptions.isEmpty();
    }

    /**
     * Updates the auth state with a queue of {@link AuthOption}s.
     *
     * @param authOptions a queue of auth options. May not be null or empty.
     *
     * @since 4.2
     */
    public void update(final Queue<AuthOption> authOptions) {
        Args.notEmpty(authOptions, "Queue of auth options");
        this.authOptions = authOptions;
        this.authScheme = null;
        this.credentials = null;
    }

    /**
     * Invalidates the authentication state by resetting its parameters.
     *
     * @deprecated (4.2)  use {@link #reset()}
     */
    @Deprecated
    public void invalidate() {
        reset();
    }

    /**
     * @deprecated (4.2) do not use
     */
    @Deprecated
    public boolean isValid() {
        return this.authScheme != null;
    }

    /**
     * Assigns the given {@link AuthScheme authentication scheme}.
     *
     * @param authScheme the {@link AuthScheme authentication scheme}
     *
     * @deprecated (4.2)  use {@link #update(AuthScheme, Credentials)}
     */
    @Deprecated
    public void setAuthScheme(final AuthScheme authScheme) {
        if (authScheme == null) {
            reset();
            return;
        }
        this.authScheme = authScheme;
    }

    /**
     * Sets user {@link Credentials} to be used for authentication
     *
     * @param credentials User credentials
     *
     * @deprecated (4.2)  use {@link #update(AuthScheme, Credentials)}
     */
    @Deprecated
    public void setCredentials(final Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Returns actual {@link AuthScope} if available
     *
     * @return actual authentication scope if available, {@code null} otherwise
     *
     * @deprecated (4.2)  do not use.
     */
    @Deprecated
    public AuthScope getAuthScope() {
        return this.authScope;
    }

    /**
     * Sets actual {@link AuthScope}.
     *
     * @param authScope Authentication scope
     *
     * @deprecated (4.2)  do not use.
     */
    @Deprecated
    public void setAuthScope(final AuthScope authScope) {
        this.authScope = authScope;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("state:").append(this.state).append(";");
        if (this.authScheme != null) {
            buffer.append("auth scheme:").append(this.authScheme.getSchemeName()).append(";");
        }
        if (this.credentials != null) {
            buffer.append("credentials present");
        }
        return buffer.toString();
    }

}
