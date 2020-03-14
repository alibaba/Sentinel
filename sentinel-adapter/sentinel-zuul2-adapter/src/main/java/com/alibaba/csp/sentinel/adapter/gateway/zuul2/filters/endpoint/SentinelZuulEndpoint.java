/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.endpoint;

import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.SentinelZuul2Constants;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.BlockResponse;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.ZuulBlockFallbackProvider;

import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;

/**
 * Default Endpoint for handling exception.
 *
 * @author wavesZh
 */
public class SentinelZuulEndpoint extends HttpSyncEndpoint {

    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        SessionContext context = request.getContext();
        Throwable throwable = context.getError();
        String fallBackRoute = (String) context.get(SentinelZuul2Constants.ZUUL_CTX_SENTINEL_FALLBACK_ROUTE);
        ZuulBlockFallbackProvider zuulBlockFallbackProvider = ZuulBlockFallbackManager
            .getFallbackProvider(fallBackRoute);
        BlockResponse response = zuulBlockFallbackProvider.fallbackResponse(fallBackRoute, throwable);
        HttpResponseMessage resp = new HttpResponseMessageImpl(context, request, response.getCode());
        resp.setBodyAsText(response.toString());
        return resp;
    }
}
