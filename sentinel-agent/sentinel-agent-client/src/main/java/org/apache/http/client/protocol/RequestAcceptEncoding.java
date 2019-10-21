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
package org.apache.http.client.protocol;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.protocol.HttpContext;

/**
 * Class responsible for handling Content Encoding requests in HTTP.
 * <p>
 * Instances of this class are stateless, therefore they're thread-safe and immutable.
 *
 * @see "http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.5"
 *
 * @since 4.1
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestAcceptEncoding implements HttpRequestInterceptor {

    private final String acceptEncoding;

    /**
     * @since 4.4
     */
    public RequestAcceptEncoding(final List<String> encodings) {
        if (encodings != null && !encodings.isEmpty()) {
            final StringBuilder buf = new StringBuilder();
            for (int i = 0; i < encodings.size(); i++) {
                if (i > 0) {
                    buf.append(",");
                }
                buf.append(encodings.get(i));
            }
            this.acceptEncoding = buf.toString();
        } else {
            this.acceptEncoding = "gzip,deflate";
        }
    }

    public RequestAcceptEncoding() {
        this(null);
    }

    @Override
    public void process(
            final HttpRequest request,
            final HttpContext context) throws HttpException, IOException {

        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final RequestConfig requestConfig = clientContext.getRequestConfig();

        /* Signal support for Accept-Encoding transfer encodings. */
        if (!request.containsHeader("Accept-Encoding") && requestConfig.isContentCompressionEnabled()) {
            request.addHeader("Accept-Encoding", acceptEncoding);
        }
    }

}
