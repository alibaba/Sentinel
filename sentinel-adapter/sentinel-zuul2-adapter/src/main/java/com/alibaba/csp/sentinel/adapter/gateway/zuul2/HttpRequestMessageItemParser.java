package com.alibaba.csp.sentinel.adapter.gateway.zuul2;

import com.alibaba.csp.sentinel.adapter.gateway.common.param.RequestItemParser;
import com.netflix.zuul.message.http.HttpRequestMessage;

public class HttpRequestMessageItemParser implements RequestItemParser<HttpRequestMessage> {
    @Override
    public String getPath(HttpRequestMessage request) {
        return request.getInboundRequest().getPath();
    }

    @Override
    public String getRemoteAddress(HttpRequestMessage request) {
        return request.getOriginalHost();
    }

    @Override
    public String getHeader(HttpRequestMessage request, String key) {
        return String.valueOf(request.getInboundRequest().getHeaders().get(key));
    }

    @Override
    public String getUrlParam(HttpRequestMessage request, String paramName) {
        return String.valueOf(request.getInboundRequest().getQueryParams().get(paramName));
    }

    @Override
    public String getCookieValue(HttpRequestMessage request, String cookieName) {
        return String.valueOf(request.getInboundRequest().parseCookies().get(cookieName));
    }
}
