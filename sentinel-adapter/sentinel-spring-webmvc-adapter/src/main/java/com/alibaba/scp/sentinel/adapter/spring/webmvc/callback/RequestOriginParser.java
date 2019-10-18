package com.alibaba.scp.sentinel.adapter.spring.webmvc.callback;

import javax.servlet.http.HttpServletRequest;

/**
 * The origin parser parses request origin (e.g. IP, user, appName) from HTTP request.
 *
 * @author zhaoyuguang
 */
public interface RequestOriginParser {

    /**
     * Parse the origin from given HTTP request.
     *
     * @param request HTTP request
     * @return parsed origin
     */
    String parseOrigin(HttpServletRequest request);
}
