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
package org.apache.http.conn;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Users may implement this interface to override the normal DNS lookup offered
 * by the OS.
 *
 * @since 4.2
 */
public interface DnsResolver {

    /**
     * Returns the IP address for the specified host name, or null if the given
     * host is not recognized or the associated IP address cannot be used to
     * build an InetAddress instance.
     *
     * @see InetAddress
     *
     * @param host
     *            The host name to be resolved by this resolver.
     * @return The IP address associated to the given host name, or null if the
     *         host name is not known by the implementation class.
     */
    InetAddress[] resolve(String host) throws UnknownHostException;

}
