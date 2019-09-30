package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.outbound;

import java.util.Deque;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.ZuulConstant;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpOutboundFilter;
import com.netflix.zuul.message.http.HttpResponseMessage;
import rx.Observable;

/**
 * 负责 exit 以及 trace
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
        if (context.get(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY) == null) {
            return response;
        }
        Throwable throwable = context.getError();
        boolean notBlock = true;
        if (throwable != null && BlockException.isBlockException(throwable)) {
            notBlock = false;
        }

        Deque<AsyncEntry> asyncEntries = (Deque<AsyncEntry>) context.get(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
        while (!asyncEntries.isEmpty()) {
            AsyncEntry asyncEntry = asyncEntries.pop();
            if (notBlock) {
                Tracer.traceEntry(throwable, asyncEntry);
            }
            asyncEntry.exit();
        }
        context.remove(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
        context.remove(ZuulConstant.ZUUL_CTX_SENTINEL_FAIL_ROUTE);
        ContextUtil.exit();
        return response;
    }

    @Override
    public boolean shouldFilter(HttpResponseMessage msg) {
        return true;
    }
}
