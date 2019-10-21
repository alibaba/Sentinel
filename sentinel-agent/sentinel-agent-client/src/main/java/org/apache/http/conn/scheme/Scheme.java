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
package org.apache.http.conn.scheme;

import java.util.Locale;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.util.Args;
import org.apache.http.util.LangUtils;

/**
 * Encapsulates specifics of a protocol scheme such as "http" or "https". Schemes are identified
 * by lowercase names. Supported schemes are typically collected in a {@link SchemeRegistry
 * SchemeRegistry}.
 * <p>
 * For example, to configure support for "https://" URLs, you could write code like the following:
 * </p>
 * <pre>
 * Scheme https = new Scheme("https", 443, new MySecureSocketFactory());
 * SchemeRegistry registry = new SchemeRegistry();
 * registry.register(https);
 * </pre>
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link org.apache.http.conn.SchemePortResolver} for default port
 * resolution and {@link org.apache.http.config.Registry} for socket factory lookups.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
@Deprecated
public final class Scheme {

    /** The name of this scheme, in lowercase. (e.g. http, https) */
    private final String name;

    /** The socket factory for this scheme */
    private final SchemeSocketFactory socketFactory;

    /** The default port for this scheme */
    private final int defaultPort;

    /** Indicates whether this scheme allows for layered connections */
    private final boolean layered;

    /** A string representation, for {@link #toString toString}. */
    private String stringRep;
    /*
     *  This is used to cache the result of the toString() method
     *  Since the method always generates the same value, there's no
     *  need to synchronize, and it does not affect immutability.
    */

    /**
     * Creates a new scheme.
     * Whether the created scheme allows for layered connections
     * depends on the class of {@code factory}.
     *
     * @param name      the scheme name, for example "http".
     *                  The name will be converted to lowercase.
     * @param port      the default port for this scheme
     * @param factory   the factory for creating sockets for communication
     *                  with this scheme
     *
     * @since 4.1
     */
    public Scheme(final String name, final int port, final SchemeSocketFactory factory) {
        Args.notNull(name, "Scheme name");
        Args.check(port > 0 && port <= 0xffff, "Port is invalid");
        Args.notNull(factory, "Socket factory");
        this.name = name.toLowerCase(Locale.ENGLISH);
        this.defaultPort = port;
        if (factory instanceof SchemeLayeredSocketFactory) {
            this.layered = true;
            this.socketFactory = factory;
        } else if (factory instanceof LayeredSchemeSocketFactory) {
            this.layered = true;
            this.socketFactory = new SchemeLayeredSocketFactoryAdaptor2((LayeredSchemeSocketFactory) factory);
        } else {
            this.layered = false;
            this.socketFactory = factory;
        }
    }

    /**
     * Creates a new scheme.
     * Whether the created scheme allows for layered connections
     * depends on the class of {@code factory}.
     *
     * @param name      the scheme name, for example "http".
     *                  The name will be converted to lowercase.
     * @param factory   the factory for creating sockets for communication
     *                  with this scheme
     * @param port      the default port for this scheme
     *
     * @deprecated (4.1)  Use {@link #Scheme(String, int, SchemeSocketFactory)}
     */
    @Deprecated
    public Scheme(final String name,
                  final SocketFactory factory,
                  final int port) {

        Args.notNull(name, "Scheme name");
        Args.notNull(factory, "Socket factory");
        Args.check(port > 0 && port <= 0xffff, "Port is invalid");

        this.name = name.toLowerCase(Locale.ENGLISH);
        if (factory instanceof LayeredSocketFactory) {
            this.socketFactory = new SchemeLayeredSocketFactoryAdaptor(
                    (LayeredSocketFactory) factory);
            this.layered = true;
        } else {
            this.socketFactory = new SchemeSocketFactoryAdaptor(factory);
            this.layered = false;
        }
        this.defaultPort = port;
    }

    /**
     * Obtains the default port.
     *
     * @return  the default port for this scheme
     */
    public final int getDefaultPort() {
        return defaultPort;
    }


    /**
     * Obtains the socket factory.
     * If this scheme is {@link #isLayered layered}, the factory implements
     * {@link LayeredSocketFactory LayeredSocketFactory}.
     *
     * @return  the socket factory for this scheme
     *
     * @deprecated (4.1)  Use {@link #getSchemeSocketFactory()}
     */
    @Deprecated
    public final SocketFactory getSocketFactory() {
        if (this.socketFactory instanceof SchemeSocketFactoryAdaptor) {
            return ((SchemeSocketFactoryAdaptor) this.socketFactory).getFactory();
        } else {
            if (this.layered) {
                return new LayeredSocketFactoryAdaptor(
                        (LayeredSchemeSocketFactory) this.socketFactory);
            } else {
                return new SocketFactoryAdaptor(this.socketFactory);
            }
        }
    }

    /**
     * Obtains the socket factory.
     * If this scheme is {@link #isLayered layered}, the factory implements
     * {@link LayeredSocketFactory LayeredSchemeSocketFactory}.
     *
     * @return  the socket factory for this scheme
     *
     * @since 4.1
     */
    public final SchemeSocketFactory getSchemeSocketFactory() {
        return this.socketFactory;
    }

    /**
     * Obtains the scheme name.
     *
     * @return  the name of this scheme, in lowercase
     */
    public final String getName() {
        return name;
    }

    /**
     * Indicates whether this scheme allows for layered connections.
     *
     * @return {@code true} if layered connections are possible,
     *         {@code false} otherwise
     */
    public final boolean isLayered() {
        return layered;
    }

    /**
     * Resolves the correct port for this scheme.
     * Returns the given port if it is valid, the default port otherwise.
     *
     * @param port      the port to be resolved,
     *                  a negative number to obtain the default port
     *
     * @return the given port or the defaultPort
     */
    public final int resolvePort(final int port) {
        return port <= 0 ? defaultPort : port;
    }

    /**
     * Return a string representation of this object.
     *
     * @return  a human-readable string description of this scheme
     */
    @Override
    public final String toString() {
        if (stringRep == null) {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(this.name);
            buffer.append(':');
            buffer.append(Integer.toString(this.defaultPort));
            stringRep = buffer.toString();
        }
        return stringRep;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Scheme) {
            final Scheme that = (Scheme) obj;
            return this.name.equals(that.name)
                && this.defaultPort == that.defaultPort
                && this.layered == that.layered;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.defaultPort);
        hash = LangUtils.hashCode(hash, this.name);
        hash = LangUtils.hashCode(hash, this.layered);
        return hash;
    }

}
