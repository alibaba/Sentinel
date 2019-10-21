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

package org.apache.http.client.methods;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.HeaderGroup;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;

/**
 * Builder for {@link HttpUriRequest} instances.
 * <p>
 * Please note that this class treats parameters differently depending on composition
 * of the request: if the request has a content entity explicitly set with
 * {@link #setEntity(org.apache.http.HttpEntity)} or it is not an entity enclosing method
 * (such as POST or PUT), parameters will be added to the query component of the request URI.
 * Otherwise, parameters will be added as a URL encoded {@link UrlEncodedFormEntity entity}.
 * </p>
 *
 * @since 4.3
 */
public class RequestBuilder {

    private String method;
    private Charset charset;
    private ProtocolVersion version;
    private URI uri;
    private HeaderGroup headergroup;
    private HttpEntity entity;
    private List<NameValuePair> parameters;
    private RequestConfig config;

    RequestBuilder(final String method) {
        super();
        this.charset = Consts.UTF_8;
        this.method = method;
    }

    RequestBuilder(final String method, final URI uri) {
        super();
        this.method = method;
        this.uri = uri;
    }

    RequestBuilder(final String method, final String uri) {
        super();
        this.method = method;
        this.uri = uri != null ? URI.create(uri) : null;
    }

    RequestBuilder() {
        this(null);
    }

    public static RequestBuilder create(final String method) {
        Args.notBlank(method, "HTTP method");
        return new RequestBuilder(method);
    }

