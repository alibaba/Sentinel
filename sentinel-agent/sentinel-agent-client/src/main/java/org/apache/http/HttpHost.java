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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Locale;

import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.annotation.Contract;
import org.apache.http.util.Args;
import org.apache.http.util.LangUtils;

/**
 * Holds all of the variables needed to describe an HTTP connection to a host.
 * This includes remote host name, port and scheme.
 *
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class HttpHost implements Cloneable, Serializable {

    private static final long serialVersionUID = -7529410654042457626L;

    /** The default scheme is "http". */
    public static final String DEFAULT_SCHEME_NAME = "http";

    /** The host to use. */
    protected final String hostname;

    /** The lowercase host, for {@link #equals} and {@link #hashCode}. */
    protected final String lcHostname;


    /** The port to use, defaults to -1 if not set. */
    protected final int port;

    /** The scheme (lowercased) */
    protected final String schemeName;

    protected final InetAddress address;

    /**
     * Creates {@code HttpHost} instance with the given scheme, hostname and port.
     *
     * @param hostname  the hostname (IP or DNS name)
     * @param port      the port number.
     *                  {@code -1} indicates the scheme default port.
     * @param scheme    the name of the scheme.
     *                  {@code null} indicates the
     *                  {@link #DEFAULT_SCHEME_NAME default scheme}
     */
    public HttpHost(final String hostname, final int port, final String scheme) {
        super();
        this.hostname   = Args.containsNoBlanks(hostname, "Host name");
        this.lcHostname = hostname.toLowerCase(Locale.ROOT);
        if (scheme != null) {
            this.schemeName = scheme.toLowerCase(Locale.ROOT);
        } else {
            this.schemeName = DEFAULT_SCHEME_NAME;
        }
        this.port = port;
        this.address = null;
    }

    /**
     * Creates {@code HttpHost} instance with the default scheme and the given hostname and port.
     *
     * @param hostname  the hostname (IP or DNS name)
     * @param port      the port number.
     *                  {@code -1} indicates the scheme default port.
     */
    public HttpHost(final String hostname, final int port) {
        this(hostname, port, null);
    }

    /**
     * Creates {@code HttpHost} instance from string. Text may not contain any blanks.
     *
     * @since 4.4
     */
    public static HttpHost create(final String s) {
        Args.containsNoBlanks(s, "HTTP Host");
        String text = s;
        String scheme = null;
        final int schemeIdx = text.indexOf("://");
        if (schemeIdx > 0) {
            scheme = text.substring(0, schemeIdx);
            text = text.substring(schemeIdx + 3);
        }
        int port = -1;
        final int portIdx = text.lastIndexOf(":");
        if (portIdx > 0) {
            try {
                port = Integer.parseInt(text.substring(portIdx + 1));
            } catch (final NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid HTTP host: " + text);
            }
            text = text.substring(0, portIdx);
        }
        return new HttpHost(text, port, scheme);
    }

    /**
     * Creates {@code HttpHost} instance with the default scheme and port and the given hostname.
     *
     * @param hostname  the hostname (IP or DNS name)
     */
    public HttpHost(final String hostname) {
        this(hostname, -1, null);
    }

    /**
     * Creates {@code HttpHost} instance with the given scheme, inet address and port.
     *
     * @param address   the inet address.
     * @param port      the port number.
     *                  {@code -1} indicates the scheme default port.
     * @param scheme    the name of the scheme.
     *                  {@code null} indicates the
     *                  {@link #DEFAULT_SCHEME_NAME default scheme}
     *
     * @since 4.3
     */
    public HttpHost(final InetAddress address, final int port, final String scheme) {
        this(Args.notNull(address,"Inet address"), address.getHostName(), port, scheme);
    }
    /**
     * Creates a new {@link HttpHost HttpHost}, specifying all values.
     * Constructor for HttpHost.
     *
     * @param address   the inet address.
     * @param hostname   the hostname (IP or DNS name)
     * @param port      the port number.
     *                  {@code -1} indicates the scheme default port.
     * @param scheme    the name of the scheme.
     *                  {@code null} indicates the
     *                  {@link #DEFAULT_SCHEME_NAME default scheme}
     *
     * @since 4.4
     */
    public HttpHost(final InetAddress address, final String hostname, final int port, final String scheme) {
        super();
        this.address = Args.notNull(address, "Inet address");
        this.hostname = Args.notNull(hostname, "Hostname");
        this.lcHostname = this.hostname.toLowerCase(Locale.ROOT);
        if (scheme != null) {
            this.schemeName = scheme.toLowerCase(Locale.ROOT);
        } else {
            this.schemeName = DEFAULT_SCHEME_NAME;
        }
        this.port = port;
    }

    /**
     * Creates {@code HttpHost} instance with the default scheme and the given inet address
     * and port.
     *
     * @param address   the inet address.
     * @param port      the port number.
     *                  {@code -1} indicates the scheme default port.
     *
     * @since 4.3
     */
    public HttpHost(final InetAddress address, final int port) {
        this(address, port, null);
    }

    /**
     * Creates {@code HttpHost} instance with the default scheme and port and the given inet
     * address.
     *
     * @param address   the inet address.
     *
     * @since 4.3
     */
    public HttpHost(final InetAddress address) {
        this(address, -1, null);
    }

    /**
     * Copy constructor for {@link HttpHost HttpHost}.
     *
     * @param httphost the HTTP host to copy details from
     */
    public HttpHost (final HttpHost httphost) {
        super();
        Args.notNull(httphost, "HTTP host");
        this.hostname   = httphost.hostname;
        this.lcHostname = httphost.lcHostname;
        this.schemeName = httphost.schemeName;
        this.port = httphost.port;
        this.address = httphost.address;
    }

    /**
     * Returns the host name.
     *
     * @return the host name (IP or DNS name)
     */
    public String getHostName() {
        return this.hostname;
    }

    /**
     * Returns the port.
     *
     * @return the host port, or {@code -1} if not set
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns the scheme name.
     *
     * @return the scheme name
     */
    public String getSchemeName() {
        return this.schemeName;
    }

    /**
     * Returns the inet address if explicitly set by a constructor,
     *   {@code null} otherwise.
     * @return the inet address
     *
     * @since 4.3
     */
    public InetAddress getAddress() {
        return this.address;
    }

    /**
     * Return the host URI, as a string.
     *
     * @return the host URI
     */
    public String toURI() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(this.schemeName);
        buffer.append("://");
        buffer.append(this.hostname);
        if (this.port != -1) {
            buffer.append(':');
            buffer.append(Integer.toString(this.port));
        }
        return buffer.toString();
    }


    /**
     * Obtains the host string, without scheme prefix.
     *
     * @return  the host string, for example {@code localhost:8080}
     */
    public String toHostString() {
        if (this.port != -1) {
            //the highest port number is 65535, which is length 6 with the addition of the colon
            final StringBuilder buffer = new StringBuilder(this.hostname.length() + 6);
            buffer.append(this.hostname);
            buffer.append(":");
            buffer.append(Integer.toString(this.port));
            return buffer.toString();
        } else {
            return this.hostname;
        }
    }


    @Override
    public String toString() {
        return toURI();
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof HttpHost) {
            final HttpHost that = (HttpHost) obj;
            return this.lcHostname.equals(that.lcHostname)
                && this.port == that.port
                && this.schemeName.equals(that.schemeName)
                && (this.address==null ? that.address== null : this.address.equals(that.address));
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.lcHostname);
        hash = LangUtils.hashCode(hash, this.port);
        hash = LangUtils.hashCode(hash, this.schemeName);
        if (address!=null) {
            hash = LangUtils.hashCode(hash, address);
        }
        return hash;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
