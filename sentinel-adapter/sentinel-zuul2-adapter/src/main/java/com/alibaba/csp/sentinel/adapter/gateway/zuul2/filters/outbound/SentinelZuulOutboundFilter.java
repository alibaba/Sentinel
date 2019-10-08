package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.outbound;

import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.ZuulConstant;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.FilterError;
import com.netflix.zuul.filters.http.HttpOutboundFilter;
import com.netflix.zuul.message.http.HttpResponseMessage;
import org.apache.commons.collections.CollectionUtils;
import rx.Observable;

/**
 * OutboundFilter for Sentinel.
 *
 * The filter will complete the entries and trace the exception that happen in previous filters.
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
		List<FilterError> errors = context.getFilterErrors().stream()
				.filter(e -> BlockException.isBlockException(e.getException()))
				.collect(Collectors.toList());
		boolean notBlock = true;
		if (CollectionUtils.isEmpty(errors)) {
			notBlock = false;
		}
		Deque<AsyncEntry> asyncEntries = (Deque<AsyncEntry>) context.get(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
		while (!asyncEntries.isEmpty()) {
			AsyncEntry asyncEntry = asyncEntries.pop();
			if (notBlock) {
				Tracer.traceEntry(context.getError(), asyncEntry);
			}
			asyncEntry.exit();
		}
		context.remove(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY);
		context.remove(ZuulConstant.ZUUL_CTX_SENTINEL_FALLBACK_ROUTE);
		ContextUtil.exit();
		return response;
	}

	@Override
	public boolean shouldFilter(HttpResponseMessage msg) {
		return true;
	}
}
