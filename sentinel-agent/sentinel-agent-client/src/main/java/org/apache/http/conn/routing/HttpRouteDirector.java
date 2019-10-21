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

package org.apache.http.conn.routing;

/**
 * Provides directions on establishing a route.
 * Implementations of this interface compare a planned route with
 * a tracked route and indicate the next step required.
 *
 * @since 4.0
 */
public interface HttpRouteDirector {

    /** Indicates that the route can not be established at all. */
    public final static int UNREACHABLE = -1;

    /** Indicates that the route is complete. */
    public final static int COMPLETE = 0;

    /** Step: open connection to target. */
    public final static int CONNECT_TARGET = 1;

    /** Step: open connection to proxy. */
    public final static int CONNECT_PROXY = 2;

    /** Step: tunnel through proxy to target. */
    public final static int TUNNEL_TARGET = 3;

    /** Step: tunnel through proxy to other proxy. */
    public final static int TUNNEL_PROXY = 4;

    /** Step: layer protocol (over tunnel). */
    public final static int LAYER_PROTOCOL = 5;


    /**
     * Provides the next step.
     *
     * @param plan      the planned route
     * @param fact      the currently established route, or
     *                  {@code null} if nothing is established
     *
     * @return  one of the constants defined in this interface, indicating
     *          either the next step to perform, or success, or failure.
     *          0 is for success, a negative value for failure.
     */
    public int nextStep(RouteInfo plan, RouteInfo fact);

}
