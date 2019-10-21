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
import java.util.HashMap;
import java.util.Map;

import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.BasicSessionCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.BearerTokenCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.KeyPairCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.RpcSignatureComposer;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Signer;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpRequest;
import com.alibaba.acm.shaded.com.aliyuncs.http.MethodType;
import com.alibaba.acm.shaded.com.aliyuncs.regions.ProductDomain;
import com.alibaba.acm.shaded.com.aliyuncs.utils.ParameterHelper;

public abstract class RpcAcsRequest<T extends AcsResponse> extends AcsRequest<T> {

    public RpcAcsRequest(String product) {
        super(product);
        initialize();
    }

    public RpcAcsRequest(String product, String version) {
        super(product);
        this.setVersion(version);
        initialize();
    }

    public RpcAcsRequest(String product, String version, String action) {
        super(product);
        this.setVersion(version);
        this.setActionName(action);
        initialize();
    }

    public RpcAcsRequest(String product, String version, String action, String locationProduct) {
        super(product);
        this.setVersion(version);
        this.setActionName(action);
        this.setLocationProduct(locationProduct);
        initialize();
    }

    public RpcAcsRequest(String product, String version, String action, String locationProduct, String endpointType) {
        super(product);
        this.setVersion(version);
        this.setActionName(action);
        this.setLocationProduct(locationProduct);
        this.setEndpointType(endpointType);
        initialize();
    }

    private void initialize() {
        this.setMethod(MethodType.GET);
        this.setAcceptFormat(FormatType.XML);
        this.composer = RpcSignatureComposer.getComposer();
    }

    @Override
    public void setActionName(String actionName) {
        super.setActionName(actionName);
        this.putQueryParameter("Action", actionName);
    }

    @Override
    public void setVersion(String version) {
        super.setVersion(version);
        this.putQueryParameter("Version", version);
    }

    @Override
    public void setSecurityToken(String securityToken) {
        super.setSecurityToken(securityToken);
        this.putQueryParameter("SecurityToken", securityToken);
    }

    @Override
    public void setAcceptFormat(FormatType acceptFormat) {
        super.setAcceptFormat(acceptFormat);
        this.putQueryParameter("Format", acceptFormat.toString());
    }

    @Override
    public String composeUrl(String endpoint, Map<String, String> queries) throws UnsupportedEncodingException {
        Map<String, String> mapQueries = (queries == null) ? this.getQueryParameters() : queries;
        StringBuilder urlBuilder = new StringBuilder("");
        urlBuilder.append(this.getProtocol().toString());
        urlBuilder.append("://").append(endpoint);
        if (-1 == urlBuilder.indexOf("?")) {
            urlBuilder.append("/?");
        }
        String query = concatQueryString(mapQueries);
        return urlBuilder.append(query).toString();
    }

    @Override
    public HttpRequest signRequest(Signer signer, AlibabaCloudCredentials credentials,
                                   FormatType format, ProductDomain domain)
        throws InvalidKeyException, IllegalStateException, UnsupportedEncodingException {

        Map<String, String> imutableMap = new HashMap<String, String>(this.getQueryParameters());
        if (null != signer && null != credentials) {
            String accessKeyId = credentials.getAccessKeyId();
            String accessSecret = credentials.getAccessKeySecret();
            if (credentials instanceof BasicSessionCredentials) {
                String sessionToken = ((BasicSessionCredentials)credentials).getSessionToken();
                if (null != sessionToken) {
                    this.putQueryParameter("SecurityToken", sessionToken);
                }
            }
            if (credentials instanceof BearerTokenCredentials) {
                String bearerToken = ((BearerTokenCredentials)credentials).getBearerToken();
                if (null != ((BearerTokenCredentials)credentials).getBearerToken()) {
                    this.putQueryParameter("BearerToken", bearerToken);
                }
            }
            imutableMap = this.composer.refreshSignParameters(this.getQueryParameters(), signer, accessKeyId, format);
            imutableMap.put("RegionId", getRegionId());
            Map<String, String> paramsToSign = new HashMap<String, String>(imutableMap);
            Map<String, String> formParams = this.getBodyParameters();
            if (formParams != null && !formParams.isEmpty()) {
                byte[] data = ParameterHelper.getFormData(formParams);
                this.setHttpContent(data, "UTF-8", FormatType.FORM);
                paramsToSign.putAll(formParams);
            }
            String strToSign = this.composer.composeStringToSign(
                this.getMethod(), null, signer, paramsToSign, null,null);

            String signature;
            if (credentials instanceof KeyPairCredentials) {
                signature = signer.signString(strToSign, credentials);
            } else {
                signature = signer.signString(strToSign, accessSecret + "&");
            }
            imutableMap.put("Signature", signature);
        }
        setUrl(this.composeUrl(domain.getDomianName(), imutableMap));
        return this;
    }
}
