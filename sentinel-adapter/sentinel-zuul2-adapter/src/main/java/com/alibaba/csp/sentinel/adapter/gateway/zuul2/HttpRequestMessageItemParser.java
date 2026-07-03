/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.zuul2;

import com.alibaba.csp.sentinel.adapter.gateway.common.param.RequestItemParser;
import com.netflix.zuul.message.http.HttpRequestMessage;

/**
 * @author wavesZh
 * @since 1.7.2
 */
public class HttpRequestMessageItemParser implements RequestItemParser<HttpRequestMessage> {

    @Override
    public String getPath(HttpRequestMessage request) {
        return request.getInboundRequest().getPath();
    }

    @Override
    public String getRemoteAddress(HttpRequestMessage request) {
        return request.getOriginalHost();
    }

    @Override
    public String getHeader(HttpRequestMessage request, String key) {
        return String.valueOf(request.getInboundRequest().getHeaders().get(key));
    }

    @Override
    public String getUrlParam(HttpRequestMessage request, String paramName) {
        return String.valueOf(request.getInboundRequest().getQueryParams().get(paramName));
    }

    @Override
    public String getCookieValue(HttpRequestMessage request, String cookieName) {
        return String.valueOf(request.getInboundRequest().parseCookies().get(cookieName));
    }
}
