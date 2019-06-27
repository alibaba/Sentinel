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

package com.alibaba.csp.sentinel.adapter.gateway.zuul.filters;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;

import static com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant.SEND_RESPONSE_FILTER_ORDER;

/**
 * This filter will mark complete and exit {@link com.alibaba.csp.sentinel.Entry}.
 *
 * @author tiger
 * @author Eric Zhao
 */
public class SentinelZuulPostFilter extends ZuulFilter {

    private final int order;

    public SentinelZuulPostFilter() {
        this(SEND_RESPONSE_FILTER_ORDER);
    }

    public SentinelZuulPostFilter(int order) {
        this.order = order;
    }

    @Override
    public String filterType() {
        return ZuulConstant.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return order;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        // Exit the entries in order.
        // The entries can be retrieved from the request context.
        SentinelEntryUtils.tryExitFromCurrentContext();
        return null;
    }
}
