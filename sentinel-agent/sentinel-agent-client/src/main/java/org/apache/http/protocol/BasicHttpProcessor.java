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

package org.apache.http.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.util.Args;

/**
 * Default implementation of {@link HttpProcessor}.
 * <p>
 * Please note access to the internal structures of this class is not
 * synchronized and therefore this class may be thread-unsafe.
 *
 * @since 4.0
 *
 * @deprecated (4.3)
 */
@Deprecated
public final class BasicHttpProcessor implements
    HttpProcessor, HttpRequestInterceptorList, HttpResponseInterceptorList, Cloneable {

    // Don't allow direct access, as nulls are not allowed
    protected final List<HttpRequestInterceptor> requestInterceptors = new ArrayList<HttpRequestInterceptor>();
    protected final List<HttpResponseInterceptor> responseInterceptors = new ArrayList<HttpResponseInterceptor>();

    public void addRequestInterceptor(final HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return;
        }
        this.requestInterceptors.add(itcp);
    }

    public void addRequestInterceptor(
            final HttpRequestInterceptor itcp, final int index) {
        if (itcp == null) {
            return;
        }
        this.requestInterceptors.add(index, itcp);
    }

    public void addResponseInterceptor(
            final HttpResponseInterceptor itcp, final int index) {
        if (itcp == null) {
            return;
        }
        this.responseInterceptors.add(index, itcp);
    }

    public void removeRequestInterceptorByClass(final Class<? extends HttpRequestInterceptor> clazz) {
        for (final Iterator<HttpRequestInterceptor> it = this.requestInterceptors.iterator();
             it.hasNext(); ) {
            final Object request = it.next();
            if (request.getClass().equals(clazz)) {
                it.remove();
            }
        }
    }

    public void removeResponseInterceptorByClass(final Class<? extends HttpResponseInterceptor> clazz) {
        for (final Iterator<HttpResponseInterceptor> it = this.responseInterceptors.iterator();
             it.hasNext(); ) {
            final Object request = it.next();
            if (request.getClass().equals(clazz)) {
                it.remove();
            }
        }
    }

    public final void addInterceptor(final HttpRequestInterceptor interceptor) {
        addRequestInterceptor(interceptor);
    }

     public final void addInterceptor(final HttpRequestInterceptor interceptor, final int index) {
        addRequestInterceptor(interceptor, index);
    }

    public int getRequestInterceptorCount() {
        return this.requestInterceptors.size();
    }

    public HttpRequestInterceptor getRequestInterceptor(final int index) {
        if ((index < 0) || (index >= this.requestInterceptors.size())) {
            return null;
        }
        return this.requestInterceptors.get(index);
    }

    public void clearRequestInterceptors() {
        this.requestInterceptors.clear();
    }

    public void addResponseInterceptor(final HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return;
        }
        this.responseInterceptors.add(itcp);
    }

    public final void addInterceptor(final HttpResponseInterceptor interceptor) {
        addResponseInterceptor(interceptor);
    }

    public final void addInterceptor(final HttpResponseInterceptor interceptor, final int index) {
        addResponseInterceptor(interceptor, index);
    }

    public int getResponseInterceptorCount() {
        return this.responseInterceptors.size();
    }

    public HttpResponseInterceptor getResponseInterceptor(final int index) {
        if ((index < 0) || (index >= this.responseInterceptors.size())) {
            return null;
        }
        return this.responseInterceptors.get(index);
    }

    public void clearResponseInterceptors() {
        this.responseInterceptors.clear();
    }

    /**
     * Sets the interceptor lists.
     * First, both interceptor lists maintained by this processor
     * will be cleared.
     * Subsequently,
     * elements of the argument list that are request interceptors will be
     * added to the request interceptor list.
     * Elements that are response interceptors will be
     * added to the response interceptor list.
     * Elements that are both request and response interceptor will be
     * added to both lists.
     * Elements that are neither request nor response interceptor
     * will be ignored.
     *
     * @param list      the list of request and response interceptors
     *                  from which to initialize
     */
    public void setInterceptors(final List<?> list) {
        Args.notNull(list, "Inteceptor list");
        this.requestInterceptors.clear();
        this.responseInterceptors.clear();
        for (final Object obj : list) {
            if (obj instanceof HttpRequestInterceptor) {
                addInterceptor((HttpRequestInterceptor) obj);
            }
            if (obj instanceof HttpResponseInterceptor) {
                addInterceptor((HttpResponseInterceptor) obj);
            }
        }
    }

    /**
     * Clears both interceptor lists maintained by this processor.
     */
    public void clearInterceptors() {
        clearRequestInterceptors();
        clearResponseInterceptors();
    }

    public void process(
            final HttpRequest request,
            final HttpContext context)
            throws IOException, HttpException {
        for (final HttpRequestInterceptor interceptor : this.requestInterceptors) {
            interceptor.process(request, context);
        }
    }

    public void process(
            final HttpResponse response,
            final HttpContext context)
            throws IOException, HttpException {
        for (final HttpResponseInterceptor interceptor : this.responseInterceptors) {
            interceptor.process(response, context);
        }
    }

    /**
     * Sets up the target to have the same list of interceptors
     * as the current instance.
     *
     * @param target object to be initialised
     */
    protected void copyInterceptors(final BasicHttpProcessor target) {
        target.requestInterceptors.clear();
        target.requestInterceptors.addAll(this.requestInterceptors);
        target.responseInterceptors.clear();
        target.responseInterceptors.addAll(this.responseInterceptors);
    }

    /**
     * Creates a copy of this instance
     *
     * @return new instance of the BasicHttpProcessor
     */
    public BasicHttpProcessor copy() {
        final BasicHttpProcessor clone = new BasicHttpProcessor();
        copyInterceptors(clone);
        return clone;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final BasicHttpProcessor clone = (BasicHttpProcessor) super.clone();
        copyInterceptors(clone);
        return clone;
    }

}
