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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.auth.AuthScheme;
import org.apache.http.client.AuthCache;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.util.Args;

/**
 * Default implementation of {@link org.apache.http.client.AuthCache}. This implements
 * expects {@link org.apache.http.auth.AuthScheme} to be {@link java.io.Serializable}
 * in order to be cacheable.
 * <p>
 * Instances of this class are thread safe as of version 4.4.
 * </p>
 *
 * @since 4.1
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class BasicAuthCache implements AuthCache {

    private final Log log = LogFactory.getLog(getClass());

    private final Map<HttpHost, byte[]> map;
    private final SchemePortResolver schemePortResolver;

    /**
     * Default constructor.
     *
     * @since 4.3
     */
    public BasicAuthCache(final SchemePortResolver schemePortResolver) {
        super();
        this.map = new ConcurrentHashMap<HttpHost, byte[]>();
        this.schemePortResolver = schemePortResolver != null ? schemePortResolver :
            DefaultSchemePortResolver.INSTANCE;
    }

    public BasicAuthCache() {
        this(null);
    }

    protected HttpHost getKey(final HttpHost host) {
        if (host.getPort() <= 0) {
            final int port;
            try {
                port = schemePortResolver.resolve(host);
            } catch (final UnsupportedSchemeException ignore) {
                return host;
            }
            return new HttpHost(host.getHostName(), port, host.getSchemeName());
        } else {
            return host;
        }
    }

    @Override
    public void put(final HttpHost host, final AuthScheme authScheme) {
        Args.notNull(host, "HTTP host");
        if (authScheme == null) {
            return;
        }
        if (authScheme instanceof Serializable) {
            try {
                final ByteArrayOutputStream buf = new ByteArrayOutputStream();
                final ObjectOutputStream out = new ObjectOutputStream(buf);
                out.writeObject(authScheme);
                out.close();
                this.map.put(getKey(host), buf.toByteArray());
            } catch (final IOException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Unexpected I/O error while serializing auth scheme", ex);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Auth scheme " + authScheme.getClass() + " is not serializable");
            }
        }
    }

    @Override
    public AuthScheme get(final HttpHost host) {
        Args.notNull(host, "HTTP host");
        final byte[] bytes = this.map.get(getKey(host));
        if (bytes != null) {
            try {
                final ByteArrayInputStream buf = new ByteArrayInputStream(bytes);
                final ObjectInputStream in = new ObjectInputStream(buf);
                final AuthScheme authScheme = (AuthScheme) in.readObject();
                in.close();
                return authScheme;
            } catch (final IOException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Unexpected I/O error while de-serializing auth scheme", ex);
                }
                return null;
            } catch (final ClassNotFoundException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Unexpected error while de-serializing auth scheme", ex);
                }
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void remove(final HttpHost host) {
        Args.notNull(host, "HTTP host");
        this.map.remove(getKey(host));
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

}
