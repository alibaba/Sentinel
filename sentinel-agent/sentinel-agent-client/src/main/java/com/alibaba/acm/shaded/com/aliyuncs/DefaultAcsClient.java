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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.annotation.XmlRootElement;

import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.AlibabaCloudCredentialsProvider;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Credential;
import com.alibaba.acm.shaded.com.aliyuncs.auth.LegacyCredentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.Signer;
import com.alibaba.acm.shaded.com.aliyuncs.auth.StaticCredentialsProvider;
import com.alibaba.acm.shaded.com.aliyuncs.endpoint.*;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ErrorCodeConstant;
import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ServerException;
import com.alibaba.acm.shaded.com.aliyuncs.http.*;
import com.alibaba.acm.shaded.com.aliyuncs.profile.DefaultProfile;
import com.alibaba.acm.shaded.com.aliyuncs.profile.IClientProfile;
import com.alibaba.acm.shaded.com.aliyuncs.reader.Reader;
import com.alibaba.acm.shaded.com.aliyuncs.reader.ReaderFactory;
import com.alibaba.acm.shaded.com.aliyuncs.regions.ProductDomain;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;
import com.alibaba.acm.shaded.com.aliyuncs.unmarshaller.UnmarshallerFactory;
import com.alibaba.acm.shaded.com.aliyuncs.utils.IOUtils;

@SuppressWarnings("deprecation")
public class DefaultAcsClient implements IAcsClient {
    private int maxRetryNumber = 3;
    private boolean autoRetry = true;
    private IClientProfile clientProfile = null;
    private AlibabaCloudCredentialsProvider credentialsProvider;
    private IHttpClient httpClient;
    private EndpointResolver endpointResolver;

    private SSLSocketFactory sslSocketFactory = null;

    @Deprecated
    public DefaultAcsClient() {
        this.clientProfile = DefaultProfile.getProfile();
        this.httpClient = HttpClientFactory.buildClient(this.clientProfile);
    }

    public DefaultAcsClient(IClientProfile profile) {
        this(profile, new StaticCredentialsProvider(profile));
    }

    public DefaultAcsClient(IClientProfile profile, AlibabaCloudCredentials credentials) {
        this(profile, new StaticCredentialsProvider(credentials));
    }

    public DefaultAcsClient(IClientProfile profile, AlibabaCloudCredentialsProvider credentialsProvider) {
        this.clientProfile = profile;
        this.credentialsProvider = credentialsProvider;
        this.clientProfile.setCredentialsProvider(this.credentialsProvider);
        this.httpClient = HttpClientFactory.buildClient(this.clientProfile);
        this.endpointResolver = initEndpointResolver();
    }

    private EndpointResolver initEndpointResolver() {
        List<EndpointResolverBase> resolverChain = new ArrayList<EndpointResolverBase>();

        // The order is very IMPORTANT!
        resolverChain.add(DefaultProfile.userCustomizedEndpointResolver);
        resolverChain.add(new LocalConfigRegionalEndpointResolver());
        resolverChain.add(new LocalConfigGlobalEndpointResolver());
        resolverChain.add(new LocationServiceEndpointResolver(this));

        EndpointResolver endpointResolver = new ChainedEndpointResolver(resolverChain);
        return endpointResolver;
    }

