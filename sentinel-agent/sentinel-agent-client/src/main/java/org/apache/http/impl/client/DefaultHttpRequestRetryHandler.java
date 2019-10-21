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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

/**
 * The default {@link HttpRequestRetryHandler} used by request executors.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class DefaultHttpRequestRetryHandler implements HttpRequestRetryHandler {

    public static final DefaultHttpRequestRetryHandler INSTANCE = new DefaultHttpRequestRetryHandler();

    /** the number of times a method will be retried */
    private final int retryCount;

    /** Whether or not methods that have successfully sent their request will be retried */
    private final boolean requestSentRetryEnabled;

    private final Set<Class<? extends IOException>> nonRetriableClasses;

    /**
     * Create the request retry handler using the specified IOException classes
     *
     * @param retryCount how many times to retry; 0 means no retries
     * @param requestSentRetryEnabled true if it's OK to retry requests that have been sent
     * @param clazzes the IOException types that should not be retried
     * @since 4.3
     */
    protected DefaultHttpRequestRetryHandler(
            final int retryCount,
            final boolean requestSentRetryEnabled,
            final Collection<Class<? extends IOException>> clazzes) {
        super();
        this.retryCount = retryCount;
        this.requestSentRetryEnabled = requestSentRetryEnabled;
        this.nonRetriableClasses = new HashSet<Class<? extends IOException>>();
        for (final Class<? extends IOException> clazz: clazzes) {
            this.nonRetriableClasses.add(clazz);
        }
    }

    /**
     * Create the request retry handler using the following list of
     * non-retriable IOException classes: <br>
     * <ul>
     * <li>InterruptedIOException</li>
     * <li>UnknownHostException</li>
     * <li>ConnectException</li>
     * <li>SSLException</li>
     * </ul>
     * @param retryCount how many times to retry; 0 means no retries
     * @param requestSentRetryEnabled true if it's OK to retry non-idempotent requests that have been sent
     */
    @SuppressWarnings("unchecked")
    public DefaultHttpRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled) {
        this(retryCount, requestSentRetryEnabled, Arrays.asList(
                InterruptedIOException.class,
                UnknownHostException.class,
                ConnectException.class,
                SSLException.class));
    }

    /**
     * Create the request retry handler with a retry count of 3, requestSentRetryEnabled false
     * and using the following list of non-retriable IOException classes: <br>
     * <ul>
     * <li>InterruptedIOException</li>
     * <li>UnknownHostException</li>
     * <li>ConnectException</li>
     * <li>SSLException</li>
     * </ul>
     */
    public DefaultHttpRequestRetryHandler() {
        this(3, false);
    }
    /**
     * Used {@code retryCount} and {@code requestSentRetryEnabled} to determine
     * if the given method should be retried.
     */
    @Override
    public boolean retryRequest(
            final IOException exception,
            final int executionCount,
            final HttpContext context) {
        Args.notNull(exception, "Exception parameter");
        Args.notNull(context, "HTTP context");
        if (executionCount > this.retryCount) {
            // Do not retry if over max retry count
            return false;
        }
        if (this.nonRetriableClasses.contains(exception.getClass())) {
            return false;
        } else {
            for (final Class<? extends IOException> rejectException : this.nonRetriableClasses) {
                if (rejectException.isInstance(exception)) {
                    return false;
                }
            }
        }
        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final HttpRequest request = clientContext.getRequest();

        if(requestIsAborted(request)){
            return false;
        }

        if (handleAsIdempotent(request)) {
            // Retry if the request is considered idempotent
            return true;
        }

        if (!clientContext.isRequestSent() || this.requestSentRetryEnabled) {
            // Retry if the request has not been sent fully or
            // if it's OK to retry methods that have been sent
            return true;
        }
        // otherwise do not retry
        return false;
    }

    /**
     * @return {@code true} if this handler will retry methods that have
     * successfully sent their request, {@code false} otherwise
     */
    public boolean isRequestSentRetryEnabled() {
        return requestSentRetryEnabled;
    }

    /**
     * @return the maximum number of times a method will be retried
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * @since 4.2
     */
    protected boolean handleAsIdempotent(final HttpRequest request) {
        return !(request instanceof HttpEntityEnclosingRequest);
    }

    /**
     * @since 4.2
     *
     * @deprecated (4.3)
     */
    @Deprecated
    protected boolean requestIsAborted(final HttpRequest request) {
        HttpRequest req = request;
        if (request instanceof RequestWrapper) { // does not forward request to original
            req = ((RequestWrapper) request).getOriginal();
        }
        return (req instanceof HttpUriRequest && ((HttpUriRequest)req).isAborted());
    }

}
