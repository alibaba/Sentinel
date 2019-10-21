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
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.util.Args;

/**
 * Abstract base class for all standard {@link X509HostnameVerifier}
 * implementations.
 *
 * @since 4.0
 *
 * @deprecated (4.4) use an implementation of {@link javax.net.ssl.HostnameVerifier} or
 *  {@link DefaultHostnameVerifier}.
 */
@Deprecated
public abstract class AbstractVerifier implements X509HostnameVerifier {

    private final Log log = LogFactory.getLog(getClass());

    final static String[] BAD_COUNTRY_2LDS =
            { "ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info",
                    "lg", "ne", "net", "or", "org" };

    static {
        // Just in case developer forgot to manually sort the array.  :-)
        Arrays.sort(BAD_COUNTRY_2LDS);
    }

    @Override
    public final void verify(final String host, final SSLSocket ssl)
            throws IOException {
        Args.notNull(host, "Host");
        SSLSession session = ssl.getSession();
        if(session == null) {
            // In our experience this only happens under IBM 1.4.x when
            // spurious (unrelated) certificates show up in the server'
            // chain.  Hopefully this will unearth the real problem:
            final InputStream in = ssl.getInputStream();
            in.available();
            /*
              If you're looking at the 2 lines of code above because
              you're running into a problem, you probably have two
              options:

                #1.  Clean up the certificate chain that your server
                     is presenting (e.g. edit "/etc/apache2/server.crt"
                     or wherever it is your server's certificate chain
                     is defined).

                                           OR

                #2.   Upgrade to an IBM 1.5.x or greater JVM, or switch
                      to a non-IBM JVM.
            */

            // If ssl.getInputStream().available() didn't cause an
            // exception, maybe at least now the session is available?
            session = ssl.getSession();
            if(session == null) {
                // If it's still null, probably a startHandshake() will
                // unearth the real problem.
                ssl.startHandshake();

                // Okay, if we still haven't managed to cause an exception,
                // might as well go for the NPE.  Or maybe we're okay now?
                session = ssl.getSession();
            }
        }

        final Certificate[] certs = session.getPeerCertificates();
        final X509Certificate x509 = (X509Certificate) certs[0];
        verify(host, x509);
    }

    @Override
    public final boolean verify(final String host, final SSLSession session) {
        try {
            final Certificate[] certs = session.getPeerCertificates();
            final X509Certificate x509 = (X509Certificate) certs[0];
            verify(host, x509);
            return true;
        } catch(final SSLException ex) {
            if (log.isDebugEnabled()) {
                log.debug(ex.getMessage(), ex);
            }
            return false;
        }
    }

    @Override
    public final void verify(
            final String host, final X509Certificate cert) throws SSLException {
        final List<SubjectName> allSubjectAltNames = DefaultHostnameVerifier.getSubjectAltNames(cert);
        final List<String> subjectAlts = new ArrayList<String>();
        if (InetAddressUtils.isIPv4Address(host) || InetAddressUtils.isIPv6Address(host)) {
            for (SubjectName subjectName: allSubjectAltNames) {
                if (subjectName.getType() == SubjectName.IP) {
                    subjectAlts.add(subjectName.getValue());
                }
            }
        } else {
            for (SubjectName subjectName: allSubjectAltNames) {
                if (subjectName.getType() == SubjectName.DNS) {
                    subjectAlts.add(subjectName.getValue());
                }
            }
        }
        final X500Principal subjectPrincipal = cert.getSubjectX500Principal();
        final String cn = DefaultHostnameVerifier.extractCN(subjectPrincipal.getName(X500Principal.RFC2253));
        verify(host,
                cn != null ? new String[] {cn} : null,
                subjectAlts != null && !subjectAlts.isEmpty() ? subjectAlts.toArray(new String[subjectAlts.size()]) : null);
    }

