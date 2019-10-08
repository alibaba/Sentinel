package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.endpoint;

import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.ZuulConstant;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.BlockResponse;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.ZuulBlockFallbackProvider;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.inbound.SentinelZuulInboundFilter;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;

/**
 * Default Endpoint for handle exception happening in {@link SentinelZuulInboundFilter}.
 *
 * @author wavesZh
 */
public class SentinelZuulEndpoint extends HttpSyncEndpoint {

    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        SessionContext context = request.getContext();
        Throwable throwable = context.getError();
        String fallBackRoute = (String) context.get(ZuulConstant.ZUUL_CTX_SENTINEL_FALLBACK_ROUTE);
        ZuulBlockFallbackProvider zuulBlockFallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(
                fallBackRoute);
        HttpResponseMessage resp = new HttpResponseMessageImpl(context, request, 200);
        BlockResponse response = zuulBlockFallbackProvider.fallbackResponse(fallBackRoute, throwable);
        resp.setBodyAsText(response.toString());
//        // need to set this manually since we are not going through the ProxyEndpoint
//        StatusCategoryUtils.setStatusCategory(context, ZuulStatusCategory.FAILURE_LOCAL);
        return resp;
    }
}
