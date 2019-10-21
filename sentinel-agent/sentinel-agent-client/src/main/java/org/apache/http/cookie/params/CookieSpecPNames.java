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

package org.apache.http.cookie.params;

/**
 * Parameter names for HTTP cookie management classes.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use constructor parameters of {@link
 *   org.apache.http.cookie.CookieSpecProvider}s.
 */
@Deprecated
public interface CookieSpecPNames {

    /**
     * Defines valid date patterns to be used for parsing non-standard
     * {@code expires} attribute. Only required for compatibility
     * with non-compliant servers that still use {@code expires}
     * defined in the Netscape draft instead of the standard
     * {@code max-age} attribute.
     * <p>
     * This parameter expects a value of type {@link java.util.Collection}.
     * The collection elements must be of type {@link String} compatible
     * with the syntax of {@link java.text.SimpleDateFormat}.
     * </p>
     */
    public static final String DATE_PATTERNS = "http.protocol.cookie-datepatterns";

    /**
     * Defines whether cookies should be forced into a single
     * {@code Cookie} request header. Otherwise, each cookie is formatted
     * as a separate {@code Cookie} header.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    public static final String SINGLE_COOKIE_HEADER = "http.protocol.single-cookie-header";

}