    public static RequestBuilder get() {
        return new RequestBuilder(HttpGet.METHOD_NAME);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder get(final URI uri) {
        return new RequestBuilder(HttpGet.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder get(final String uri) {
        return new RequestBuilder(HttpGet.METHOD_NAME, uri);
    }

    public static RequestBuilder head() {
        return new RequestBuilder(HttpHead.METHOD_NAME);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder head(final URI uri) {
        return new RequestBuilder(HttpHead.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder head(final String uri) {
        return new RequestBuilder(HttpHead.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder patch() {
        return new RequestBuilder(HttpPatch.METHOD_NAME);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder patch(final URI uri) {
        return new RequestBuilder(HttpPatch.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder patch(final String uri) {
        return new RequestBuilder(HttpPatch.METHOD_NAME, uri);
    }

    public static RequestBuilder post() {
        return new RequestBuilder(HttpPost.METHOD_NAME);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder post(final URI uri) {
        return new RequestBuilder(HttpPost.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder post(final String uri) {
        return new RequestBuilder(HttpPost.METHOD_NAME, uri);
    }

    public static RequestBuilder put() {
        return new RequestBuilder(HttpPut.METHOD_NAME);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder put(final URI uri) {
        return new RequestBuilder(HttpPut.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder put(final String uri) {
        return new RequestBuilder(HttpPut.METHOD_NAME, uri);
    }

    public static RequestBuilder delete() {
        return new RequestBuilder(HttpDelete.METHOD_NAME);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder delete(final URI uri) {
        return new RequestBuilder(HttpDelete.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder delete(final String uri) {
        return new RequestBuilder(HttpDelete.METHOD_NAME, uri);
    }

    public static RequestBuilder trace() {
        return new RequestBuilder(HttpTrace.METHOD_NAME);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder trace(final URI uri) {
        return new RequestBuilder(HttpTrace.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder trace(final String uri) {
        return new RequestBuilder(HttpTrace.METHOD_NAME, uri);
    }

    public static RequestBuilder options() {
        return new RequestBuilder(HttpOptions.METHOD_NAME);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder options(final URI uri) {
        return new RequestBuilder(HttpOptions.METHOD_NAME, uri);
    }

    /**
     * @since 4.4
     */
    public static RequestBuilder options(final String uri) {
        return new RequestBuilder(HttpOptions.METHOD_NAME, uri);
    }

    public static RequestBuilder copy(final HttpRequest request) {
        Args.notNull(request, "HTTP request");
        return new RequestBuilder().doCopy(request);
    }

    private RequestBuilder doCopy(final HttpRequest request) {
        if (request == null) {
            return this;
        }
        method = request.getRequestLine().getMethod();
        version = request.getRequestLine().getProtocolVersion();

        if (headergroup == null) {
            headergroup = new HeaderGroup();
        }
        headergroup.clear();
        headergroup.setHeaders(request.getAllHeaders());

        parameters = null;
        entity = null;

        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntity originalEntity = ((HttpEntityEnclosingRequest) request).getEntity();
            final ContentType contentType = ContentType.get(originalEntity);
            if (contentType != null &&
                    contentType.getMimeType().equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
                try {
                    final List<NameValuePair> formParams = URLEncodedUtils.parse(originalEntity);
                    if (!formParams.isEmpty()) {
                        parameters = formParams;
                    }
                } catch (final IOException ignore) {
                }
            } else {
                entity = originalEntity;
            }
        }

        final URI originalUri;
        if (request instanceof HttpUriRequest) {
            uri = ((HttpUriRequest) request).getURI();
        } else {
            uri = URI.create(request.getRequestLine().getUri());
        }

        if (request instanceof Configurable) {
            config = ((Configurable) request).getConfig();
        } else {
            config = null;
        }
        return this;
    }

    /**
     * @since 4.4
     */
    public RequestBuilder setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * @since 4.4
     */
    public Charset getCharset() {
        return charset;
    }

    public String getMethod() {
        return method;
    }

    public ProtocolVersion getVersion() {
        return version;
    }

    public RequestBuilder setVersion(final ProtocolVersion version) {
        this.version = version;
        return this;
    }

    public URI getUri() {
        return uri;
    }

    public RequestBuilder setUri(final URI uri) {
        this.uri = uri;
        return this;
    }

    public RequestBuilder setUri(final String uri) {
        this.uri = uri != null ? URI.create(uri) : null;
        return this;
    }

    public Header getFirstHeader(final String name) {
        return headergroup != null ? headergroup.getFirstHeader(name) : null;
    }

    public Header getLastHeader(final String name) {
        return headergroup != null ? headergroup.getLastHeader(name) : null;
    }

    public Header[] getHeaders(final String name) {
        return headergroup != null ? headergroup.getHeaders(name) : null;
    }

    public RequestBuilder addHeader(final Header header) {
        if (headergroup == null) {
            headergroup = new HeaderGroup();
        }
        headergroup.addHeader(header);
        return this;
    }

    public RequestBuilder addHeader(final String name, final String value) {
        if (headergroup == null) {
            headergroup = new HeaderGroup();
        }
        this.headergroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public RequestBuilder removeHeader(final Header header) {
        if (headergroup == null) {
            headergroup = new HeaderGroup();
        }
        headergroup.removeHeader(header);
        return this;
    }

    public RequestBuilder removeHeaders(final String name) {
        if (name == null || headergroup == null) {
            return this;
        }
        for (final HeaderIterator i = headergroup.iterator(); i.hasNext(); ) {
            final Header header = i.nextHeader();
            if (name.equalsIgnoreCase(header.getName())) {
                i.remove();
            }
        }
        return this;
    }

    public RequestBuilder setHeader(final Header header) {
        if (headergroup == null) {
            headergroup = new HeaderGroup();
        }
        this.headergroup.updateHeader(header);
        return this;
    }

    public RequestBuilder setHeader(final String name, final String value) {
        if (headergroup == null) {
            headergroup = new HeaderGroup();
        }
        this.headergroup.updateHeader(new BasicHeader(name, value));
        return this;
    }

    public HttpEntity getEntity() {
        return entity;
    }

    public RequestBuilder setEntity(final HttpEntity entity) {
        this.entity = entity;
        return this;
    }

    public List<NameValuePair> getParameters() {
        return parameters != null ? new ArrayList<NameValuePair>(parameters) :
            new ArrayList<NameValuePair>();
    }

    public RequestBuilder addParameter(final NameValuePair nvp) {
        Args.notNull(nvp, "Name value pair");
        if (parameters == null) {
            parameters = new LinkedList<NameValuePair>();
        }
        parameters.add(nvp);
        return this;
    }

    public RequestBuilder addParameter(final String name, final String value) {
        return addParameter(new BasicNameValuePair(name, value));
    }

    public RequestBuilder addParameters(final NameValuePair... nvps) {
        for (final NameValuePair nvp: nvps) {
            addParameter(nvp);
        }
        return this;
    }

    public RequestConfig getConfig() {
        return config;
    }

    public RequestBuilder setConfig(final RequestConfig config) {
        this.config = config;
        return this;
    }

    public HttpUriRequest build() {
        final HttpRequestBase result;
        URI uriNotNull = this.uri != null ? this.uri : URI.create("/");
        HttpEntity entityCopy = this.entity;
        if (parameters != null && !parameters.isEmpty()) {
            if (entityCopy == null && (HttpPost.METHOD_NAME.equalsIgnoreCase(method)
                    || HttpPut.METHOD_NAME.equalsIgnoreCase(method))) {
                entityCopy = new UrlEncodedFormEntity(parameters, charset != null ? charset : HTTP.DEF_CONTENT_CHARSET);
            } else {
                try {
                    uriNotNull = new URIBuilder(uriNotNull)
                      .setCharset(this.charset)
                      .addParameters(parameters)
                      .build();
                } catch (final URISyntaxException ex) {
                    // should never happen
                }
            }
        }
        if (entityCopy == null) {
            result = new InternalRequest(method);
        } else {
            final InternalEntityEclosingRequest request = new InternalEntityEclosingRequest(method);
            request.setEntity(entityCopy);
            result = request;
        }
        result.setProtocolVersion(this.version);
        result.setURI(uriNotNull);
        if (this.headergroup != null) {
            result.setHeaders(this.headergroup.getAllHeaders());
        }
        result.setConfig(this.config);
        return result;
    }

    static class InternalRequest extends HttpRequestBase {

        private final String method;

        InternalRequest(final String method) {
            super();
            this.method = method;
        }

        @Override
        public String getMethod() {
            return this.method;
        }

    }

    static class InternalEntityEclosingRequest extends HttpEntityEnclosingRequestBase {

        private final String method;

        InternalEntityEclosingRequest(final String method) {
            super();
            this.method = method;
        }

        @Override
        public String getMethod() {
            return this.method;
        }

    }

}
