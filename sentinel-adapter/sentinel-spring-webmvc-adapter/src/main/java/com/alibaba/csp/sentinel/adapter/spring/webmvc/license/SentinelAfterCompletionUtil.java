package com.alibaba.csp.sentinel.adapter.spring.webmvc.license;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;

import javax.servlet.http.HttpServletRequest;

/**
 * Exception interception statistics
 * 
 * @author Roy
 * @date 2022/1/249:15
 */
public class SentinelAfterCompletionUtil {

    public static SentinelWebInterceptor sentinelWebInterceptor;

    /**
     * The afterCompletion method of the HandlerInterceptor does not catch exception statistics when using @ControllerAdvice for global exception fetching
     * Call SentinelAfterException.exit(request, ex) method to realize exception statistics
     * 
     * @param request
     * @param ex
     */
    public static void exit(HttpServletRequest request, Exception ex) {
        sentinelWebInterceptor.afterCompletion(request, null, null, ex);
    }
}
