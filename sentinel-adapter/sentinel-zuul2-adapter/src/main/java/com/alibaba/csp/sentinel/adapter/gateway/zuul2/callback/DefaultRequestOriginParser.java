package com.alibaba.csp.sentinel.adapter.gateway.zuul2.callback;

import com.netflix.zuul.message.http.HttpRequestMessage;

/**
 * @author wavesZh
 */
public class DefaultRequestOriginParser implements RequestOriginParser {

    @Override
    public String parseOrigin(HttpRequestMessage request) {
        return "";
    }
}
