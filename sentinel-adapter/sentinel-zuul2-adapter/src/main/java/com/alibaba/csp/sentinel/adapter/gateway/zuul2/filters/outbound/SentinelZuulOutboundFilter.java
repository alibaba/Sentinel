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

package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.outbound;

import java.util.Deque;

import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.SentinelZuul2Constants;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.EntryHolder;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpOutboundFilter;
import com.netflix.zuul.message.http.HttpResponseMessage;
import rx.Observable;

/**
 * The Zuul outbound filter which will complete the Sentinel entries and
 * trace the exception that happened in previous filters.
 *
 * @author wavesZh
 */
public class SentinelZuulOutboundFilter extends HttpOutboundFilter {

    private final int order;

    public SentinelZuulOutboundFilter(int order) {
        this.order = order;
    }

    @Override
    public int filterOrder() {
        return order;
    }

    @Override
    public Observable<HttpResponseMessage> applyAsync(HttpResponseMessage input) {
        return Observable.just(apply(input));
    }

    public HttpResponseMessage apply(HttpResponseMessage response) {
        SessionContext context = response.getContext();

        if (context.get(SentinelZuul2Constants.ZUUL_CTX_SENTINEL_ENTRIES_KEY) == null) {
            return response;
        }
        boolean previousBlocked = context.getFilterErrors().stream()
            .anyMatch(e -> BlockException.isBlockException(e.getException()));
        Deque<EntryHolder> holders = (Deque<EntryHolder>) context.get(SentinelZuul2Constants.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
        while (!holders.isEmpty()) {
            EntryHolder holder = holders.pop();
            if (!previousBlocked) {
                Tracer.traceEntry(context.getError(), holder.getEntry());
                holder.getEntry().exit(1, holder.getParams());
            }
        }
        return response;
    }


    @Override
    public boolean shouldFilter(HttpResponseMessage msg) {
        return true;
    }
}
