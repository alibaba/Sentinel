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
 * HTTP POST method.
 * <p>
 * The HTTP POST method is defined in section 9.5 of
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
 * </p>
 * <blockquote>
 * The POST method is used to request that the origin server accept the entity
 * enclosed in the request as a new subordinate of the resource identified by
 * the Request-URI in the Request-Line. POST is designed to allow a uniform
 * method to cover the following functions:
 * <ul>
 *   <li>Annotation of existing resources</li>
 *   <li>Posting a message to a bulletin board, newsgroup, mailing list, or
 *     similar group of articles</li>
 *   <li>Providing a block of data, such as the result of submitting a form,
 *     to a data-handling process</li>
 *   <li>Extending a database through an append operation</li>
 * </ul>
 * </blockquote>
 *
 * @since 4.0
 */
public class HttpPost extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "POST";

    public HttpPost() {
        super();
    }

    public HttpPost(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpPost(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}
