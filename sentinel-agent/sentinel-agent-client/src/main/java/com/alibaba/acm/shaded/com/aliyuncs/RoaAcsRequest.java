/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.acm.shaded.com.aliyuncs;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.BasicSessionCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.BearerTokenCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.RoaSignatureComposer;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Signer;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpRequest;
import com.alibaba.acm.shaded.com.aliyuncs.regions.ProductDomain;
import com.alibaba.acm.shaded.com.aliyuncs.utils.ParameterHelper;

public abstract class RoaAcsRequest<T extends AcsResponse> extends AcsRequest<T> {

    protected String uriPattern = null;
    private Map<String, String> pathParameters = new HashMap<String, String>();

    public RoaAcsRequest(String product) {
        super(product);
        initialize();
    }

    public RoaAcsRequest(String product, String version) {
        super(product);
        this.setVersion(version);
        initialize();
    }

    public RoaAcsRequest(String product, String version, String action) {
        super(product);
        this.setVersion(version);
        this.setActionName(action);
        initialize();
    }

    public RoaAcsRequest(String product, String version, String action, String locationProduct) {
        super(product);
        this.setVersion(version);
        this.setActionName(action);
        this.setLocationProduct(locationProduct);
        initialize();
    }

    public RoaAcsRequest(String product, String version, String action, String locationProduct, String endpointType) {
        super(product);
        this.setVersion(version);
        this.setActionName(action);
        this.setLocationProduct(locationProduct);
        this.setEndpointType(endpointType);
        initialize();
    }

    private void initialize() {
        this.composer = RoaSignatureComposer.getComposer();
        this.setHttpContent(new byte[0], "utf-8", FormatType.RAW);
    }

    @Override
    public void setVersion(String version) {
        super.setVersion(version);
        this.putHeaderParameter("x-acs-version", version);
    }

    @Override
    public void setSecurityToken(String securityToken) {
        super.setSecurityToken(securityToken);
        this.putHeaderParameter("x-acs-security-token", securityToken);
    }

    public Map<String, String> getPathParameters() {
        return Collections.unmodifiableMap(pathParameters);
    }

    protected void putPathParameter(String name, Object value) {
        setParameter(this.pathParameters, name, value);
    }

    protected void putPathParameter(String name, String value) {
        setParameter(this.pathParameters, name, value);
    }

    @Override
    public String composeUrl(String endpoint, Map<String, String> queries) throws UnsupportedEncodingException {

        Map<String, String> mapQueries = (queries == null) ? this.getQueryParameters() : queries;
        StringBuilder urlBuilder = new StringBuilder("");
        urlBuilder.append(this.getProtocol().toString());
        urlBuilder.append("://").append(endpoint);
        if (null != this.uriPattern) {
            urlBuilder.append(RoaSignatureComposer.replaceOccupiedParameters(uriPattern, this.getPathParameters()));
        }
        if (-1 == urlBuilder.indexOf("?")) {
            urlBuilder.append("?");
        } else if (!urlBuilder.toString().endsWith("?")) {
            urlBuilder.append("&");
        }
        String query = concatQueryString(mapQueries);
        String url = urlBuilder.append(query).toString();
        if (url.endsWith("?") || url.endsWith("&")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    @Override
    public HttpRequest signRequest(Signer signer, AlibabaCloudCredentials credentials,
                                   FormatType format, ProductDomain domain)
        throws InvalidKeyException, IllegalStateException, UnsupportedEncodingException, NoSuchAlgorithmException {

        Map<String, String> formParams = this.getBodyParameters();
        if (formParams != null && !formParams.isEmpty()) {
            byte[] data = ParameterHelper.getFormData(formParams);
            this.setHttpContent(data, "UTF-8", FormatType.FORM);
        }

        Map<String, String> imutableMap = new HashMap<String, String>(this.getHeaders());
        if (null != signer && null != credentials) {
            String accessKeyId = credentials.getAccessKeyId();
            imutableMap = this.composer.refreshSignParameters(this.getHeaders(), signer, accessKeyId, format);
            if (credentials instanceof BasicSessionCredentials) {
                String sessionToken = ((BasicSessionCredentials)credentials).getSessionToken();
                if (null != sessionToken) {
                    imutableMap.put("x-acs-security-token", sessionToken);
                }
            }
            if (credentials instanceof BearerTokenCredentials) {
                String bearerToken = ((BearerTokenCredentials)credentials).getBearerToken();
                if (null != ((BearerTokenCredentials)credentials).getBearerToken()) {
                    imutableMap.put("x-acs-bearer-token", bearerToken);
                }
            }
            String strToSign = this.composer.composeStringToSign(this.getMethod(), this.getUriPattern(), signer,
                this.getQueryParameters(), imutableMap, this.getPathParameters());
            String signature = signer.signString(strToSign, credentials);
            imutableMap.put("Authorization", "acs " + accessKeyId + ":" + signature);
        }
        this.setUrl(this.composeUrl(domain.getDomianName(), this.getQueryParameters()));
        this.headers = imutableMap;
        return this;
    }
}
