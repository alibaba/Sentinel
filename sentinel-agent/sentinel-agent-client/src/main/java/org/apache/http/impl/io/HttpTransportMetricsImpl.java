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

package org.apache.http.impl.io;

import org.apache.http.io.HttpTransportMetrics;

/**
 * Default implementation of {@link HttpTransportMetrics}.
 *
 * @since 4.0
 */
public class HttpTransportMetricsImpl implements HttpTransportMetrics {

    private long bytesTransferred = 0;

    public HttpTransportMetricsImpl() {
        super();
    }

    @Override
    public long getBytesTransferred() {
        return this.bytesTransferred;
    }

    public void setBytesTransferred(final long count) {
        this.bytesTransferred = count;
    }

    public void incrementBytesTransferred(final long count) {
        this.bytesTransferred += count;
    }

    @Override
    public void reset() {
        this.bytesTransferred = 0;
    }

}
