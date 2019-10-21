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
package org.apache.http.impl.execchain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

/**
 * A Proxy class for {@link org.apache.http.HttpEntity} enclosed in a request message.
 *
 * @since 4.3
 */
class RequestEntityProxy implements HttpEntity  {

    static void enhance(final HttpEntityEnclosingRequest request) {
        final HttpEntity entity = request.getEntity();
        if (entity != null && !entity.isRepeatable() && !isEnhanced(entity)) {
            request.setEntity(new RequestEntityProxy(entity));
        }
    }

    static boolean isEnhanced(final HttpEntity entity) {
        return entity instanceof RequestEntityProxy;
    }

    static boolean isRepeatable(final HttpRequest request) {
        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (entity != null) {
                if (isEnhanced(entity)) {
                    final RequestEntityProxy proxy = (RequestEntityProxy) entity;
                    if (!proxy.isConsumed()) {
                        return true;
                    }
                }
                return entity.isRepeatable();
            }
        }
        return true;
    }

    private final HttpEntity original;
    private boolean consumed = false;

    RequestEntityProxy(final HttpEntity original) {
        super();
        this.original = original;
    }

    public HttpEntity getOriginal() {
        return original;
    }

    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public boolean isRepeatable() {
        return original.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return original.isChunked();
    }

    @Override
    public long getContentLength() {
        return original.getContentLength();
    }

    @Override
    public Header getContentType() {
        return original.getContentType();
    }

    @Override
    public Header getContentEncoding() {
        return original.getContentEncoding();
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return original.getContent();
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        consumed = true;
        original.writeTo(outstream);
    }

    @Override
    public boolean isStreaming() {
        return original.isStreaming();
    }

    @Override
    @Deprecated
    public void consumeContent() throws IOException {
        consumed = true;
        original.consumeContent();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestEntityProxy{");
        sb.append(original);
        sb.append('}');
        return sb.toString();
    }

}
