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
import java.util.Map.Entry;

import com.alibaba.acm.shaded.com.aliyuncs.auth.AcsURLEncoder;
import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Credential;
import com.alibaba.acm.shaded.com.aliyuncs.auth.ISignatureComposer;
import com.alibaba.acm.shaded.com.aliyuncs.auth.LegacyCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Signer;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpRequest;
import com.alibaba.acm.shaded.com.aliyuncs.http.ProtocolType;
import com.alibaba.acm.shaded.com.aliyuncs.regions.ProductDomain;

@SuppressWarnings("deprecation")
public abstract class AcsRequest<T extends AcsResponse> extends HttpRequest {

    private String version = null;
    private String product = null;
    private String actionName = null;
    private String regionId = null;
    private String securityToken = null;
    private FormatType acceptFormat = null;
    protected ISignatureComposer composer = null;
    private ProtocolType protocol = null;
    private final Map<String, String> queryParameters = new HashMap<String, String>();
    private final Map<String, String> domainParameters = new HashMap<String, String>();
    private final Map<String, String> bodyParameters = new HashMap<String, String>();

    private String locationProduct;
    private String endpointType;
    private ProductDomain productDomain = null;

    public AcsRequest(String product) {
        super(null);
        this.headers.put("x-sdk-client", "Java/2.0.0");
        this.headers.put("x-sdk-invoke-type", "normal");
        this.product = product;
    }

    public String getLocationProduct() {
        return locationProduct;
    }

    public void setLocationProduct(String locationProduct) {
        this.locationProduct = locationProduct;
        putQueryParameter("ServiceCode", locationProduct);
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
        putQueryParameter("Type", endpointType);
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getProduct() {
        return product;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getQueryParameters() {
        return Collections.unmodifiableMap(queryParameters);
    }

    public <K> void putQueryParameter(String name, K value) {
        setParameter(this.queryParameters, name, value);
    }

    protected void putQueryParameter(String name, String value) {
        setParameter(this.queryParameters, name, value);
    }

    public Map<String, String> getDomainParameters() {
        return Collections.unmodifiableMap(domainParameters);
    }

    protected void putDomainParameter(String name, Object value) {
        setParameter(this.domainParameters, name, value);
    }

    public Map<String, String> getBodyParameters() {
        return Collections.unmodifiableMap(bodyParameters);
    }

    protected void putDomainParameter(String name, String value) {
        setParameter(this.domainParameters, name, value);
    }

    protected void putBodyParameter(String name, Object value) {
        setParameter(this.bodyParameters, name, value);
    }

    protected void setParameter(Map<String, String> map, String name, Object value) {
        if (null == map || null == name || null == value) {
            return;
        }
        map.put(name, String.valueOf(value));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public FormatType getAcceptFormat() {
        return acceptFormat;
    }

    public void setAcceptFormat(FormatType acceptFormat) {
        this.acceptFormat = acceptFormat;
        this.putHeaderParameter("Accept",
            FormatType.mapFormatToAccept(acceptFormat));
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        putQueryParameter("SecurityToken", securityToken);
    }

    public static String concatQueryString(Map<String, String> parameters)
        throws UnsupportedEncodingException {
        if (null == parameters) { return null; }

        StringBuilder urlBuilder = new StringBuilder("");
        for (Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            urlBuilder.append(AcsURLEncoder.encode(key));
            if (val != null) {
                urlBuilder.append("=").append(AcsURLEncoder.encode(val));
            }
            urlBuilder.append("&");
        }

        int strIndex = urlBuilder.length();
        if (parameters.size() > 0) { urlBuilder.deleteCharAt(strIndex - 1); }

        return urlBuilder.toString();
    }

    public HttpRequest signRequest(Signer signer, Credential credential,
                                   FormatType format, ProductDomain domain)
        throws InvalidKeyException, IllegalStateException,
        UnsupportedEncodingException, NoSuchAlgorithmException {
        return signRequest(signer, new LegacyCredentials(credential), format, domain);
    }

    public abstract HttpRequest signRequest(Signer signer, AlibabaCloudCredentials credentials,
                                            FormatType format, ProductDomain domain)
        throws InvalidKeyException, IllegalStateException,
        UnsupportedEncodingException, NoSuchAlgorithmException;

    public abstract String composeUrl(String endpoint, Map<String, String> queries)
        throws UnsupportedEncodingException;

    public abstract Class<T> getResponseClass();

    public ProductDomain getProductDomain() {
        return productDomain;
    }

    public void setProductDomain(ProductDomain productDomain) {
        this.productDomain = productDomain;
    }
    
    public void setEndpoint(String endpoint) {
        ProductDomain productDomain = new ProductDomain(product, endpoint);
        setProductDomain(productDomain);
    }

}
