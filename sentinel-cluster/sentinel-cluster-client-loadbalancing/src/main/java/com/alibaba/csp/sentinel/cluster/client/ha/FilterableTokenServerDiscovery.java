/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.client.ha;

import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.spi.SpiLoader;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.List;

/**
 * token Server Discovery delegate, can filter the  token server result of the delegate
 *
 * @author icodening
 * @date 2022.03.17
 */
public class FilterableTokenServerDiscovery implements TokenServerDiscovery {

    private final TokenServerDiscovery delegate;

    private final List<TokenServerFilter> tokenServerFilters;

    public FilterableTokenServerDiscovery(TokenServerDiscovery delegate) {
        AssertUtil.notNull(delegate, "delegate cannot be null");
        this.delegate = delegate;
        this.tokenServerFilters = SpiLoader.of(TokenServerFilter.class).loadInstanceListSorted();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TokenServerDescriptor> getTokenServers(String appName) {
        List<TokenServerDescriptor> tokenServers = delegate.getTokenServers(appName);
        for (TokenServerFilter tokenServerFilter : tokenServerFilters) {
            tokenServers = tokenServerFilter.filter(tokenServers);
        }
        return tokenServers;
    }
}
