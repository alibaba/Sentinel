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
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * This filter track routing exception and exit entry;
 *
 * @author tiger
 * @author Eric Zhao
 */
public class SentinelZuulErrorFilter extends ZuulFilter {

    private final int order;

    public SentinelZuulErrorFilter() {
        this(-1);
    }

    public SentinelZuulErrorFilter(int order) {
        this.order = order;
    }

    @Override
    public String filterType() {
        return ZuulConstant.ERROR_TYPE;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.getThrowable() != null;
    }

    @Override
    public int filterOrder() {
        return order;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        Throwable throwable = ctx.getThrowable();
        if (throwable != null) {
            if (!BlockException.isBlockException(throwable)) {
                // Trace exception for each entry and exit entries in order.
                // The entries can be retrieved from the request context.
                SentinelEntryUtils.tryTraceExceptionThenExitFromCurrentContext(throwable);
                RecordLog.info("[SentinelZuulErrorFilter] Trace error cause", throwable.getCause());
            }
        }
        return null;
    }
}
