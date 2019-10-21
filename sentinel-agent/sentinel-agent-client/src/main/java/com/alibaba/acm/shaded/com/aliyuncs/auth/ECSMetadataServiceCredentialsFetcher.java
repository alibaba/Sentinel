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

package com.alibaba.acm.shaded.com.aliyuncs.auth;

/**
 * Created by haowei.yao on 2017/9/12.
 */

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpRequest;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpResponse;
import com.alibaba.acm.shaded.com.aliyuncs.http.MethodType;
import com.alibaba.acm.shaded.com.aliyuncs.http.clients.CompatibleUrlConnClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ECSMetadataServiceCredentialsFetcher {
    private static final String URL_IN_ECS_METADATA =
        "/latest/meta-data/ram/security-credentials/";
    private static final int DEFAULT_TIMEOUT_IN_MILLISECONDS = 5000;
    private URL credentialUrl;
    private String roleName;
    private String metadataServiceHost = "100.100.100.200";
    private int connectionTimeoutInMilliseconds;
    private static final String ECS_METADAT_FETCH_ERROR_MSG =
        "Failed to get RAM session credentials from ECS metadata service.";
    private static final int DEFAULT_ECS_SESSION_TOKEN_DURATION_SECONDS = 3600 * 6;

    public ECSMetadataServiceCredentialsFetcher() {
        this.connectionTimeoutInMilliseconds = DEFAULT_TIMEOUT_IN_MILLISECONDS;
    }

    public void setRoleName(String roleName) {
        if (null == roleName) {
            throw new NullPointerException("You must specifiy a valid role name.");
        }
        this.roleName = roleName;
        setCredentialUrl();
    }

    private void setCredentialUrl() {
        try {
            this.credentialUrl = new URL("http://" + metadataServiceHost + URL_IN_ECS_METADATA + roleName);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    public ECSMetadataServiceCredentialsFetcher withECSMetadataServiceHost(String host) {
        System.err.println("withECSMetadataServiceHost() method is only for testing, please don't use it");
        this.metadataServiceHost = host;
        setCredentialUrl();
        return this;
    }

    public ECSMetadataServiceCredentialsFetcher withConnectionTimeout(int milliseconds) {
        this.connectionTimeoutInMilliseconds = milliseconds;
        return this;
    }

    public String getMetadata() throws ClientException {
        HttpRequest request = new HttpRequest(credentialUrl.toString());
        request.setMethod(MethodType.GET);
        request.setConnectTimeout(connectionTimeoutInMilliseconds);
        request.setReadTimeout(connectionTimeoutInMilliseconds);
        HttpResponse response;

        try {
            response = CompatibleUrlConnClient.compatibleGetResponse(request);
        } catch (Exception e) {
            throw new ClientException("Failed to connect ECS Metadata Service: " + e.toString());
        }

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new ClientException(ECS_METADAT_FETCH_ERROR_MSG + " HttpCode=" + response.getStatus());
        }

        return new String(response.getHttpContent());
    }

    public InstanceProfileCredentials fetch() throws ClientException {
        String jsonContent = getMetadata();
        JsonObject jsonObject = null;
        jsonObject = new JsonParser().parse(jsonContent).getAsJsonObject();

        if (jsonObject.has("Code") &&
            jsonObject.has("AccessKeyId") &&
            jsonObject.has("AccessKeySecret") &&
            jsonObject.has("SecurityToken") &&
            jsonObject.has("Expiration")) {

        } else {
            throw new ClientException("Invalid json got from ECS Metadata service.");
        }

        if (!"Success".equals(jsonObject.get("Code").getAsString())) {
            throw new ClientException(ECS_METADAT_FETCH_ERROR_MSG);
        }
        return new InstanceProfileCredentials(
            jsonObject.get("AccessKeyId").getAsString(),
            jsonObject.get("AccessKeySecret").getAsString(),
            jsonObject.get("SecurityToken").getAsString(),
            jsonObject.get("Expiration").getAsString(),
            DEFAULT_ECS_SESSION_TOKEN_DURATION_SECONDS
        );
    }

    public InstanceProfileCredentials fetch(int retryTimes) throws ClientException {
        for (int i = 0; i <= retryTimes; i++) {
            try {
                return fetch();
            } catch (ClientException e) {
                if (i == retryTimes) {
                    throw e;
                }
            }
        }
        throw new ClientException("Failed to connect ECS Metadata Service: Max retry times exceeded.");
    }
}