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

import org.apache.http.HttpResponseInterceptor;

/**
 * Provides access to an ordered list of response interceptors.
 * Lists are expected to be built upfront and used read-only afterwards
 * for {@link HttpProcessor processing}.
 *
 * @since 4.0
 *
 * @deprecated (4.3)
 */
@Deprecated
public interface HttpResponseInterceptorList {

    /**
     * Appends a response interceptor to this list.
     *
     * @param interceptor the response interceptor to add
     */
    void addResponseInterceptor(HttpResponseInterceptor interceptor);

    /**
     * Inserts a response interceptor at the specified index.
     *
     * @param interceptor the response interceptor to add
     * @param index     the index to insert the interceptor at
     */
    void addResponseInterceptor(HttpResponseInterceptor interceptor, int index);

    /**
     * Obtains the current size of this list.
     *
     * @return  the number of response interceptors in this list
     */
    int getResponseInterceptorCount();

    /**
     * Obtains a response interceptor from this list.
     *
     * @param index     the index of the interceptor to obtain,
     *                  0 for first
     *
     * @return  the interceptor at the given index, or
     *          {@code null} if the index is out of range
     */
    HttpResponseInterceptor getResponseInterceptor(int index);

    /**
     * Removes all response interceptors from this list.
     */
    void clearResponseInterceptors();

    /**
     * Removes all response interceptor of the specified class
     *
     * @param clazz  the class of the instances to be removed.
     */
    void removeResponseInterceptorByClass(Class<? extends HttpResponseInterceptor> clazz);

    /**
     * Sets the response interceptors in this list.
     * This list will be cleared and re-initialized to contain
     * all response interceptors from the argument list.
     * If the argument list includes elements that are not response
     * interceptors, the behavior is implementation dependent.
     *
     * @param list the list of response interceptors
     */
    void setInterceptors(List<?> list);

}

