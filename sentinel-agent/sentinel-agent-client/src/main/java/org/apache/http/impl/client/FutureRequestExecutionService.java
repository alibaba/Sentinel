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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.protocol.HttpContext;

/**
 * HttpAsyncClientWithFuture wraps calls to execute with a {@link HttpRequestFutureTask}
 * and schedules them using the provided executor service. Scheduled calls may be cancelled.
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class FutureRequestExecutionService implements Closeable {

    private final HttpClient httpclient;
    private final ExecutorService executorService;
    private final FutureRequestExecutionMetrics metrics = new FutureRequestExecutionMetrics();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Create a new FutureRequestExecutionService.
     *
     * @param httpclient
     *            you should tune your httpclient instance to match your needs. You should
     *            align the max number of connections in the pool and the number of threads
     *            in the executor; it doesn't make sense to have more threads than connections
     *            and if you have less connections than threads, the threads will just end up
     *            blocking on getting a connection from the pool.
     * @param executorService
     *            any executorService will do here. E.g.
     *            {@link java.util.concurrent.Executors#newFixedThreadPool(int)}
     */
    public FutureRequestExecutionService(
            final HttpClient httpclient,
            final ExecutorService executorService) {
        this.httpclient = httpclient;
        this.executorService = executorService;
    }

    /**
     * Schedule a request for execution.
     *
     * @param <T>
     *
     * @param request
     *            request to execute
     * @param responseHandler
     *            handler that will process the response.
     * @return HttpAsyncClientFutureTask for the scheduled request.
     */
    public <T> HttpRequestFutureTask<T> execute(
            final HttpUriRequest request,
            final HttpContext context,
            final ResponseHandler<T> responseHandler) {
        return execute(request, context, responseHandler, null);
    }

    /**
     * Schedule a request for execution.
     *
     * @param <T>
     *
     * @param request
     *            request to execute
     * @param context
     *            optional context; use null if not needed.
     * @param responseHandler
     *            handler that will process the response.
     * @param callback
     *            callback handler that will be called when the request is scheduled,
     *            started, completed, failed, or cancelled.
     * @return HttpAsyncClientFutureTask for the scheduled request.
     */
    public <T> HttpRequestFutureTask<T> execute(
            final HttpUriRequest request,
            final HttpContext context,
            final ResponseHandler<T> responseHandler,
            final FutureCallback<T> callback) {
        if(closed.get()) {
            throw new IllegalStateException("Close has been called on this httpclient instance.");
        }
        metrics.getScheduledConnections().incrementAndGet();
        final HttpRequestTaskCallable<T> callable = new HttpRequestTaskCallable<T>(
            httpclient, request, context, responseHandler, callback, metrics);
        final HttpRequestFutureTask<T> httpRequestFutureTask = new HttpRequestFutureTask<T>(
            request, callable);
        executorService.execute(httpRequestFutureTask);

        return httpRequestFutureTask;
    }

    /**
     * @return metrics gathered for this instance.
     * @see FutureRequestExecutionMetrics
     */
    public FutureRequestExecutionMetrics metrics() {
        return metrics;
    }

    @Override
    public void close() throws IOException {
        closed.set(true);
        executorService.shutdownNow();
        if (httpclient instanceof Closeable) {
            ((Closeable) httpclient).close();
        }
    }
}
