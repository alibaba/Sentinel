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

package com.alibaba.csp.sentinel.adapter.zuul.filters;

import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.netflix.zuul.ZuulFilter;

/**
 * Abstract class for sentinel filters.
 *
 * @author tiger
 */
public abstract class AbstractSentinelFilter extends ZuulFilter {

    private final SentinelZuulProperties sentinelZuulProperties;

    public SentinelZuulProperties getSentinelZuulProperties() {
        return sentinelZuulProperties;
    }

    public AbstractSentinelFilter(SentinelZuulProperties sentinelZuulProperties) {
        AssertUtil.notNull(sentinelZuulProperties,"SentinelZuulProperties can not be null");
        this.sentinelZuulProperties = sentinelZuulProperties;
    }

    @Override
    public boolean shouldFilter() {
        return sentinelZuulProperties.isEnabled();
    }

}
