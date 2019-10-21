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
package org.apache.http.impl.conn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.DnsResolver;
import org.apache.http.util.Args;

/**
 * In-memory {@link DnsResolver} implementation.
 *
 * @since 4.2
 */
public class InMemoryDnsResolver implements DnsResolver {

    /** Logger associated to this class. */
    private final Log log = LogFactory.getLog(InMemoryDnsResolver.class);

    /**
     * In-memory collection that will hold the associations between a host name
     * and an array of InetAddress instances.
     */
    private final Map<String, InetAddress[]> dnsMap;

    /**
     * Builds a DNS resolver that will resolve the host names against a
     * collection held in-memory.
     */
    public InMemoryDnsResolver() {
        dnsMap = new ConcurrentHashMap<String, InetAddress[]>();
    }

    /**
     * Associates the given array of IP addresses to the given host in this DNS overrider.
     * The IP addresses are assumed to be already resolved.
     *
     * @param host
     *            The host name to be associated with the given IP.
     * @param ips
     *            array of IP addresses to be resolved by this DNS overrider to the given
     *            host name.
     */
    public void add(final String host, final InetAddress... ips) {
        Args.notNull(host, "Host name");
        Args.notNull(ips, "Array of IP addresses");
        dnsMap.put(host, ips);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress[] resolve(final String host) throws UnknownHostException {
        final InetAddress[] resolvedAddresses = dnsMap.get(host);
        if (log.isInfoEnabled()) {
            log.info("Resolving " + host + " to " + Arrays.deepToString(resolvedAddresses));
        }
        if(resolvedAddresses == null){
            throw new UnknownHostException(host + " cannot be resolved");
        }
        return resolvedAddresses;
    }

}
