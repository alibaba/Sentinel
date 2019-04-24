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

import java.util.regex.Pattern;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

import org.springframework.web.server.ServerWebExchange;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public class RegexRoutePathMatcher implements Predicate<ServerWebExchange> {

    private final String pattern;
    private final Pattern regex;

    public RegexRoutePathMatcher(String pattern) {
        AssertUtil.assertNotBlank(pattern, "pattern cannot be blank");
        this.pattern = pattern;
        this.regex = Pattern.compile(pattern);
    }

    @Override
    public boolean test(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        return regex.matcher(path).matches();
    }

    public String getPattern() {
        return pattern;
    }
}
