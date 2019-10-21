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
package com.alibaba.csp.sentinel.adapter.gateway.common.api;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;

import java.util.Objects;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public class ApiPathPredicateItem implements ApiPredicateItem {

    private String pattern;
    private int matchStrategy = SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT;

    public ApiPathPredicateItem setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public ApiPathPredicateItem setMatchStrategy(int matchStrategy) {
        this.matchStrategy = matchStrategy;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public int getMatchStrategy() {
        return matchStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ApiPathPredicateItem that = (ApiPathPredicateItem)o;

        if (matchStrategy != that.matchStrategy) { return false; }
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + matchStrategy;
        return result;
    }

    @Override
    public String toString() {
        return "ApiPathPredicateItem{" +
            "pattern='" + pattern + '\'' +
            ", matchStrategy=" + matchStrategy +
            '}';
    }
}
