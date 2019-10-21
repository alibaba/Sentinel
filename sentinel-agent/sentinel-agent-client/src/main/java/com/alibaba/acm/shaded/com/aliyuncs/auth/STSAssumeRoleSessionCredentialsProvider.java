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

import com.alibaba.acm.shaded.com.aliyuncs.DefaultAcsClient;
import com.alibaba.acm.shaded.com.aliyuncs.IAcsClient;
import com.alibaba.acm.shaded.com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.alibaba.acm.shaded.com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ServerException;
import com.alibaba.acm.shaded.com.aliyuncs.profile.IClientProfile;

/**
 * This implementation of AlibabaCloudCredentialsProvider accesses Alibaba Cloud STS service to assume a Role and get
 * back a temporary session for authentication.
 */
public class STSAssumeRoleSessionCredentialsProvider implements AlibabaCloudCredentialsProvider {

    /**
     * Default duration for started sessions.
     */
    public static final int DEFAULT_DURATION_SECONDS = 3600;

    /**
     * The client for starting STS sessions.
     */
    private IAcsClient stsClient;

    /**
     * The arn of the role to be assumed.
     */
    private final String roleArn;

    /**
     * An identifier for the assumed role session.
     */
    private String roleSessionName;

    /**
     * The Duration for assume role sessions.
     */
    private long roleSessionDurationSeconds;

    private BasicSessionCredentials credentials = null;

    /**
     *
     * For test
     * To know how many rounds AssumeRole has been called
     */
    public long assumeRoleRound = 0;


    public STSAssumeRoleSessionCredentialsProvider(AlibabaCloudCredentials longLivedCredentials,
                                                   String roleArn, IClientProfile clientProfile) {
        this(new StaticCredentialsProvider(longLivedCredentials), roleArn, clientProfile);
    }

    public STSAssumeRoleSessionCredentialsProvider withRoleSessionName(String roleSessionName) {
        this.roleSessionName = roleSessionName;
        return this;
    }

    public STSAssumeRoleSessionCredentialsProvider withRoleSessionDurationSeconds(long roleSessionDurationSeconds) {
        if (roleSessionDurationSeconds < 900 || roleSessionDurationSeconds > 3600) {
            throw new IllegalArgumentException(
                "Assume Role session duration should be in the range of 15min - 1Hr");
        }
        this.roleSessionDurationSeconds = roleSessionDurationSeconds;
        return this;
    }

    public STSAssumeRoleSessionCredentialsProvider withSTSClient(IAcsClient client) {
        this.stsClient = client;
        return this;
    }

    public STSAssumeRoleSessionCredentialsProvider(AlibabaCloudCredentialsProvider longLivedCredentialsProvider,
                                                   String roleArn, IClientProfile clientProfile) {
        if (roleArn == null) {
            throw new NullPointerException(
                "You must specify a value for roleArn.");
        }
        this.roleArn = roleArn;
        this.roleSessionName = getNewRoleSessionName();
        this.stsClient = new DefaultAcsClient(clientProfile, longLivedCredentialsProvider);
        this.roleSessionDurationSeconds = DEFAULT_DURATION_SECONDS;
    }

    public static String getNewRoleSessionName() {
        return "aliyun-java-sdk-" + System.currentTimeMillis();
    }

    @Override
    public AlibabaCloudCredentials getCredentials() throws ClientException, ServerException{
        if (credentials == null || credentials.willSoonExpire()) {
            credentials = getNewSessionCredentials();
        }
        return credentials;
    }

    private BasicSessionCredentials getNewSessionCredentials() throws ClientException, ServerException {

        assumeRoleRound += 1;

        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest();
        assumeRoleRequest.setRoleArn(roleArn);
        assumeRoleRequest.setRoleSessionName(roleSessionName);
        assumeRoleRequest.setDurationSeconds(roleSessionDurationSeconds);

        AssumeRoleResponse response = stsClient.getAcsResponse(assumeRoleRequest);
        return new BasicSessionCredentials(
            response.getCredentials().getAccessKeyId(),
            response.getCredentials().getAccessKeySecret(),
            response.getCredentials().getSecurityToken(),
            roleSessionDurationSeconds
        );
    }
}