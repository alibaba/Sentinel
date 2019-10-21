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
package com.alibaba.acm.shaded.com.aliyuncs.profile;

import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentialsProvider;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Credential;
import com.alibaba.acm.shaded.com.aliyuncs.auth.CredentialsBackupCompatibilityAdaptor;
import com.alibaba.acm.shaded.com.aliyuncs.auth.ICredentialProvider;
import com.alibaba.acm.shaded.com.aliyuncs.auth.ISigner;
import com.alibaba.acm.shaded.com.aliyuncs.endpoint.UserCustomizedEndpointResolver;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpClientConfig;

@SuppressWarnings("deprecation")
public class DefaultProfile implements IClientProfile {

    private static DefaultProfile profile = null;
    public static UserCustomizedEndpointResolver userCustomizedEndpointResolver = new UserCustomizedEndpointResolver();
    private String regionId = null;
    private FormatType acceptFormat = null;
    private ICredentialProvider icredential = null;
    private Credential credential;
    private String certPath;
    private HttpClientConfig httpClientConfig = HttpClientConfig.getDefault();

    private DefaultProfile() {
    }

    private DefaultProfile(String regionId) {
        this.regionId = regionId;
    }

    private DefaultProfile(String regionId, Credential creden) {
        this.credential = creden;
        this.regionId = regionId;
    }


    private DefaultProfile(ICredentialProvider icredential) {
        this.icredential = icredential;
    }

    private DefaultProfile(String region, ICredentialProvider icredential) {
        this.regionId = region;
        this.icredential = icredential;
    }

    private DefaultProfile(ICredentialProvider icredential, String region, FormatType format) {
        this.regionId = region;
        this.acceptFormat = format;
        this.icredential = icredential;
    }

    @Override
    public synchronized String getRegionId() {
        return regionId;
    }

    @Override
    public synchronized FormatType getFormat() {
        return acceptFormat;
    }

    @Override
    public synchronized Credential getCredential() {
        if (null == credential && null != icredential) { credential = icredential.fresh(); }
        return credential;
    }

    @Override
    @Deprecated
    public ISigner getSigner() {
        return null;
    }

    public synchronized static DefaultProfile getProfile() {
        if (null == profile) { profile = new DefaultProfile(); }

        return profile;
    }

    public synchronized static DefaultProfile getProfile(String regionId, ICredentialProvider icredential) {
        profile = new DefaultProfile(regionId, icredential);
        return profile;
    }

    public synchronized static DefaultProfile getProfile(String regionId, String accessKeyId, String secret) {
        Credential creden = new Credential(accessKeyId, secret);
        profile = new DefaultProfile(regionId, creden);
        return profile;
    }

    public synchronized static DefaultProfile getProfile(String regionId, String accessKeyId, String secret,
                                                         String stsToken) {
        Credential creden = new Credential(accessKeyId, secret, stsToken);
        profile = new DefaultProfile(regionId, creden);
        return profile;
    }

    public synchronized static DefaultProfile getProfile(String regionId) {
        return new DefaultProfile(regionId);
    }

    @Deprecated
    public synchronized static void addEndpoint(String endpointName, String regionId, String product, String domain)
        throws ClientException {
        addEndpoint(endpointName, regionId, product, domain, true);
    }

    @Deprecated
    public synchronized static void addEndpoint(String endpointName, String regionId, String product, String domain,
                                                boolean isNeverExpire) {
        // endpointName, isNeverExpire take no effect
        addEndpoint(regionId, product, domain);
    }

    public synchronized static void addEndpoint(String regionId, String product, String endpoint) {
        userCustomizedEndpointResolver.putEndpointEntry(regionId, product, endpoint);
    }

    @Override
    public void setCredentialsProvider(AlibabaCloudCredentialsProvider credentialsProvider) {
        if (credential != null) {
            return;
        }
        credential = new CredentialsBackupCompatibilityAdaptor(credentialsProvider);
    }

    @Override
    public String getCertPath() {
        return certPath;
    }

    @Override
    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    @Override
    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    @Override
    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }
}
