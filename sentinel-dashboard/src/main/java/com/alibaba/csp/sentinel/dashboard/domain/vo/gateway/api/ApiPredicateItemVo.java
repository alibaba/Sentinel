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
package com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api;

/**
 * Value Object for add or update gateway api.
 *
 * @author cdfive
 * @since 1.7.0
 */
public class ApiPredicateItemVo {

    /**
     * The pattern for matching url.
     */
    private String pattern;

    /**
     * The matching Strategy in url. Constants are defined in class SentinelGatewayConstants.\
     *
     * <ul>
     *     <li>0(URL_MATCH_STRATEGY_EXACT): exact match mode</li>
     *     <li>1(URL_MATCH_STRATEGY_PREFIX): prefix match mode</li>
     *     <li>2(URL_MATCH_STRATEGY_REGEX): regex match mode</li>
     * </ul>
     */
    private Integer matchStrategy;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getMatchStrategy() {
        return matchStrategy;
    }

    public void setMatchStrategy(Integer matchStrategy) {
        this.matchStrategy = matchStrategy;
    }
}