    @Override
    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request)
        throws ClientException, ServerException {
        return this.doAction(request, autoRetry, maxRetryNumber, this.clientProfile);
    }

    @Override
    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request,
                                                         boolean autoRetry, int maxRetryCounts)
        throws ClientException, ServerException {
        return this.doAction(request, autoRetry, maxRetryCounts, this.clientProfile);
    }

    @Override
    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request, IClientProfile profile)
        throws ClientException, ServerException {
        return this.doAction(request, this.autoRetry, this.maxRetryNumber, profile);
    }

    @Override
    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request, String regionId, Credential credential)
        throws ClientException, ServerException {
        boolean retry = this.autoRetry;
        int retryNumber = this.maxRetryNumber;
        Signer signer = Signer.getSigner(new LegacyCredentials(credential));
        FormatType format = null;
        if (null == request.getRegionId()) {
            request.setRegionId(regionId);
        }

        return this.doAction(request, retry, retryNumber, request.getRegionId(), credential, signer, format);
    }

    @Override
    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request)
        throws ServerException, ClientException {
        HttpResponse baseResponse = this.doAction(request);
        return parseAcsResponse(request.getResponseClass(), baseResponse);
    }

    @Override
    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request,
                                                    boolean autoRetry, int maxRetryCounts)
        throws ServerException, ClientException {
        HttpResponse baseResponse = this.doAction(request, autoRetry, maxRetryCounts);
        return parseAcsResponse(request.getResponseClass(), baseResponse);
    }

    @Override
    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request, IClientProfile profile)
        throws ServerException, ClientException {
        HttpResponse baseResponse = this.doAction(request, profile);
        return parseAcsResponse(request.getResponseClass(), baseResponse);
    }

    @Override
    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request, String regionId, Credential credential)
        throws ServerException, ClientException {
        HttpResponse baseResponse = this.doAction(request, regionId, credential);
        return parseAcsResponse(request.getResponseClass(), baseResponse);
    }

    @Override
    public <T extends AcsResponse> T getAcsResponse(AcsRequest<T> request, String regionId)
        throws ServerException, ClientException {
        if (null == request.getRegionId()) {
            request.setRegionId(regionId);
        }
        HttpResponse baseResponse = this.doAction(request);
        return parseAcsResponse(request.getResponseClass(), baseResponse);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CommonResponse getCommonResponse(CommonRequest request)
        throws ServerException, ClientException {
        HttpResponse baseResponse = this.doAction(request.buildRequest());
        if (baseResponse.isSuccess()) {
            String stringContent = baseResponse.getHttpContentString();
            CommonResponse response = new CommonResponse();
            response.setData(stringContent);
            response.setHttpStatus(baseResponse.getStatus());
            response.setHttpResponse(baseResponse);
            return response;
        } else {
            FormatType format = baseResponse.getHttpContentType();
            AcsError error = readError(baseResponse, format);
            if (500 <= baseResponse.getStatus()) {
                throw new ServerException(error.getErrorCode(), error.getErrorMessage(), error.getRequestId());
            } else {
                throw new ClientException(error.getErrorCode(), error.getErrorMessage(), error.getRequestId());
            }
        }
    }

    @Override
    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request, boolean autoRetry,
                                                         int maxRetryCounts, IClientProfile profile)
        throws ClientException, ServerException {
        if (null == profile) {
            throw new ClientException("SDK.InvalidProfile", "No active profile found.");
        }
        boolean retry = autoRetry;
        int retryNumber = maxRetryCounts;
        String region = profile.getRegionId();
        if (null == request.getRegionId()) {
            request.setRegionId(region);
        }

        AlibabaCloudCredentials credentials = this.credentialsProvider.getCredentials();
        Signer signer = Signer.getSigner(credentials);
        FormatType format = profile.getFormat();

        return this.doAction(request, retry, retryNumber, request.getRegionId(), credentials, signer, format);
    }

    private <T extends AcsResponse> T parseAcsResponse(Class<T> clasz, HttpResponse baseResponse)
        throws ServerException, ClientException {

        FormatType format = baseResponse.getHttpContentType();

        if (baseResponse.isSuccess()) {
            return readResponse(clasz, baseResponse, format);
        } else {
            AcsError error = readError(baseResponse, format);
            if (500 <= baseResponse.getStatus()) {
                throw new ServerException(error.getErrorCode(), error.getErrorMessage(), error.getRequestId());
            } else {
                throw new ClientException(error.getErrorCode(), error.getErrorMessage(), error.getRequestId());
            }
        }
    }

    @Deprecated
    public <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request,
                                                         boolean autoRetry, int maxRetryNumber,
                                                         String regionId, Credential credential,
                                                         Signer signer, FormatType format)
        throws ClientException, ServerException {
        return doAction(
            request, autoRetry, maxRetryNumber, regionId, new LegacyCredentials(credential),
            signer, format
        );
    }

    private <T extends AcsResponse> HttpResponse doAction(AcsRequest<T> request,
                                                          boolean autoRetry, int maxRetryNumber,
                                                          String regionId,
                                                          AlibabaCloudCredentials credentials,
                                                          Signer signer, FormatType format)
        throws ClientException, ServerException {

        try {
            FormatType requestFormatType = request.getAcceptFormat();
            if (null != requestFormatType) {
                format = requestFormatType;
            }
            ProductDomain domain = null;
            if (request.getProductDomain() != null) {
                domain = request.getProductDomain();
            } else {
                ResolveEndpointRequest resolveEndpointRequest = new ResolveEndpointRequest(
                        regionId,
                        request.getProduct(),
                        request.getLocationProduct(),
                        request.getEndpointType()
                );
                String endpoint = endpointResolver.resolve(resolveEndpointRequest);
                domain = new ProductDomain(request.getProduct(), endpoint);

                if (endpoint.endsWith("endpoint-test.exception.com")) {
                    // For endpoint testability, if the endpoint is xxxx.endpoint-test.special.com
                    // throw a client exception with this endpoint
                    throw new ClientException(ErrorCodeConstant.SDK_ENDPOINT_TESTABILITY, endpoint);
                }
            }

            if (request.getProtocol() == null) {
                request.setProtocol(this.clientProfile.getHttpClientConfig().getProtocolType());
            }

            boolean shouldRetry = true;
            for (int retryTimes = 0; shouldRetry; retryTimes++) {
                shouldRetry = autoRetry && retryTimes < maxRetryNumber;
                try {
                    HttpRequest httpRequest = request.signRequest(signer, credentials, format, domain);
                    HttpResponse response;
                    response = this.httpClient.syncInvoke(httpRequest);

                    if ((500 <= response.getStatus() || response.getHttpContent() == null) && shouldRetry) {
                            continue;
                    }
                    return response;
                } catch (SocketTimeoutException exp) {
                    if (shouldRetry) {
                        continue;
                    } else {
                        throw new ClientException("SDK.ServerUnreachable", "SocketTimeoutException has occurred on a socket read or accept.", exp);
                    }
                } catch (IOException exp) {
                    if (shouldRetry) {
                        continue;
                    } else {
                        throw new ClientException("SDK.ServerUnreachable", "Server unreachable: " + exp.toString(), exp);
                    }
                }
            }
        } catch (InvalidKeyException exp) {
            throw new ClientException("SDK.InvalidAccessSecret", "Specified access secret is not valid.", exp);
        } catch (NoSuchAlgorithmException exp) {
            throw new ClientException("SDK.InvalidMD5Algorithm", "MD5 hash is not supported by client side.", exp);
        }

        return null;
    }

    private <T extends AcsResponse> T readResponse(Class<T> clasz, HttpResponse httpResponse, FormatType format)
        throws ClientException {
        // new version response contains "@XmlRootElement" annotation
        if (clasz.isAnnotationPresent(XmlRootElement.class) && !clientProfile.getHttpClientConfig().isCompatibleMode()) {
            com.alibaba.acm.shaded.com.aliyuncs.unmarshaller.Unmarshaller unmarshaller = UnmarshallerFactory.getUnmarshaller(format);
            return unmarshaller.unmarshal(clasz, httpResponse);
        } else {
            Reader reader = ReaderFactory.createInstance(format);
            UnmarshallerContext context = new UnmarshallerContext();
            T response = null;
            String stringContent = httpResponse.getHttpContentString();
            try {
                response = clasz.newInstance();
            } catch (Exception e) {
                throw new ClientException("SDK.InvalidResponseClass", "Unable to allocate " + clasz.getName() + " class");
            }

            String responseEndpoint = clasz.getName().substring(clasz.getName().lastIndexOf(".") + 1);
            if (response.checkShowJsonItemName()) {
                context.setResponseMap(reader.read(stringContent, responseEndpoint));
            } else {
                context.setResponseMap(reader.readForHideArrayItem(stringContent, responseEndpoint));
            }

            context.setHttpResponse(httpResponse);
            response.getInstance(context);
            return response;
        }
    }

    private AcsError readError(HttpResponse httpResponse, FormatType format) throws ClientException {
        try {
            AcsError error = new AcsError();
            String responseEndpoint = "Error";
            Reader reader = ReaderFactory.createInstance(format);
            UnmarshallerContext context = new UnmarshallerContext();
            String stringContent = httpResponse.getHttpContentString();
            context.setResponseMap(reader.read(stringContent, responseEndpoint));
            return error.getInstance(context);
        } catch (Throwable e) {
            String message = httpResponse.getHttpContentString();
            throw new ClientException("SDK.UnknownError", message);
        }

    }

    public boolean isAutoRetry() {
        return autoRetry;
    }

    public void setAutoRetry(boolean autoRetry) {
        this.autoRetry = autoRetry;
    }

    public int getMaxRetryNumber() {
        return maxRetryNumber;
    }

    public void setMaxRetryNumber(int maxRetryNumber) {
        this.maxRetryNumber = maxRetryNumber;
    }

    public void restoreSSLCertificate() {
        this.httpClient.restoreSSLCertificate();
    }

    public void ignoreSSLCertificate() {
        this.httpClient.ignoreSSLCertificate();
    }

    public void setEndpointResolver(EndpointResolver resolver) {
        endpointResolver = resolver;
    }

    @Override
    public void shutdown() {
        IOUtils.closeQuietly(this.httpClient);
    }

}
