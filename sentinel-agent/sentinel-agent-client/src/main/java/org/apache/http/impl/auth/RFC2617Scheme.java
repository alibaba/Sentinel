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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HttpRequest;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.CharsetUtils;

/**
 * Abstract authentication scheme class that lays foundation for all
 * RFC 2617 compliant authentication schemes and provides capabilities common
 * to all authentication schemes defined in RFC 2617.
 *
 * @since 4.0
 */
@SuppressWarnings("deprecation")
public abstract class RFC2617Scheme extends AuthSchemeBase implements Serializable {

    private static final long serialVersionUID = -2845454858205884623L;

    private final Map<String, String> params;
    private transient Charset credentialsCharset;

    /**
     * Creates an instance of {@code RFC2617Scheme} with the given challenge
     * state.
     *
     * @since 4.2
     *
     * @deprecated (4.3) do not use.
     */
    @Deprecated
    public RFC2617Scheme(final ChallengeState challengeState) {
        super(challengeState);
        this.params = new HashMap<String, String>();
        this.credentialsCharset = Consts.ASCII;
    }

    /**
     * @since 4.3
     */
    public RFC2617Scheme(final Charset credentialsCharset) {
        super();
        this.params = new HashMap<String, String>();
        this.credentialsCharset = credentialsCharset != null ? credentialsCharset : Consts.ASCII;
    }

    public RFC2617Scheme() {
        this(Consts.ASCII);
    }


    /**
     * @since 4.3
     */
    public Charset getCredentialsCharset() {
        return credentialsCharset != null ? credentialsCharset : Consts.ASCII;
    }

    String getCredentialsCharset(final HttpRequest request) {
        String charset = (String) request.getParams().getParameter(AuthPNames.CREDENTIAL_CHARSET);
        if (charset == null) {
            charset = getCredentialsCharset().name();
        }
        return charset;
    }

    @Override
    protected void parseChallenge(
            final CharArrayBuffer buffer, final int pos, final int len) throws MalformedChallengeException {
        final HeaderValueParser parser = BasicHeaderValueParser.INSTANCE;
        final ParserCursor cursor = new ParserCursor(pos, buffer.length());
        final HeaderElement[] elements = parser.parseElements(buffer, cursor);
        this.params.clear();
        for (final HeaderElement element : elements) {
            this.params.put(element.getName().toLowerCase(Locale.ROOT), element.getValue());
        }
    }

    /**
     * Returns authentication parameters map. Keys in the map are lower-cased.
     *
     * @return the map of authentication parameters
     */
    protected Map<String, String> getParameters() {
        return this.params;
    }

    /**
     * Returns authentication parameter with the given name, if available.
     *
     * @param name The name of the parameter to be returned
     *
     * @return the parameter with the given name
     */
    @Override
    public String getParameter(final String name) {
        if (name == null) {
            return null;
        }
        return this.params.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns authentication realm. The realm may not be null.
     *
     * @return the authentication realm
     */
    @Override
    public String getRealm() {
        return getParameter("realm");
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(this.credentialsCharset.name());
        out.writeObject(this.challengeState);
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.credentialsCharset = CharsetUtils.get(in.readUTF());
        if (this.credentialsCharset == null) {
            this.credentialsCharset = Consts.ASCII;
        }
        this.challengeState = (ChallengeState) in.readObject();
    }

    private void readObjectNoData() throws ObjectStreamException {
    }

}
