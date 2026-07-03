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
package com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.matcher;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.matcher.AbstractApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.route.ZuulRouteMatchers;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.zuul.message.http.HttpRequestMessage;

/**
 * @author wavesZh
 */
public class HttpRequestMessageApiMatcher extends AbstractApiMatcher<HttpRequestMessage> {

    public HttpRequestMessageApiMatcher(ApiDefinition apiDefinition) {
        super(apiDefinition);
    }

    @Override
    protected void initializeMatchers() {
        if (apiDefinition.getPredicateItems() != null) {
            for (ApiPredicateItem item : apiDefinition.getPredicateItems()) {
                Predicate<HttpRequestMessage> predicate = fromApiPredicate(item);
                if (predicate != null) {
                    matchers.add(predicate);
                }
            }
        }
    }

    private Predicate<HttpRequestMessage> fromApiPredicate(/*@NonNull*/ ApiPredicateItem item) {
        if (item instanceof ApiPathPredicateItem) {
            return fromApiPathPredicate((ApiPathPredicateItem)item);
        }
        return null;
    }

    private Predicate<HttpRequestMessage> fromApiPathPredicate(/*@Valid*/ ApiPathPredicateItem item) {
        String pattern = item.getPattern();
        if (StringUtil.isBlank(pattern)) {
            return null;
        }
        switch (item.getMatchStrategy()) {
            case SentinelGatewayConstants.URL_MATCH_STRATEGY_REGEX:
                return ZuulRouteMatchers.regexPath(pattern);
            case SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX:
                return ZuulRouteMatchers.antPath(pattern);
            default:
                return ZuulRouteMatchers.exactPath(pattern);
        }
    }
}
