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

import java.util.concurrent.FutureTask;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * FutureTask implementation that wraps a HttpAsyncClientCallable and exposes various task
 * specific metrics.
 *
 * @param <V>
 */
public class HttpRequestFutureTask<V> extends FutureTask<V> {

    private final HttpUriRequest request;
    private final HttpRequestTaskCallable<V> callable;

    public HttpRequestFutureTask(
            final HttpUriRequest request,
            final HttpRequestTaskCallable<V> httpCallable) {
        super(httpCallable);
        this.request = request;
        this.callable = httpCallable;
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.FutureTask#cancel(boolean)
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        callable.cancel();
        if (mayInterruptIfRunning) {
            request.abort();
        }
        return super.cancel(mayInterruptIfRunning);
    }

    /**
     * @return the time in millis the task was scheduled.
     */
    public long scheduledTime() {
        return callable.getScheduled();
    }

    /**
     * @return the time in millis the task was started.
     */
    public long startedTime() {
        return callable.getStarted();
    }

    /**
     * @return the time in millis the task was finished/cancelled.
     */
    public long endedTime() {
        if (isDone()) {
            return callable.getEnded();
        } else {
            throw new IllegalStateException("Task is not done yet");
        }
    }

    /**
     * @return the time in millis it took to make the request (excluding the time it was
     * scheduled to be executed).
     */
    public long requestDuration() {
        if (isDone()) {
            return endedTime() - startedTime();
        } else {
            throw new IllegalStateException("Task is not done yet");
        }
    }

    /**
     * @return the time in millis it took to execute the task from the moment it was scheduled.
     */
    public long taskDuration() {
        if (isDone()) {
            return endedTime() - scheduledTime();
        } else {
            throw new IllegalStateException("Task is not done yet");
        }
    }

    @Override
    public String toString() {
        return request.getRequestLine().getUri();
    }

}