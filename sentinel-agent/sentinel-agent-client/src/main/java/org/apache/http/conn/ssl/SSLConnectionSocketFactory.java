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
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.Args;
import org.apache.http.util.TextUtils;

/**
 * Layered socket factory for TLS/SSL connections.
 * <p>
 * SSLSocketFactory can be used to validate the identity of the HTTPS server against a list of
 * trusted certificates and to authenticate to the HTTPS server using a private key.
 * <p>
 * SSLSocketFactory will enable server authentication when supplied with
 * a {@link java.security.KeyStore trust-store} file containing one or several trusted certificates. The client
 * secure socket will reject the connection during the SSL session handshake if the target HTTPS
 * server attempts to authenticate itself with a non-trusted certificate.
 * <p>
 * Use JDK keytool utility to import a trusted certificate and generate a trust-store file:
 *    <pre>
 *     keytool -import -alias "my server cert" -file server.crt -keystore my.truststore
 *    </pre>
 * <p>
 * In special cases the standard trust verification process can be bypassed by using a custom
 * {@link org.apache.http.conn.ssl.TrustStrategy}. This interface is primarily intended for allowing self-signed
 * certificates to be accepted as trusted without having to add them to the trust-store file.
 * <p>
 * SSLSocketFactory will enable client authentication when supplied with
 * a {@link java.security.KeyStore key-store} file containing a private key/public certificate
 * pair. The client secure socket will use the private key to authenticate
 * itself to the target HTTPS server during the SSL session handshake if
 * requested to do so by the server.
 * The target HTTPS server will in its turn verify the certificate presented
 * by the client in order to establish client's authenticity.
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
 *      </p>
 *      <pre>keytool -certreq -alias "my client key" -file mycertreq.csr -keystore my.keystore</pre>
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
 *       <pre>keytool -import -alias "my trusted ca" -file caroot.crt -keystore my.keystore</pre>
 *     </li>
 *     <li>
 *       <p>
 *       Import the PKCS#7 file containing the complete certificate chain
 *       </p>
 *       <pre>keytool -import -alias "my client key" -file mycert.p7 -keystore my.keystore</pre>
 *     </li>
 *     <li>
 *       <p>
 *       Verify the content of the resultant keystore file
 *       </p>
 *       <pre>keytool -list -v -keystore my.keystore</pre>
 *     </li>
 *   </ul>
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.SAFE)
@SuppressWarnings("deprecation")
public class SSLConnectionSocketFactory implements LayeredConnectionSocketFactory {

    public static final String TLS   = "TLS";
    public static final String SSL   = "SSL";
    public static final String SSLV2 = "SSLv2";

    @Deprecated
    public static final X509HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER
        = AllowAllHostnameVerifier.INSTANCE;

    @Deprecated
    public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
        = BrowserCompatHostnameVerifier.INSTANCE;

    @Deprecated
    public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER
        = StrictHostnameVerifier.INSTANCE;

    private final Log log = LogFactory.getLog(getClass());

    /**
     * @since 4.4
     */
    public static HostnameVerifier getDefaultHostnameVerifier() {
        return new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
    }

    /**
     * Obtains default SSL socket factory with an SSL context based on the standard JSSE
     * trust material ({@code cacerts} file in the security properties directory).
     * System properties are not taken into consideration.
     *
     * @return default SSL socket factory
     */
    public static SSLConnectionSocketFactory getSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory(SSLContexts.createDefault(), getDefaultHostnameVerifier());
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
     * <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html">
     * Java&#x2122; Secure Socket Extension (JSSE) Reference Guide</a>.
     *
     * @return default system SSL socket factory
     */
    public static SSLConnectionSocketFactory getSystemSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory(
            (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault(),
            split(System.getProperty("https.protocols")),
            split(System.getProperty("https.cipherSuites")),
            getDefaultHostnameVerifier());
    }

    private final javax.net.ssl.SSLSocketFactory socketfactory;
    private final HostnameVerifier hostnameVerifier;
    private final String[] supportedProtocols;
    private final String[] supportedCipherSuites;

    public SSLConnectionSocketFactory(final SSLContext sslContext) {
        this(sslContext, getDefaultHostnameVerifier());
    }

    /**
     * @deprecated (4.4) Use {@link #SSLConnectionSocketFactory(javax.net.ssl.SSLContext,
     *   javax.net.ssl.HostnameVerifier)}
     */
    @Deprecated
    public SSLConnectionSocketFactory(
            final SSLContext sslContext, final X509HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
                null, null, hostnameVerifier);
    }

    /**
     * @deprecated (4.4) Use {@link #SSLConnectionSocketFactory(javax.net.ssl.SSLContext,
     *   String[], String[], javax.net.ssl.HostnameVerifier)}
     */
    @Deprecated
    public SSLConnectionSocketFactory(
            final SSLContext sslContext,
            final String[] supportedProtocols,
            final String[] supportedCipherSuites,
            final X509HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
                supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    /**
     * @deprecated (4.4) Use {@link #SSLConnectionSocketFactory(javax.net.ssl.SSLSocketFactory,
     *   javax.net.ssl.HostnameVerifier)}
     */
    @Deprecated
    public SSLConnectionSocketFactory(
            final javax.net.ssl.SSLSocketFactory socketfactory,
            final X509HostnameVerifier hostnameVerifier) {
        this(socketfactory, null, null, hostnameVerifier);
    }

    /**
     * @deprecated (4.4) Use {@link #SSLConnectionSocketFactory(javax.net.ssl.SSLSocketFactory,
     *   String[], String[], javax.net.ssl.HostnameVerifier)}
     */
    @Deprecated
    public SSLConnectionSocketFactory(
            final javax.net.ssl.SSLSocketFactory socketfactory,
            final String[] supportedProtocols,
            final String[] supportedCipherSuites,
            final X509HostnameVerifier hostnameVerifier) {
        this(socketfactory, supportedProtocols, supportedCipherSuites, (HostnameVerifier) hostnameVerifier);
    }

    /**
     * @since 4.4
     */
    public SSLConnectionSocketFactory(
            final SSLContext sslContext, final HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
                null, null, hostnameVerifier);
    }

    /**
     * @since 4.4
     */
    public SSLConnectionSocketFactory(
            final SSLContext sslContext,
            final String[] supportedProtocols,
            final String[] supportedCipherSuites,
            final HostnameVerifier hostnameVerifier) {
        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
                supportedProtocols, supportedCipherSuites, hostnameVerifier);
    }

    /**
     * @since 4.4
     */
    public SSLConnectionSocketFactory(
            final javax.net.ssl.SSLSocketFactory socketfactory,
            final HostnameVerifier hostnameVerifier) {
        this(socketfactory, null, null, hostnameVerifier);
    }

    /**
     * @since 4.4
     */
    public SSLConnectionSocketFactory(
            final javax.net.ssl.SSLSocketFactory socketfactory,
            final String[] supportedProtocols,
            final String[] supportedCipherSuites,
            final HostnameVerifier hostnameVerifier) {
        this.socketfactory = Args.notNull(socketfactory, "SSL socket factory");
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
        this.hostnameVerifier = hostnameVerifier != null ? hostnameVerifier : getDefaultHostnameVerifier();
    }

    /**
     * Performs any custom initialization for a newly created SSLSocket
     * (before the SSL handshake happens).
     *
     * The default implementation is a no-op, but could be overridden to, e.g.,
     * call {@link javax.net.ssl.SSLSocket#setEnabledCipherSuites(String[])}.
     * @throws IOException may be thrown if overridden
     */
    protected void prepareSocket(final SSLSocket socket) throws IOException {
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
            if (connectTimeout > 0 && sock.getSoTimeout() == 0) {
                sock.setSoTimeout(connectTimeout);
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Connecting socket to " + remoteAddress + " with timeout " + connectTimeout);
            }
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
            this.log.debug("Starting handshake");
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
        if (supportedProtocols != null) {
            sslsock.setEnabledProtocols(supportedProtocols);
        } else {
            // If supported protocols are not explicitly set, remove all SSL protocol versions
            final String[] allProtocols = sslsock.getEnabledProtocols();
            final List<String> enabledProtocols = new ArrayList<String>(allProtocols.length);
            for (final String protocol: allProtocols) {
                if (!protocol.startsWith("SSL")) {
                    enabledProtocols.add(protocol);
                }
            }
            if (!enabledProtocols.isEmpty()) {
                sslsock.setEnabledProtocols(enabledProtocols.toArray(new String[enabledProtocols.size()]));
            }
        }
        if (supportedCipherSuites != null) {
            sslsock.setEnabledCipherSuites(supportedCipherSuites);
        }

        if (this.log.isDebugEnabled()) {
            this.log.debug("Enabled protocols: " + Arrays.asList(sslsock.getEnabledProtocols()));
            this.log.debug("Enabled cipher suites:" + Arrays.asList(sslsock.getEnabledCipherSuites()));
        }

        prepareSocket(sslsock);
        this.log.debug("Starting handshake");
        sslsock.startHandshake();
        verifyHostname(sslsock, target);
        return sslsock;
    }

    private void verifyHostname(final SSLSocket sslsock, final String hostname) throws IOException {
        try {
            SSLSession session = sslsock.getSession();
            if (session == null) {
                // In our experience this only happens under IBM 1.4.x when
                // spurious (unrelated) certificates show up in the server'
                // chain.  Hopefully this will unearth the real problem:
                final InputStream in = sslsock.getInputStream();
                in.available();
                // If ssl.getInputStream().available() didn't cause an
                // exception, maybe at least now the session is available?
                session = sslsock.getSession();
                if (session == null) {
                    // If it's still null, probably a startHandshake() will
                    // unearth the real problem.
                    sslsock.startHandshake();
                    session = sslsock.getSession();
                }
            }
            if (session == null) {
                throw new SSLHandshakeException("SSL session not available");
            }

            if (this.log.isDebugEnabled()) {
                this.log.debug("Secure session established");
                this.log.debug(" negotiated protocol: " + session.getProtocol());
                this.log.debug(" negotiated cipher suite: " + session.getCipherSuite());

                try {

                    final Certificate[] certs = session.getPeerCertificates();
                    final X509Certificate x509 = (X509Certificate) certs[0];
                    final X500Principal peer = x509.getSubjectX500Principal();

                    this.log.debug(" peer principal: " + peer.toString());
                    final Collection<List<?>> altNames1 = x509.getSubjectAlternativeNames();
                    if (altNames1 != null) {
                        final List<String> altNames = new ArrayList<String>();
                        for (final List<?> aC : altNames1) {
                            if (!aC.isEmpty()) {
                                altNames.add((String) aC.get(1));
                            }
                        }
                        this.log.debug(" peer alternative names: " + altNames);
                    }

                    final X500Principal issuer = x509.getIssuerX500Principal();
                    this.log.debug(" issuer principal: " + issuer.toString());
                    final Collection<List<?>> altNames2 = x509.getIssuerAlternativeNames();
                    if (altNames2 != null) {
                        final List<String> altNames = new ArrayList<String>();
                        for (final List<?> aC : altNames2) {
                            if (!aC.isEmpty()) {
                                altNames.add((String) aC.get(1));
                            }
                        }
                        this.log.debug(" issuer alternative names: " + altNames);
                    }
                } catch (final Exception ignore) {
                }
            }

            if (!this.hostnameVerifier.verify(hostname, session)) {
                final Certificate[] certs = session.getPeerCertificates();
                final X509Certificate x509 = (X509Certificate) certs[0];
                final List<SubjectName> subjectAlts = DefaultHostnameVerifier.getSubjectAltNames(x509);
                throw new SSLPeerUnverifiedException("Certificate for <" + hostname + "> doesn't match any " +
                        "of the subject alternative names: " + subjectAlts);
            }
            // verifyHostName() didn't blowup - good!
        } catch (final IOException iox) {
            // close the socket before re-throwing the exception
            try { sslsock.close(); } catch (final Exception x) { /*ignore*/ }
            throw iox;
        }
    }

}
