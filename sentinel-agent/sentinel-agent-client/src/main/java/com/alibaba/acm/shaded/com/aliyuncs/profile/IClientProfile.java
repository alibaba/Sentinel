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
import com.alibaba.acm.shaded.com.aliyuncs.auth.ISigner;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpClientConfig;

@SuppressWarnings("deprecation")
public interface IClientProfile {

    @Deprecated
    public ISigner getSigner();

    public String getRegionId();

    public FormatType getFormat();

    @Deprecated
    public Credential getCredential();

    /**
     * This method exists because ClientProfile holds too much modules like endpoint management
     * @param credentialsProvider
     */
    public void setCredentialsProvider(AlibabaCloudCredentialsProvider credentialsProvider);

    /**
     *  use HttpClientConfig.setCertPath instead
     * @param certPath
     */
    @Deprecated
    public void setCertPath(String certPath);

    /**
     *  use HttpClientConfig.getCertPath instead
     */
    @Deprecated
    public String getCertPath();

    /**
     * http client configs
     */
    public HttpClientConfig getHttpClientConfig();

    public void setHttpClientConfig(HttpClientConfig httpClientConfig);
}
