package com.alibaba.csp.sentinel.adapter.zuul.fallback;

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
