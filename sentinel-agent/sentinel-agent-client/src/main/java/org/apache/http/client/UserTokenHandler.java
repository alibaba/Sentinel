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

package org.apache.http.client;

import org.apache.http.protocol.HttpContext;

/**
 * A handler for determining if the given execution context is user specific
 * or not. The token object returned by this handler is expected to uniquely
 * identify the current user if the context is user specific or to be
 * {@code null} if the context does not contain any resources or details
 * specific to the current user.
 * <p>
 * The user token will be used to ensure that user specific resources will not
 * be shared with or reused by other users.
 * </p>
 *
 * @since 4.0
 */
public interface UserTokenHandler {

    /**
     * The token object returned by this method is expected to uniquely
     * identify the current user if the context is user specific or to be
     * {@code null} if it is not.
     *
     * @param context the execution context
     *
     * @return user token that uniquely identifies the user or
     * {@code null} if the context is not user specific.
     */
    Object getUserToken(HttpContext context);

}
