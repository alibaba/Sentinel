package com.alibaba.csp.sentinel.adapter.gateway.zuul.callback;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tiger
 */
public class DefaultRequestOriginParser implements RequestOriginParser {

    @Override
    public String parseOrigin(HttpServletRequest request) {
        return "";
    }
}
