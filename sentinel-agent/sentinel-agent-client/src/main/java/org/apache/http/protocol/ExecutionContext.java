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

package org.apache.http.protocol;

/**
 * {@link HttpContext} attribute names for protocol execution.
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link HttpCoreContext}.
 */
@Deprecated
public interface ExecutionContext {

    /**
     * Attribute name of a {@link org.apache.http.HttpConnection} object that
     * represents the actual HTTP connection.
     */
    public static final String HTTP_CONNECTION  = "http.connection";

    /**
     * Attribute name of a {@link org.apache.http.HttpRequest} object that
     * represents the actual HTTP request.
     */
    public static final String HTTP_REQUEST     = "http.request";

    /**
     * Attribute name of a {@link org.apache.http.HttpResponse} object that
     * represents the actual HTTP response.
     */
    public static final String HTTP_RESPONSE    = "http.response";

    /**
     * Attribute name of a {@link org.apache.http.HttpHost} object that
     * represents the connection target.
     */
    public static final String HTTP_TARGET_HOST = "http.target_host";

    /**
     * Attribute name of a {@link org.apache.http.HttpHost} object that
     * represents the connection proxy.
     *
     * @deprecated (4.3) do not use.
     */
    @Deprecated
    public static final String HTTP_PROXY_HOST  = "http.proxy_host";

    /**
     * Attribute name of a {@link Boolean} object that represents the
     * the flag indicating whether the actual request has been fully transmitted
     * to the target host.
     */
    public static final String HTTP_REQ_SENT    = "http.request_sent";

}
