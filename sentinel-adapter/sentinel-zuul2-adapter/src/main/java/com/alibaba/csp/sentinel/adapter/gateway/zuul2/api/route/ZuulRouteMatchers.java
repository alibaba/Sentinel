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
package com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.route;

import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.zuul.message.http.HttpRequestMessage;

/**
 * @author wavesZh
 */
public final class ZuulRouteMatchers {

    public static Predicate<HttpRequestMessage> all() {
        return requestContext -> true;
    }

    public static Predicate<HttpRequestMessage> antPath(String pathPattern) {
        return new PrefixRoutePathMatcher(pathPattern);
    }

    public static Predicate<HttpRequestMessage> exactPath(final String path) {
        return exchange -> exchange.getPath().equals(path);
    }

    public static Predicate<HttpRequestMessage> regexPath(String pathPattern) {
        return new RegexRoutePathMatcher(pathPattern);
    }

    private ZuulRouteMatchers() {}
}
