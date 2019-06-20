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
package com.alibaba.csp.sentinel.adapter.gateway.sc;

import java.net.InetSocketAddress;
import java.util.Optional;

import com.alibaba.csp.sentinel.adapter.gateway.common.param.RequestItemParser;

import org.springframework.http.HttpCookie;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public class ServerWebExchangeItemParser implements RequestItemParser<ServerWebExchange> {

    @Override
    public String getPath(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().value();
    }

    @Override
    public String getRemoteAddress(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null) {
            return null;
        }
        return remoteAddress.getAddress().getHostAddress();
    }

    @Override
    public String getHeader(ServerWebExchange exchange, String key) {
        return exchange.getRequest().getHeaders().getFirst(key);
    }

    @Override
    public String getUrlParam(ServerWebExchange exchange, String paramName) {
        return exchange.getRequest().getQueryParams().getFirst(paramName);
    }

    @Override
    public String getCookieValue(ServerWebExchange exchange, String cookieName) {
        return Optional.ofNullable(exchange.getResponse().getCookies().getFirst(cookieName))
            .map(HttpCookie::getValue)
            .orElse(null);
    }
}
