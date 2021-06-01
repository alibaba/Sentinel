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
package com.alibaba.csp.sentinel.adapter.gateway.sc.api.matcher;

import java.util.Optional;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.matcher.AbstractApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.sc.route.RouteMatchers;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

import org.springframework.web.server.ServerWebExchange;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public class WebExchangeApiMatcher extends AbstractApiMatcher<ServerWebExchange> {

    public WebExchangeApiMatcher(ApiDefinition apiDefinition) {
        super(apiDefinition);
    }

    @Override
    protected void initializeMatchers() {
        if (apiDefinition.getPredicateItems() != null) {
            apiDefinition.getPredicateItems().forEach(item ->
                fromApiPredicate(item).ifPresent(matchers::add));
        }
    }

    private Optional<Predicate<ServerWebExchange>> fromApiPredicate(/*@NonNull*/ ApiPredicateItem item) {
        if (item instanceof ApiPathPredicateItem) {
            return fromApiPathPredicate((ApiPathPredicateItem)item);
        }
        return Optional.empty();
    }

    private Optional<Predicate<ServerWebExchange>> fromApiPathPredicate(/*@Valid*/ ApiPathPredicateItem item) {
        String pattern = item.getPattern();
        if (StringUtil.isBlank(pattern)) {
            return Optional.empty();
        }
        switch (item.getMatchStrategy()) {
            case SentinelGatewayConstants.URL_MATCH_STRATEGY_REGEX:
                return Optional.of(RouteMatchers.regexPath(pattern));
            case SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX:
                return Optional.of(RouteMatchers.antPath(pattern));
            default:
                return Optional.of(RouteMatchers.exactPath(pattern));
        }
    }
}
