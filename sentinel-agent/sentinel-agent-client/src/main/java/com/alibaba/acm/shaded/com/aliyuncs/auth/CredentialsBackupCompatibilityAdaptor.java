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

import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;

@Deprecated
public class CredentialsBackupCompatibilityAdaptor extends Credential {

    private final AlibabaCloudCredentialsProvider provider;
    public CredentialsBackupCompatibilityAdaptor(AlibabaCloudCredentialsProvider provider) {
        this.provider = provider;
    }

    private AlibabaCloudCredentials getCredentials() {
        try {
            AlibabaCloudCredentials credentials = this.provider.getCredentials();
            return credentials;
        } catch (ClientException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public String getAccessKeyId() {
        return getCredentials().getAccessKeyId();
    }

    @Override
    public String getAccessSecret() {
        return getCredentials().getAccessKeySecret();
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public String getSecurityToken() {
        AlibabaCloudCredentials credentials = getCredentials();
        if (credentials instanceof BasicSessionCredentials) {
            return ((BasicSessionCredentials)credentials).getSessionToken();
        } else {
            return null;
        }
    }

}