    public final void verify(final String host, final String[] cns,
                             final String[] subjectAlts,
                             final boolean strictWithSubDomains)
            throws SSLException {

        final String cn = cns != null && cns.length > 0 ? cns[0] : null;
        final List<String> subjectAltList = subjectAlts != null && subjectAlts.length > 0 ? Arrays.asList(subjectAlts) : null;

        final String normalizedHost = InetAddressUtils.isIPv6Address(host) ?
                DefaultHostnameVerifier.normaliseAddress(host.toLowerCase(Locale.ROOT)) : host;

        if (subjectAltList != null) {
            for (final String subjectAlt: subjectAltList) {
                final String normalizedAltSubject = InetAddressUtils.isIPv6Address(subjectAlt) ?
                        DefaultHostnameVerifier.normaliseAddress(subjectAlt) : subjectAlt;
                if (matchIdentity(normalizedHost, normalizedAltSubject, strictWithSubDomains)) {
                    return;
                }
            }
            throw new SSLException("Certificate for <" + host + "> doesn't match any " +
                    "of the subject alternative names: " + subjectAltList);
        } else if (cn != null) {
            final String normalizedCN = InetAddressUtils.isIPv6Address(cn) ?
                    DefaultHostnameVerifier.normaliseAddress(cn) : cn;
            if (matchIdentity(normalizedHost, normalizedCN, strictWithSubDomains)) {
                return;
            }
            throw new SSLException("Certificate for <" + host + "> doesn't match " +
                    "common name of the certificate subject: " + cn);
        } else {
            throw new SSLException("Certificate subject for <" + host + "> doesn't contain " +
                    "a common name and does not have alternative names");
        }
    }

    private static boolean matchIdentity(final String host, final String identity, final boolean strict) {
        if (host == null) {
            return false;
        }
        final String normalizedHost = host.toLowerCase(Locale.ROOT);
        final String normalizedIdentity = identity.toLowerCase(Locale.ROOT);
        // The CN better have at least two dots if it wants wildcard
        // action.  It also can't be [*.co.uk] or [*.co.jp] or
        // [*.org.uk], etc...
        final String parts[] = normalizedIdentity.split("\\.");
        final boolean doWildcard = parts.length >= 3 && parts[0].endsWith("*") &&
                (!strict || validCountryWildcard(parts));
        if (doWildcard) {
            boolean match;
            final String firstpart = parts[0];
            if (firstpart.length() > 1) { // e.g. server*
                final String prefix = firstpart.substring(0, firstpart.length() - 1); // e.g. server
                final String suffix = normalizedIdentity.substring(firstpart.length()); // skip wildcard part from cn
                final String hostSuffix = normalizedHost.substring(prefix.length()); // skip wildcard part from normalizedHost
                match = normalizedHost.startsWith(prefix) && hostSuffix.endsWith(suffix);
            } else {
                match = normalizedHost.endsWith(normalizedIdentity.substring(1));
            }
            return match && (!strict || countDots(normalizedHost) == countDots(normalizedIdentity));
        } else {
            return normalizedHost.equals(normalizedIdentity);
        }
    }

    private static boolean validCountryWildcard(final String parts[]) {
        if (parts.length != 3 || parts[2].length() != 2) {
            return true; // it's not an attempt to wildcard a 2TLD within a country code
        }
        return Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
    }

    public static boolean acceptableCountryWildcard(final String cn) {
        return validCountryWildcard(cn.split("\\."));
    }

    public static String[] getCNs(final X509Certificate cert) {
        final String subjectPrincipal = cert.getSubjectX500Principal().toString();
        try {
            final String cn = DefaultHostnameVerifier.extractCN(subjectPrincipal);
            return cn != null ? new String[] { cn } : null;
        } catch (final SSLException ex) {
            return null;
        }
    }

    /**
     * Extracts the array of SubjectAlt DNS names from an X509Certificate.
     * Returns null if there aren't any.
     * <p>
     * Note:  Java doesn't appear able to extract international characters
     * from the SubjectAlts.  It can only extract international characters
     * from the CN field.
     * </p>
     * <p>
     * (Or maybe the version of OpenSSL I'm using to test isn't storing the
     * international characters correctly in the SubjectAlts?).
     * </p>
     *
     * @param cert X509Certificate
     * @return Array of SubjectALT DNS names stored in the certificate.
     */
    public static String[] getDNSSubjectAlts(final X509Certificate cert) {
        final List<SubjectName> subjectAltNames = DefaultHostnameVerifier.getSubjectAltNames(cert);
        if (subjectAltNames == null) {
            return null;
        }
        final List<String> dnsAlts = new ArrayList<String>();
        for (SubjectName subjectName: subjectAltNames) {
            if (subjectName.getType() == SubjectName.DNS) {
                dnsAlts.add(subjectName.getValue());
            }
        }
        return dnsAlts.isEmpty() ? dnsAlts.toArray(new String[dnsAlts.size()]) : null;
    }

    /**
     * Counts the number of dots "." in a string.
     * @param s  string to count dots from
     * @return  number of dots
     */
    public static int countDots(final String s) {
        int count = 0;
        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }

}
