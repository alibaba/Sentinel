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

package org.apache.http.impl.entity;

import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;
import org.apache.http.entity.ContentLengthStrategy;

/**
 * Decorator for  {@link ContentLengthStrategy} implementations that disallows the use of
 * identity transfer encoding.
 *
 * @since 4.2
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DisallowIdentityContentLengthStrategy implements ContentLengthStrategy {

    public static final DisallowIdentityContentLengthStrategy INSTANCE =
        new DisallowIdentityContentLengthStrategy(new LaxContentLengthStrategy(0));

    private final ContentLengthStrategy contentLengthStrategy;

    public DisallowIdentityContentLengthStrategy(final ContentLengthStrategy contentLengthStrategy) {
        super();
        this.contentLengthStrategy = contentLengthStrategy;
    }

    @Override
    public long determineLength(final HttpMessage message) throws HttpException {
        final long result = this.contentLengthStrategy.determineLength(message);
        if (result == ContentLengthStrategy.IDENTITY) {
            throw new ProtocolException("Identity transfer encoding cannot be used");
        }
        return result;
    }

}
