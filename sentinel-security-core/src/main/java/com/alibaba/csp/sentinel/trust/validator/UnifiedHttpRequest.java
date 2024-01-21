/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.trust.validator;

import java.util.List;
import java.util.Map;

import org.jose4j.jwt.JwtClaims;

/**
 * @author lwj
 * @since 2.0.0
 */
public class UnifiedHttpRequest {

    private final String sourceIp;

    private final String destIp;

    private final String remoteIp;

    private final String host;

    private final int port;

    private final String method;

    private final String path;

    private final Map<String, List<String>> headers;

    private final Map<String, List<String>> params;

    private JwtClaims jwtClaims;

    private String principal;

    private String sni;

    private UnifiedHttpRequest(String sourceIp, String destIp, String remoteIp,
                               String host, int port, String method, String path, Map<String, List<String>> headers,
                               Map<String, List<String>> params, String principal, String sni) {
        this.sourceIp = sourceIp;
        this.destIp = destIp;
        this.remoteIp = remoteIp;
        this.host = host;
        this.port = port;
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.params = params;
        this.principal = principal;
        this.sni = sni;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getDestIp() {
        return destIp;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    public JwtClaims getJwtClaims() {
        return jwtClaims;
    }

    public void setJwtClaims(JwtClaims jwtClaims) {
        this.jwtClaims = jwtClaims;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getSni() {
        return sni;
    }

    public void setSni(String sni) {
        this.sni = sni;
    }

    public static class UnifiedHttpRequestBuilder {

        private String sourceIp;

        private String destIp;

        private String remoteIp;

        private String host;

        private int port;

        private String method;

        private String path;

        private Map<String, List<String>> headers;

        private String principal;

        private Map<String, List<String>> params;

        private String sni;

        public UnifiedHttpRequestBuilder setSourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
            return this;
        }

        public UnifiedHttpRequestBuilder setDestIp(String destIp) {
            this.destIp = destIp;
            return this;
        }

        public UnifiedHttpRequestBuilder setRemoteIp(String remoteIp) {
            this.remoteIp = remoteIp;
            return this;
        }

        public UnifiedHttpRequestBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public UnifiedHttpRequestBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public UnifiedHttpRequestBuilder setMethod(String method) {
            this.method = method;
            return this;
        }

        public UnifiedHttpRequestBuilder setPath(String path) {
            this.path = path;
            return this;
        }

        public UnifiedHttpRequestBuilder setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public UnifiedHttpRequestBuilder setParams(Map<String, List<String>> params) {
            this.params = params;
            return this;
        }

        public UnifiedHttpRequestBuilder setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public UnifiedHttpRequestBuilder setSni(String sni) {
            this.sni = sni;
            return this;
        }

        public UnifiedHttpRequest build() {
            return new UnifiedHttpRequest(sourceIp, destIp, remoteIp, host, port,
                method, path, headers, params, principal, sni);
        }

    }
}
