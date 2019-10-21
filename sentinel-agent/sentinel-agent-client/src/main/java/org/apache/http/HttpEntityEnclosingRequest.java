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
package org.apache.http;

/**
 * A request with an entity.
 *
 * @since 4.0
 */
public interface HttpEntityEnclosingRequest extends HttpRequest {

    /**
     * Tells if this request should use the expect-continue handshake.
     * The expect continue handshake gives the server a chance to decide
     * whether to accept the entity enclosing request before the possibly
     * lengthy entity is sent across the wire.
     * @return true if the expect continue handshake should be used, false if
     * not.
     */
    boolean expectContinue();

    /**
     * Associates the entity with this request.
     *
     * @param entity the entity to send.
     */
    void setEntity(HttpEntity entity);

    /**
     * Returns the entity associated with this request.
     *
     * @return entity
     */
    HttpEntity getEntity();

}
