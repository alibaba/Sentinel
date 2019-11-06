package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpInboundSyncFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.netty.filter.ZuulEndPointRunner;

public class Route extends HttpInboundSyncFilter {
	@Override
	public HttpRequestMessage apply(HttpRequestMessage input) {
		String path = input.getPath();
		SessionContext context = input.getContext();
		context.setEndpoint(ZuulEndPointRunner.PROXY_ENDPOINT_FILTER_NAME);
		if (path.equalsIgnoreCase("/aliyun")) {
			context.setRouteVIP("aliyun");
		} else {
			context.setRouteVIP("another");
		}
		return input;
	}

	@Override
	public int filterOrder() {
		return 0;
	}

	@Override
	public boolean shouldFilter(HttpRequestMessage msg) {
		return true;
	}
}
