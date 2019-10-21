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

package org.apache.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.Args;

/**
 * Base class for wrapping entities.
 * Keeps a {@link #wrappedEntity wrappedEntity} and delegates all
 * calls to it. Implementations of wrapping entities can derive
 * from this class and need to override only those methods that
 * should not be delegated to the wrapped entity.
 *
 * @since 4.0
 */
public class HttpEntityWrapper implements HttpEntity {

    /** The wrapped entity. */
    protected HttpEntity wrappedEntity;

    /**
     * Creates a new entity wrapper.
     */
    public HttpEntityWrapper(final HttpEntity wrappedEntity) {
        super();
        this.wrappedEntity = Args.notNull(wrappedEntity, "Wrapped entity");
    } // constructor

    @Override
    public boolean isRepeatable() {
        return wrappedEntity.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return wrappedEntity.isChunked();
    }

    @Override
    public long getContentLength() {
        return wrappedEntity.getContentLength();
    }

    @Override
    public Header getContentType() {
        return wrappedEntity.getContentType();
    }

    @Override
    public Header getContentEncoding() {
        return wrappedEntity.getContentEncoding();
    }

    @Override
    public InputStream getContent()
        throws IOException {
        return wrappedEntity.getContent();
    }

    @Override
    public void writeTo(final OutputStream outstream)
        throws IOException {
        wrappedEntity.writeTo(outstream);
    }

    @Override
    public boolean isStreaming() {
        return wrappedEntity.isStreaming();
    }

    /**
     * @deprecated (4.1) Either use {@link #getContent()} and call {@link java.io.InputStream#close()} on that;
     * otherwise call {@link #writeTo(OutputStream)} which is required to free the resources.
     */
    @Override
    @Deprecated
    public void consumeContent() throws IOException {
        wrappedEntity.consumeContent();
    }

}
