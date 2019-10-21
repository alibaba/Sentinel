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
 * Created by haowei.yao on 2017/9/27.
 */

import com.alibaba.acm.shaded.com.aliyuncs.DefaultAcsClient;
import com.alibaba.acm.shaded.com.aliyuncs.IAcsClient;
import com.alibaba.acm.shaded.com.aliyuncs.auth.sts.GetSessionAccessKeyRequest;
import com.alibaba.acm.shaded.com.aliyuncs.auth.sts.GenerateSessionAccessKeyResponse;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ServerException;
import com.alibaba.acm.shaded.com.aliyuncs.http.ProtocolType;
import com.alibaba.acm.shaded.com.aliyuncs.profile.IClientProfile;

public class STSGetSessionAccessKeyCredentialsProvider implements AlibabaCloudCredentialsProvider{

    public static final int DEFAULT_DURATION_SECONDS = 3600;
    private final IAcsClient stsClient;
    private final KeyPairCredentials keyPairCredentials;
    private long sessionDurationSeconds = DEFAULT_DURATION_SECONDS;
    private BasicSessionCredentials sessionCredentials = null;

    public STSGetSessionAccessKeyCredentialsProvider(KeyPairCredentials keyPairCredentials,
                                                     IClientProfile profile) {
        this.keyPairCredentials = keyPairCredentials;
        this.stsClient = new DefaultAcsClient(profile, keyPairCredentials);
    }


    public STSGetSessionAccessKeyCredentialsProvider withDurationSeconds(long seconds) {
        this.sessionDurationSeconds = seconds;
        return this;
    }

    @Override
    public AlibabaCloudCredentials getCredentials() throws ClientException, ServerException {
        if (sessionCredentials == null || sessionCredentials.willSoonExpire()) {
            sessionCredentials = getNewSessionCredentials();
        }
        return sessionCredentials;
    }

    private BasicSessionCredentials getNewSessionCredentials() throws ClientException, ServerException {
        GetSessionAccessKeyRequest request = new GetSessionAccessKeyRequest();
        request.setPublicKeyId(keyPairCredentials.getAccessKeyId());
        request.setDurationSeconds((int)sessionDurationSeconds);
        request.setProtocol(ProtocolType.HTTPS);

        GenerateSessionAccessKeyResponse response = this.stsClient.getAcsResponse(request);

        return new BasicSessionCredentials(
            response.getSessionAccessKey().getSessionAccessKeyId(),
            response.getSessionAccessKey().getSessionAccessKeySecert(),
            null,
            sessionDurationSeconds
        );
    }
}
