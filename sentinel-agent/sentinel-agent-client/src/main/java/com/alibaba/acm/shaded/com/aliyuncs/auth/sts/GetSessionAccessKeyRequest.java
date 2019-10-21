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
package com.alibaba.acm.shaded.com.aliyuncs.auth.sts;

import com.alibaba.acm.shaded.com.aliyuncs.RpcAcsRequest;

/**
 * Created by zhangw on 2017/8/10.
 */
public class GetSessionAccessKeyRequest extends RpcAcsRequest<GenerateSessionAccessKeyResponse> {
    
    private static String version = "2015-04-01";

    private static String product = "Sts";
    
    private static String action = "GenerateSessionAccessKey";

    public GetSessionAccessKeyRequest() {
        super(product, version, action);       
    }

    private int durationSeconds = 3600;

    private String publicKeyId;

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
        putQueryParameter("DurationSeconds", durationSeconds);
    }

    public String getPublicKeyId() {
        return publicKeyId;
    }

    public void setPublicKeyId(String publicKeyId) {
        this.publicKeyId = publicKeyId;
        putQueryParameter("PublicKeyId", publicKeyId);
    }

    @Override
    public Class<GenerateSessionAccessKeyResponse> getResponseClass() {
        return GenerateSessionAccessKeyResponse.class;
    }

    public static void setStsVersion(String version) {
        GetSessionAccessKeyRequest.version = version;
    }

    public static void setStsProduct(String product) {
        GetSessionAccessKeyRequest.product = product;
    }

    public static void setStsAction(String action) {
        GetSessionAccessKeyRequest.action = action;
    }
}
