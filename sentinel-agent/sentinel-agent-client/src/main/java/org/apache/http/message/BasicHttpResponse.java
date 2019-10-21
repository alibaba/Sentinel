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

package org.apache.http.message;

import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.StatusLine;
import org.apache.http.util.Args;

/**
 * Basic implementation of {@link HttpResponse}.
 *
 * @see org.apache.http.impl.DefaultHttpResponseFactory
 *
 * @since 4.0
 */
public class BasicHttpResponse extends AbstractHttpMessage implements HttpResponse {

    private StatusLine          statusline;
    private ProtocolVersion     ver;
    private int                 code;
    private String              reasonPhrase;
    private HttpEntity          entity;
    private final ReasonPhraseCatalog reasonCatalog;
    private Locale              locale;

    /**
     * Creates a new response.
     * This is the constructor to which all others map.
     *
     * @param statusline        the status line
     * @param catalog           the reason phrase catalog, or
     *                          {@code null} to disable automatic
     *                          reason phrase lookup
     * @param locale            the locale for looking up reason phrases, or
     *                          {@code null} for the system locale
     */
    public BasicHttpResponse(final StatusLine statusline,
                             final ReasonPhraseCatalog catalog,
                             final Locale locale) {
        super();
        this.statusline = Args.notNull(statusline, "Status line");
        this.ver = statusline.getProtocolVersion();
        this.code = statusline.getStatusCode();
        this.reasonPhrase = statusline.getReasonPhrase();
        this.reasonCatalog = catalog;
        this.locale = locale;
    }

    /**
     * Creates a response from a status line.
     * The response will not have a reason phrase catalog and
     * use the system default locale.
     *
     * @param statusline        the status line
     */
    public BasicHttpResponse(final StatusLine statusline) {
        super();
        this.statusline = Args.notNull(statusline, "Status line");
        this.ver = statusline.getProtocolVersion();
        this.code = statusline.getStatusCode();
        this.reasonPhrase = statusline.getReasonPhrase();
        this.reasonCatalog = null;
        this.locale = null;
    }

    /**
     * Creates a response from elements of a status line.
     * The response will not have a reason phrase catalog and
     * use the system default locale.
     *
     * @param ver       the protocol version of the response
     * @param code      the status code of the response
     * @param reason    the reason phrase to the status code, or
     *                  {@code null}
     */
    public BasicHttpResponse(final ProtocolVersion ver,
                             final int code,
                             final String reason) {
        super();
        Args.notNegative(code, "Status code");
        this.statusline = null;
        this.ver = ver;
        this.code = code;
        this.reasonPhrase = reason;
        this.reasonCatalog = null;
        this.locale = null;
    }


    // non-javadoc, see interface HttpMessage
    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.ver;
    }

    // non-javadoc, see interface HttpResponse
    @Override
    public StatusLine getStatusLine() {
        if (this.statusline == null) {
            this.statusline = new BasicStatusLine(
                    this.ver != null ? this.ver : HttpVersion.HTTP_1_1,
                    this.code,
                    this.reasonPhrase != null ? this.reasonPhrase : getReason(this.code));
        }
        return this.statusline;
    }

    // non-javadoc, see interface HttpResponse
    @Override
    public HttpEntity getEntity() {
        return this.entity;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    // non-javadoc, see interface HttpResponse
    @Override
    public void setStatusLine(final StatusLine statusline) {
        this.statusline = Args.notNull(statusline, "Status line");
        this.ver = statusline.getProtocolVersion();
        this.code = statusline.getStatusCode();
        this.reasonPhrase = statusline.getReasonPhrase();
    }

    // non-javadoc, see interface HttpResponse
    @Override
    public void setStatusLine(final ProtocolVersion ver, final int code) {
        Args.notNegative(code, "Status code");
        this.statusline = null;
        this.ver = ver;
        this.code = code;
        this.reasonPhrase = null;
    }

    // non-javadoc, see interface HttpResponse
    @Override
    public void setStatusLine(
            final ProtocolVersion ver, final int code, final String reason) {
        Args.notNegative(code, "Status code");
        this.statusline = null;
        this.ver = ver;
        this.code = code;
        this.reasonPhrase = reason;
    }

    // non-javadoc, see interface HttpResponse
    @Override
    public void setStatusCode(final int code) {
        Args.notNegative(code, "Status code");
        this.statusline = null;
        this.code = code;
        this.reasonPhrase = null;
    }

    // non-javadoc, see interface HttpResponse
    @Override
    public void setReasonPhrase(final String reason) {
        this.statusline = null;
        this.reasonPhrase = reason;
    }

    // non-javadoc, see interface HttpResponse
    @Override
    public void setEntity(final HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public void setLocale(final Locale locale) {
        this.locale = Args.notNull(locale, "Locale");
        this.statusline = null;
    }

    /**
     * Looks up a reason phrase.
     * This method evaluates the currently set catalog and locale.
     * It also handles a missing catalog.
     *
     * @param code      the status code for which to look up the reason
     *
     * @return  the reason phrase, or {@code null} if there is none
     */
    protected String getReason(final int code) {
        return this.reasonCatalog != null ? this.reasonCatalog.getReason(code,
                this.locale != null ? this.locale : Locale.getDefault()) : null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getStatusLine());
        sb.append(' ');
        sb.append(this.headergroup);
        if (this.entity != null) {
            sb.append(' ');
            sb.append(this.entity);
        }
        return sb.toString();
    }

}
