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
package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;

/**
 * {@link org.apache.http.entity.HttpEntityWrapper} responsible for handling
 * deflate Content Coded responses. In RFC2616 terms, {@code deflate}
 * means a {@code zlib} stream as defined in RFC1950. Some server
 * implementations have misinterpreted RFC2616 to mean that a
 * {@code deflate} stream as defined in RFC1951 should be used
 * (or maybe they did that since that's how IE behaves?). It's confusing
 * that {@code deflate} in HTTP 1.1 means {@code zlib} streams
 * rather than {@code deflate} streams. We handle both types in here,
 * since that's what is seen on the internet. Moral - prefer
 * {@code gzip}!
 *
 * @see GzipDecompressingEntity
 *
 * @since 4.1
 */
public class DeflateDecompressingEntity extends DecompressingEntity {

    /**
     * Creates a new {@link DeflateDecompressingEntity} which will wrap the specified
     * {@link HttpEntity}.
     *
     * @param entity
     *            a non-null {@link HttpEntity} to be wrapped
     */
    public DeflateDecompressingEntity(final HttpEntity entity) {
        super(entity, new InputStreamFactory() {

            @Override
            public InputStream create(final InputStream instream) throws IOException {
                return new DeflateInputStream(instream);
            }

        });
    }

}
