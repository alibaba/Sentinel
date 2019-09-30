package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.endpoint;

import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.ZuulConstant;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.BlockResponse;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.fallback.ZuulBlockFallbackProvider;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;

/**
 * 自定义异常处理 Filter, 返回静态 response
 */
public class SentinelZuulEndpoint extends HttpSyncEndpoint {

    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        SessionContext context = request.getContext();
        Throwable throwable = context.getError();
        String fallBackRoute = (String) context.get(ZuulConstant.ZUUL_CTX_SENTINEL_FAIL_ROUTE);
        ZuulBlockFallbackProvider zuulBlockFallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(
                fallBackRoute);
        HttpResponseMessage resp = new HttpResponseMessageImpl(context, request, 200);
        BlockResponse response = zuulBlockFallbackProvider.fallbackResponse(fallBackRoute, throwable);
        resp.setBodyAsText(response.toString());
        // need to set this manually since we are not going through the ProxyEndpoint
        StatusCategoryUtils.setStatusCategory(context, ZuulStatusCategory.FAILURE_LOCAL);
        return resp;
    }
}
