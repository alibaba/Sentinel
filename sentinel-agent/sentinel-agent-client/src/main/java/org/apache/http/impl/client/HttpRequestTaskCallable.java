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
package org.apache.http.impl.client;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.protocol.HttpContext;

class HttpRequestTaskCallable<V> implements Callable<V> {

    private final HttpUriRequest request;
    private final HttpClient httpclient;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private final long scheduled = System.currentTimeMillis();
    private long started = -1;
    private long ended = -1;

    private final HttpContext context;
    private final ResponseHandler<V> responseHandler;
    private final FutureCallback<V> callback;

    private final FutureRequestExecutionMetrics metrics;

    HttpRequestTaskCallable(
            final HttpClient httpClient,
            final HttpUriRequest request,
            final HttpContext context,
            final ResponseHandler<V> responseHandler,
            final FutureCallback<V> callback,
            final FutureRequestExecutionMetrics metrics) {
        this.httpclient = httpClient;
        this.responseHandler = responseHandler;
        this.request = request;
        this.context = context;
        this.callback = callback;
        this.metrics = metrics;
    }

    public long getScheduled() {
        return scheduled;
    }

    public long getStarted() {
        return started;
    }

    public long getEnded() {
        return ended;
    }

    @Override
    public V call() throws Exception {
        if (!cancelled.get()) {
            try {
                metrics.getActiveConnections().incrementAndGet();
                started = System.currentTimeMillis();
                try {
                    metrics.getScheduledConnections().decrementAndGet();
                    final V result = httpclient.execute(request, responseHandler, context);
                    ended = System.currentTimeMillis();
                    metrics.getSuccessfulConnections().increment(started);
                    if (callback != null) {
                        callback.completed(result);
                    }
                    return result;
                } catch (final Exception e) {
                    metrics.getFailedConnections().increment(started);
                    ended = System.currentTimeMillis();
                    if (callback != null) {
                        callback.failed(e);
                    }
                    throw e;
                }
            } finally {
                metrics.getRequests().increment(started);
                metrics.getTasks().increment(started);
                metrics.getActiveConnections().decrementAndGet();
            }
        } else {
            throw new IllegalStateException("call has been cancelled for request " + request.getURI());
        }
    }

    public void cancel() {
        cancelled.set(true);
        if (callback != null) {
            callback.cancelled();
        }
    }
}