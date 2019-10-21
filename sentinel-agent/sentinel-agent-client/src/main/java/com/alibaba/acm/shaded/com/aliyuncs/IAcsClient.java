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

import java.util.List;

import com.alibaba.acm.shaded.com.aliyuncs.auth.Credential;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Signer;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ServerException;
import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.HttpResponse;
import com.alibaba.acm.shaded.com.aliyuncs.profile.IClientProfile;

@SuppressWarnings("deprecation")
public interface IAcsClient {

    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request)
        throws ClientException, ServerException;

    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request,
                                                         boolean autoRetry, int maxRetryCounts)
        throws ClientException, ServerException;

    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request, IClientProfile profile)
        throws ClientException, ServerException;

    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request, String regionId,
                                                         Credential credential) throws ClientException, ServerException;

    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request)
        throws ServerException, ClientException;

    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request,
                                                    boolean autoRetry, int maxRetryCounts)
        throws ServerException, ClientException;

    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request,
                                                    IClientProfile profile) throws ServerException, ClientException;

    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request,
                                                    String regionId, Credential credential)
        throws ServerException, ClientException;

    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request,
                                                    String regionId)
        throws ServerException, ClientException;

    public CommonResponse getCommonResponse(CommonRequest request) throws ServerException, ClientException;

    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request, boolean autoRetry,
                                                         int maxRetryCounts, IClientProfile profile)
        throws ClientException, ServerException;

    public void restoreSSLCertificate() ;

    public void ignoreSSLCertificate() ;

    public abstract void shutdown();

}