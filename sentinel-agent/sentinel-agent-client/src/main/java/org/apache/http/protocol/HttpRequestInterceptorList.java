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

import java.util.List;

import org.apache.http.HttpRequestInterceptor;

/**
 * Provides access to an ordered list of request interceptors.
 * Lists are expected to be built upfront and used read-only afterwards
 * for {@link HttpProcessor processing}.
 *
 * @since 4.0
 *
 * @deprecated (4.3)
 */
@Deprecated
public interface HttpRequestInterceptorList {

    /**
     * Appends a request interceptor to this list.
     *
     * @param interceptor the request interceptor to add
     */
    void addRequestInterceptor(HttpRequestInterceptor interceptor);

    /**
     * Inserts a request interceptor at the specified index.
     *
     * @param interceptor the request interceptor to add
     * @param index     the index to insert the interceptor at
     */
    void addRequestInterceptor(HttpRequestInterceptor interceptor, int index);

    /**
     * Obtains the current size of this list.
     *
     * @return  the number of request interceptors in this list
     */
    int getRequestInterceptorCount();

    /**
     * Obtains a request interceptor from this list.
     *
     * @param index     the index of the interceptor to obtain,
     *                  0 for first
     *
     * @return  the interceptor at the given index, or
     *          {@code null} if the index is out of range
     */
    HttpRequestInterceptor getRequestInterceptor(int index);

    /**
     * Removes all request interceptors from this list.
     */
    void clearRequestInterceptors();

    /**
     * Removes all request interceptor of the specified class
     *
     * @param clazz  the class of the instances to be removed.
     */
    void removeRequestInterceptorByClass(Class<? extends HttpRequestInterceptor> clazz);

    /**
     * Sets the request interceptors in this list.
     * This list will be cleared and re-initialized to contain
     * all request interceptors from the argument list.
     * If the argument list includes elements that are not request
     * interceptors, the behavior is implementation dependent.
     *
     * @param list the list of request interceptors
     */
    void setInterceptors(List<?> list);

}

