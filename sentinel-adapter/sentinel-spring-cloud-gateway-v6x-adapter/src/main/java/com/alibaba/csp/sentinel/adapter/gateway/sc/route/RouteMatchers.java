/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.sc.route;


import com.alibaba.csp.sentinel.util.function.Predicate;

import org.springframework.web.server.ServerWebExchange;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public final class RouteMatchers {

    public static Predicate<ServerWebExchange> all() {
        return exchange -> true;
    }

    public static Predicate<ServerWebExchange> antPath(String pathPattern) {
        return new AntRoutePathMatcher(pathPattern);
    }

    public static Predicate<ServerWebExchange> exactPath(final String path) {
        return exchange -> exchange.getRequest().getPath().value().equals(path);
    }

    public static Predicate<ServerWebExchange> regexPath(String pathPattern) {
        return new RegexRoutePathMatcher(pathPattern);
    }

    private RouteMatchers() {}
}
