package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcTotalConfig;

import javax.servlet.http.HttpServletRequest;

/**
 * @author by lfy
 * @Date 2022/8/3 20:31
 */
public class SentinelWebExceptionHandlerHelper {

    private static final String DEFAULT_REQUEST_EXCEPTION_ATTRIBUTE_NAME = SentinelWebMvcConfig.DEFAULT_REQUEST_ATTRIBUTE_NAME + "-exception";
    private static final String DEFAULT_TOTAL_REQUEST_EXCEPTION_ATTRIBUTE_NAME = SentinelWebMvcTotalConfig.DEFAULT_REQUEST_ATTRIBUTE_NAME + "-exception";


    public static void handlerRequestException(HttpServletRequest request, Throwable t) {
        if (request == null) {
            return;
        }
        request.setAttribute(DEFAULT_REQUEST_EXCEPTION_ATTRIBUTE_NAME, t);
    }

    public static void handlerTotalRequestException(HttpServletRequest request, Throwable t) {
        if (request == null) {
            return;
        }
        request.setAttribute(DEFAULT_TOTAL_REQUEST_EXCEPTION_ATTRIBUTE_NAME, t);
    }

    protected static Throwable getAndClearCurrentRequestException(HttpServletRequest request) {
        return getAndClearRequestException(request, DEFAULT_REQUEST_EXCEPTION_ATTRIBUTE_NAME);
    }

    protected static Throwable getAndClearTotalRequestException(HttpServletRequest request) {
        return getAndClearRequestException(request, DEFAULT_TOTAL_REQUEST_EXCEPTION_ATTRIBUTE_NAME);
    }

    private static Throwable getAndClearRequestException(HttpServletRequest request, String attr) {
        if (request == null) {
            return null;
        }
        Throwable t = (Throwable) request.getAttribute(attr);
        request.removeAttribute(attr);
        return t;
    }
}
