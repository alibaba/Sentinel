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
import java.util.HashMap;
import java.util.Map;

import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.OssSignatureComposer;
import com.alibaba.acm.shaded.com.aliyuncs.auth.RoaSignatureComposer;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Signer;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpRequest;
import com.alibaba.acm.shaded.com.aliyuncs.regions.ProductDomain;

public abstract class OssAcsRequest<T extends AcsResponse>
    extends RoaAcsRequest<T> {

    private String bucketName = null;

    public OssAcsRequest(String product, String actionName) {
        super(product);
        this.setActionName(actionName);
        this.composer = OssSignatureComposer.getComposer();
    }

    @Override
    public void setVersion(String version) {

    }

    @Override
    public String composeUrl(String endpoint,
                             Map<String, String> queries)
        throws UnsupportedEncodingException {
        Map<String, String> mapQueries =
            queries == null ? this.getQueryParameters() : queries;

        StringBuilder urlBuilder = new StringBuilder("");
        urlBuilder.append(this.getProtocol().toString());
        urlBuilder.append("://");
        if (null != this.bucketName) { urlBuilder.append(this.bucketName).append("."); }
        urlBuilder.append(endpoint);
        if (null != this.uriPattern) {
            urlBuilder.append(
                RoaSignatureComposer.replaceOccupiedParameters
                    (uriPattern, this.getPathParameters()));
        }
        if (-1 == urlBuilder.indexOf("?")) { urlBuilder.append("?"); }
        String query = concatQueryString(mapQueries);

        return urlBuilder.append(query).toString();
    }

    @Override
    public HttpRequest signRequest(Signer signer, AlibabaCloudCredentials credentials,
                                   FormatType format, ProductDomain domain)
        throws InvalidKeyException, IllegalStateException,
        UnsupportedEncodingException, NoSuchAlgorithmException {
        Map<String, String> imutableMap = new HashMap<String, String>(this.getHeaders());
        if (null != signer && null != credentials) {
            String accessKeyId = credentials.getAccessKeyId();
            imutableMap = this.composer.refreshSignParameters
                (this.getHeaders(), signer, accessKeyId, format);
            String uri = this.uriPattern;
            if (null != this.bucketName) {
                uri = "/" + bucketName + uri;
            }
            String strToSign = this.composer.composeStringToSign(this.getMethod(), uri, signer,
                this.getQueryParameters(), imutableMap, this.getPathParameters());
            String signature = signer.signString(strToSign, credentials);
            imutableMap.put("Authorization", "OSS " + accessKeyId + ":" + signature);
        }
        HttpRequest request = new HttpRequest(
            this.composeUrl(domain.getDomianName(), this.getQueryParameters()), imutableMap);
        request.setMethod(this.getMethod());
        request.setHttpContent(this.getHttpContent(), this.getEncoding(), this.getHttpContentType());

        return request;
    }

    @Override
    public abstract Class<T> getResponseClass();
}
