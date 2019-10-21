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

import com.alibaba.acm.shaded.com.aliyuncs.profile.IClientProfile;

/**
 * A container of legacy credential classes or a static credentials object
 * For backward compatibility
 */
public class StaticCredentialsProvider implements AlibabaCloudCredentialsProvider {

    private AlibabaCloudCredentials credentials = null;
    private IClientProfile clientProfile = null;

    public StaticCredentialsProvider(AlibabaCloudCredentials credentials) {
        this.credentials = credentials;
    }

    @SuppressWarnings("deprecation")
    public StaticCredentialsProvider(IClientProfile clientProfile) {
        this.clientProfile = clientProfile;
        Credential legacyCredential = this.clientProfile.getCredential();
        if (null != legacyCredential.getSecurityToken()) {
            this.credentials = new BasicSessionCredentials(
                legacyCredential.getAccessKeyId(),
                legacyCredential.getAccessSecret(),
                legacyCredential.getSecurityToken()
            );
        } else {
            this.credentials = new LegacyCredentials(legacyCredential);
        }
    }

    @Override
    public AlibabaCloudCredentials getCredentials()
    {
        return this.credentials;
    }
}
