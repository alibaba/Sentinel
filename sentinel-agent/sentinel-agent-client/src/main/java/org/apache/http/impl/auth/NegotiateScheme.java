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
package org.apache.http.impl.auth;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

/**
 * SPNEGO (Simple and Protected GSSAPI Negotiation Mechanism) authentication
 * scheme.
 *
 * @since 4.1
 *
 * @deprecated (4.2)  use {@link SPNegoScheme} or {@link KerberosScheme}.
 */
@Deprecated
public class NegotiateScheme extends GGSSchemeBase {

    private final Log log = LogFactory.getLog(getClass());

    private static final String SPNEGO_OID       = "1.3.6.1.5.5.2";
    private static final String KERBEROS_OID     = "1.2.840.113554.1.2.2";

    private final SpnegoTokenGenerator spengoGenerator;

    /**
     * Default constructor for the Negotiate authentication scheme.
     *
     */
    public NegotiateScheme(final SpnegoTokenGenerator spengoGenerator, final boolean stripPort) {
        super(stripPort);
        this.spengoGenerator = spengoGenerator;
    }

    public NegotiateScheme(final SpnegoTokenGenerator spengoGenerator) {
        this(spengoGenerator, false);
    }

    public NegotiateScheme() {
        this(null, false);
    }

    /**
     * Returns textual designation of the Negotiate authentication scheme.
     *
     * @return {@code Negotiate}
     */
    @Override
    public String getSchemeName() {
        return "Negotiate";
    }

    @Override
    public Header authenticate(
            final Credentials credentials,
            final HttpRequest request) throws AuthenticationException {
        return authenticate(credentials, request, null);
    }

    /**
     * Produces Negotiate authorization Header based on token created by
     * processChallenge.
     *
     * @param credentials Never used be the Negotiate scheme but must be provided to
     * satisfy common-httpclient API. Credentials from JAAS will be used instead.
     * @param request The request being authenticated
     *
     * @throws AuthenticationException if authorisation string cannot
     *   be generated due to an authentication failure
     *
     * @return an Negotiate authorisation Header
     */
    @Override
    public Header authenticate(
            final Credentials credentials,
            final HttpRequest request,
            final HttpContext context) throws AuthenticationException {
        return super.authenticate(credentials, request, context);
    }

    @Override
    protected byte[] generateToken(final byte[] input, final String authServer) throws GSSException {
        return super.generateToken(input, authServer);
    }

    @Override
    protected byte[] generateToken(final byte[] input, final String authServer, final Credentials credentials) throws GSSException {
        /* Using the SPNEGO OID is the correct method.
         * Kerberos v5 works for IIS but not JBoss. Unwrapping
         * the initial token when using SPNEGO OID looks like what is
         * described here...
         *
         * http://msdn.microsoft.com/en-us/library/ms995330.aspx
         *
         * Another helpful URL...
         *
         * http://publib.boulder.ibm.com/infocenter/wasinfo/v7r0/index.jsp?topic=/com.ibm.websphere.express.doc/info/exp/ae/tsec_SPNEGO_token.html
         *
         * Unfortunately SPNEGO is JRE >=1.6.
         */

        /** Try SPNEGO by default, fall back to Kerberos later if error */
        Oid negotiationOid  = new Oid(SPNEGO_OID);

        byte[] token = input;
        boolean tryKerberos = false;
        try {
            token = generateGSSToken(token, negotiationOid, authServer, credentials);
        } catch (final GSSException ex){
            // BAD MECH means we are likely to be using 1.5, fall back to Kerberos MECH.
            // Rethrow any other exception.
            if (ex.getMajor() == GSSException.BAD_MECH ){
                log.debug("GSSException BAD_MECH, retry with Kerberos MECH");
                tryKerberos = true;
            } else {
                throw ex;
            }

        }
        if (tryKerberos){
            /* Kerberos v5 GSS-API mechanism defined in RFC 1964.*/
            log.debug("Using Kerberos MECH " + KERBEROS_OID);
            negotiationOid  = new Oid(KERBEROS_OID);
            token = generateGSSToken(token, negotiationOid, authServer, credentials);

            /*
             * IIS accepts Kerberos and SPNEGO tokens. Some other servers Jboss, Glassfish?
             * seem to only accept SPNEGO. Below wraps Kerberos into SPNEGO token.
             */
            if (token != null && spengoGenerator != null) {
                try {
                    token = spengoGenerator.generateSpnegoDERObject(token);
                } catch (final IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }
        return token;
    }

    /**
     * Returns the authentication parameter with the given name, if available.
     *
     * <p>There are no valid parameters for Negotiate authentication so this
     * method always returns {@code null}.</p>
     *
     * @param name The name of the parameter to be returned
     *
     * @return the parameter with the given name
     */
    @Override
    public String getParameter(final String name) {
        Args.notNull(name, "Parameter name");
        return null;
    }

    /**
     * The concept of an authentication realm is not supported by the Negotiate
     * authentication scheme. Always returns {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public String getRealm() {
        return null;
    }

    /**
     * Returns {@code true}.
     * Negotiate authentication scheme is connection based.
     *
     * @return {@code true}.
     */
    @Override
    public boolean isConnectionBased() {
        return true;
    }

}
