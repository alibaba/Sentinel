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
package com.alibaba.csp.sentinel.adapter.gateway.zuul.filters;

import java.util.Deque;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant;
import com.alibaba.csp.sentinel.context.ContextUtil;

import com.netflix.zuul.context.RequestContext;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
final class SentinelEntryUtils {

    @SuppressWarnings("unchecked")
    static void tryExitFromCurrentContext() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.containsKey(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY)) {
            Deque<AsyncEntry> asyncEntries = (Deque<AsyncEntry>) ctx.get(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
            AsyncEntry entry;
            while (!asyncEntries.isEmpty()) {
                entry = asyncEntries.pop();
                entry.exit();
            }
            ctx.remove(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
        }

        ContextUtil.exit();
    }

    @SuppressWarnings("unchecked")
    static void tryTraceExceptionThenExitFromCurrentContext(Throwable t) {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.containsKey(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY)) {
            Deque<AsyncEntry> asyncEntries = (Deque<AsyncEntry>) ctx.get(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
            AsyncEntry entry;
            while (!asyncEntries.isEmpty()) {
                entry = asyncEntries.pop();
                Tracer.traceEntry(t, entry);
                entry.exit();
            }
            ctx.remove(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
        }
        ContextUtil.exit();
    }

    private SentinelEntryUtils() {}
}
