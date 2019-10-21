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

package org.apache.http.conn.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpInetSocketAddress;
import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.scheme.LayeredSchemeSocketFactory;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SchemeLayeredSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.TextUtils;

/**
 * Layered socket factory for TLS/SSL connections.
 * <p>
 * SSLSocketFactory can be used to validate the identity of the HTTPS server against a list of
 * trusted certificates and to authenticate to the HTTPS server using a private key.
 * </p>
 * <p>
 * SSLSocketFactory will enable server authentication when supplied with
 * a {@link KeyStore trust-store} file containing one or several trusted certificates. The client
 * secure socket will reject the connection during the SSL session handshake if the target HTTPS
 * server attempts to authenticate itself with a non-trusted certificate.
 * </p>
 * <p>
 * Use JDK keytool utility to import a trusted certificate and generate a trust-store file:
 * </p>
 * <pre>keytool -import -alias "my server cert" -file server.crt -keystore my.truststore
 * </pre>
 * <p>
 * In special cases the standard trust verification process can be bypassed by using a custom
 * {@link TrustStrategy}. This interface is primarily intended for allowing self-signed
 * certificates to be accepted as trusted without having to add them to the trust-store file.
 * </p>
 * <p>
 * SSLSocketFactory will enable client authentication when supplied with
 * a {@link KeyStore key-store} file containing a private key/public certificate
 * pair. The client secure socket will use the private key to authenticate
 * itself to the target HTTPS server during the SSL session handshake if
 * requested to do so by the server.
 * The target HTTPS server will in its turn verify the certificate presented
 * by the client in order to establish client's authenticity.
 * </p>
 * <p>
 * Use the following sequence of actions to generate a key-store file
 * </p>
 *   <ul>
 *     <li>
 *      <p>
 *      Use JDK keytool utility to generate a new key
 *      </p>
 *      <pre>keytool -genkey -v -alias "my client key" -validity 365 -keystore my.keystore</pre>
 *      <p>
 *      For simplicity use the same password for the key as that of the key-store
 *      </p>
 *     </li>
 *     <li>
 *      <p>
 *      Issue a certificate signing request (CSR)
 *     </p>
 *     <pre>keytool -certreq -alias "my client key" -file mycertreq.csr -keystore my.keystore</pre>
 *     </li>
 *     <li>
 *      <p>
 *      Send the certificate request to the trusted Certificate Authority for signature.
 *      One may choose to act as her own CA and sign the certificate request using a PKI
 *      tool, such as OpenSSL.
 *      </p>
 *     </li>
 *     <li>
 *      <p>
 *       Import the trusted CA root certificate
 *      </p>
 *      <pre>keytool -import -alias "my trusted ca" -file caroot.crt -keystore my.keystore</pre>
 *     </li>
 *     <li>
 *      <p>
 *       Import the PKCS#7 file containg the complete certificate chain
 *      </p>
 *      <pre>keytool -import -alias "my client key" -file mycert.p7 -keystore my.keystore</pre>
 *     </li>
 *     <li>
 *      <p>
 *       Verify the content the resultant keystore file
 *      </p>
 *      <pre>keytool -list -v -keystore my.keystore</pre>
 *     </li>
 *   </ul>
 *
 * @since 4.0
 *
 * @deprecated (4.3) use {@link SSLConnectionSocketFactory}.
 */
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
@Deprecated
public class SSLSocketFactory implements LayeredConnectionSocketFactory, SchemeLayeredSocketFactory,
                                         LayeredSchemeSocketFactory, LayeredSocketFactory {

    public static final String TLS   = "TLS";
    public static final String SSL   = "SSL";
    public static final String SSLV2 = "SSLv2";

    public static final X509HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER
        = new AllowAllHostnameVerifier();

    public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
        = new BrowserCompatHostnameVerifier();

    public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER
        = new StrictHostnameVerifier();

    /**
     * Obtains default SSL socket factory with an SSL context based on the standard JSSE
     * trust material ({@code cacerts} file in the security properties directory).
     * System properties are not taken into consideration.
     *
     * @return default SSL socket factory
     */
    public static SSLSocketFactory getSocketFactory() throws SSLInitializationException {
        return new SSLSocketFactory(
            SSLContexts.createDefault(),
            BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    private static String[] split(final String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    /**
     * Obtains default SSL socket factory with an SSL context based on system properties
     * as described in
     * <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html">
     * "JavaTM Secure Socket Extension (JSSE) Reference Guide for the JavaTM 2 Platform
     * Standard Edition 5</a>
     *
     * @return default system SSL socket factory
     */
    public static SSLSocketFactory getSystemSocketFactory() throws SSLInitializationException {
        return new SSLSocketFactory(
            (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault(),
            split(System.getProperty("https.protocols")),
            split(System.getProperty("https.cipherSuites")),
            BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    private final javax.net.ssl.SSLSocketFactory socketfactory;
    private final HostNameResolver nameResolver;
    // TODO: make final
    private volatile X509HostnameVerifier hostnameVerifier;
    private final String[] supportedProtocols;
    private final String[] supportedCipherSuites;

    public SSLSocketFactory(
            final String algorithm,
            final KeyStore keystore,
            final String keyPassword,
            final KeyStore truststore,
            final SecureRandom random,
            final HostNameResolver nameResolver)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(SSLContexts.custom()
                .useProtocol(algorithm)
                .setSecureRandom(random)
                .loadKeyMaterial(keystore, keyPassword != null ? keyPassword.toCharArray() : null)
                .loadTrustMaterial(truststore)
                .build(),
                nameResolver);
    }

    /**
     * @since 4.1
     */
    public SSLSocketFactory(
            final String algorithm,
            final KeyStore keystore,
            final String keyPassword,
            final KeyStore truststore,
            final SecureRandom random,
            final TrustStrategy trustStrategy,
            final X509HostnameVerifier hostnameVerifier)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(SSLContexts.custom()
                .useProtocol(algorithm)
                .setSecureRandom(random)
                .loadKeyMaterial(keystore, keyPassword != null ? keyPassword.toCharArray() : null)
                .loadTrustMaterial(truststore, trustStrategy)
                .build(),
                hostnameVerifier);
    }

    /**
     * @since 4.1
     */
    public SSLSocketFactory(
            final String algorithm,
            final KeyStore keystore,
            final String keyPassword,
            final KeyStore truststore,
            final SecureRandom random,
            final X509HostnameVerifier hostnameVerifier)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(SSLContexts.custom()
                .useProtocol(algorithm)
                .setSecureRandom(random)
                .loadKeyMaterial(keystore, keyPassword != null ? keyPassword.toCharArray() : null)
                .loadTrustMaterial(truststore)
                .build(),
                hostnameVerifier);
    }

    public SSLSocketFactory(
            final KeyStore keystore,
            final String keystorePassword,
            final KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(SSLContexts.custom()
                .loadKeyMaterial(keystore, keystorePassword != null ? keystorePassword.toCharArray() : null)
                .loadTrustMaterial(truststore)
                .build(),
                BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public SSLSocketFactory(
            final KeyStore keystore,
            final String keystorePassword)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException{
        this(SSLContexts.custom()
                .loadKeyMaterial(keystore, keystorePassword != null ? keystorePassword.toCharArray() : null)
                .build(),
                BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public SSLSocketFactory(
            final KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(SSLContexts.custom()
                .loadTrustMaterial(truststore)
                .build(),
                BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    /**
     * @since 4.1
     */
    public SSLSocketFactory(
            final TrustStrategy trustStrategy,
            final X509HostnameVerifier hostnameVerifier)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(SSLContexts.custom()
                .loadTrustMaterial(null, trustStrategy)
                .build(),
                hostnameVerifier);
    }

    /**
     * @since 4.1
     */
    public SSLSocketFactory(
            final TrustStrategy trustStrategy)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        this(SSLContexts.custom()
                .loadTrustMaterial(null, trustStrategy)
                .build(),
                BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public SSLSocketFactory(final SSLContext sslContext) {
        this(sslContext, BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public SSLSocketFactory(
            final SSLContext sslContext, final HostNameResolver nameResolver) {
        super();
        this.socketfactory = sslContext.getSocketFactory();
        this.hostnameVerifier = BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        this.nameResolver = nameResolver;
        this.supportedProtocols = null;
        this.supportedCipherSuites = null;
    }

    /**
     * @since 4.1
     */
    public SSLSocketFactory(
            final SSLContext sslContext, final X509HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
                null, null, hostnameVerifier);
    }

    /**
     * @since 4.3
     */
    public SSLSocketFactory(
            final SSLContext sslContext,
            final String[] supportedProtocols,
            final String[] supportedCipherSuites,
            final X509HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
                supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    /**
     * @since 4.2
     */
    public SSLSocketFactory(
            final javax.net.ssl.SSLSocketFactory socketfactory,
            final X509HostnameVerifier hostnameVerifier) {
        this(socketfactory, null, null, hostnameVerifier);
    }

    /**
     * @since 4.3
     */
    public SSLSocketFactory(
            final javax.net.ssl.SSLSocketFactory socketfactory,
            final String[] supportedProtocols,
            final String[] supportedCipherSuites,
            final X509HostnameVerifier hostnameVerifier) {
        this.socketfactory = Args.notNull(socketfactory, "SSL socket factory");
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
        this.hostnameVerifier = hostnameVerifier != null ? hostnameVerifier : BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        this.nameResolver = null;
    }

    /**
     * @param params Optional parameters. Parameters passed to this method will have no effect.
     *               This method will create a unconnected instance of {@link Socket} class.
     * @since 4.1
     */
    @Override
    public Socket createSocket(final HttpParams params) throws IOException {
        return createSocket((HttpContext) null);
    }

    @Override
    public Socket createSocket() throws IOException {
        return createSocket((HttpContext) null);
    }

    /**
     * @since 4.1
     */
    @Override
    public Socket connectSocket(
            final Socket socket,
            final InetSocketAddress remoteAddress,
            final InetSocketAddress localAddress,
            final HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
        Args.notNull(remoteAddress, "Remote address");
        Args.notNull(params, "HTTP parameters");
        final HttpHost host;
        if (remoteAddress instanceof HttpInetSocketAddress) {
            host = ((HttpInetSocketAddress) remoteAddress).getHttpHost();
        } else {
            host = new HttpHost(remoteAddress.getHostName(), remoteAddress.getPort(), "https");
        }
        final int socketTimeout = HttpConnectionParams.getSoTimeout(params);
        final int connectTimeout = HttpConnectionParams.getConnectionTimeout(params);
        socket.setSoTimeout(socketTimeout);
        return connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, null);
    }

    /**
     * Checks whether a socket connection is secure.
     * This factory creates TLS/SSL socket connections
     * which, by default, are considered secure.
     * <p>
     * Derived classes may override this method to perform
     * runtime checks, for example based on the cypher suite.
     * </p>
     *
     * @param sock      the connected socket
     *
     * @return  {@code true}
     *
     * @throws IllegalArgumentException if the argument is invalid
     */
    @Override
    public boolean isSecure(final Socket sock) throws IllegalArgumentException {
        Args.notNull(sock, "Socket");
        Asserts.check(sock instanceof SSLSocket, "Socket not created by this factory");
        Asserts.check(!sock.isClosed(), "Socket is closed");
        return true;
    }

    /**
     * @since 4.2
     */
    @Override
    public Socket createLayeredSocket(
        final Socket socket,
        final String host,
        final int port,
        final HttpParams params) throws IOException, UnknownHostException {
        return createLayeredSocket(socket, host, port, (HttpContext) null);
    }

    @Override
    public Socket createLayeredSocket(
        final Socket socket,
        final String host,
        final int port,
        final boolean autoClose) throws IOException, UnknownHostException {
        return createLayeredSocket(socket, host, port, (HttpContext) null);
    }

    public void setHostnameVerifier(final X509HostnameVerifier hostnameVerifier) {
        Args.notNull(hostnameVerifier, "Hostname verifier");
        this.hostnameVerifier = hostnameVerifier;
    }

    public X509HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    @Override
    public Socket connectSocket(
            final Socket socket,
            final String host, final int port,
            final InetAddress local, final int localPort,
            final HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
        final InetAddress remote;
        if (this.nameResolver != null) {
            remote = this.nameResolver.resolve(host);
        } else {
            remote = InetAddress.getByName(host);
        }
        InetSocketAddress localAddress = null;
        if (local != null || localPort > 0) {
            localAddress = new InetSocketAddress(local, localPort > 0 ? localPort : 0);
        }
        final InetSocketAddress remoteAddress = new HttpInetSocketAddress(
                new HttpHost(host, port), remote, port);
        return connectSocket(socket, remoteAddress, localAddress, params);
    }

    @Override
    public Socket createSocket(
            final Socket socket,
            final String host, final int port,
            final boolean autoClose) throws IOException, UnknownHostException {
        return createLayeredSocket(socket, host, port, autoClose);
    }

    /**
     * Performs any custom initialization for a newly created SSLSocket
     * (before the SSL handshake happens).
     *
     * The default implementation is a no-op, but could be overridden to, e.g.,
     * call {@link SSLSocket#setEnabledCipherSuites(java.lang.String[])}.
     * @throws IOException (only if overridden)
     *
     * @since 4.2
     */
    protected void prepareSocket(final SSLSocket socket) throws IOException {
    }

    private void internalPrepareSocket(final SSLSocket socket) throws IOException {
        if (supportedProtocols != null) {
            socket.setEnabledProtocols(supportedProtocols);
        }
        if (supportedCipherSuites != null) {
            socket.setEnabledCipherSuites(supportedCipherSuites);
        }
        prepareSocket(socket);
    }

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        return SocketFactory.getDefault().createSocket();
    }

    @Override
    public Socket connectSocket(
            final int connectTimeout,
            final Socket socket,
            final HttpHost host,
            final InetSocketAddress remoteAddress,
            final InetSocketAddress localAddress,
            final HttpContext context) throws IOException {
        Args.notNull(host, "HTTP host");
        Args.notNull(remoteAddress, "Remote address");
        final Socket sock = socket != null ? socket : createSocket(context);
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        try {
            sock.connect(remoteAddress, connectTimeout);
        } catch (final IOException ex) {
            try {
                sock.close();
            } catch (final IOException ignore) {
            }
            throw ex;
        }
        // Setup SSL layering if necessary
        if (sock instanceof SSLSocket) {
            final SSLSocket sslsock = (SSLSocket) sock;
            sslsock.startHandshake();
            verifyHostname(sslsock, host.getHostName());
            return sock;
        } else {
            return createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context);
        }
    }

    @Override
    public Socket createLayeredSocket(
            final Socket socket,
            final String target,
            final int port,
            final HttpContext context) throws IOException {
        final SSLSocket sslsock = (SSLSocket) this.socketfactory.createSocket(
                socket,
                target,
                port,
                true);
        internalPrepareSocket(sslsock);
        sslsock.startHandshake();
        verifyHostname(sslsock, target);
        return sslsock;
    }

    private void verifyHostname(final SSLSocket sslsock, final String hostname) throws IOException {
        try {
            this.hostnameVerifier.verify(hostname, sslsock);
            // verifyHostName() didn't blowup - good!
        } catch (final IOException iox) {
            // close the socket before re-throwing the exception
            try { sslsock.close(); } catch (final Exception x) { /*ignore*/ }
            throw iox;
        }
    }

}
