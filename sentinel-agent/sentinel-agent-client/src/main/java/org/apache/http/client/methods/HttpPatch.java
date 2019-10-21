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

package org.apache.http.client.methods;

import java.net.URI;

/**
 * HTTP PATCH method.
 * <p>
 * The HTTP PATCH method is defined in <a
 * href="http://tools.ietf.org/html/rfc5789">RF5789</a>:
 * </p>
 * <blockquote> The PATCH
 * method requests that a set of changes described in the request entity be
 * applied to the resource identified by the Request- URI. Differs from the PUT
 * method in the way the server processes the enclosed entity to modify the
 * resource identified by the Request-URI. In a PUT request, the enclosed entity
 * origin server, and the client is requesting that the stored version be
 * replaced. With PATCH, however, the enclosed entity contains a set of
 * instructions describing how a resource currently residing on the origin
 * server should be modified to produce a new version.
 * </blockquote>
 *
 * @since 4.2
 */
public class HttpPatch extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "PATCH";

    public HttpPatch() {
        super();
    }

    public HttpPatch(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpPatch(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}
