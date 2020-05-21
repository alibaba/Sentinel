package com.alibaba.csp.sentinel.annotation.aspectj.integration.service;

/**
 * @author zhaoyuguang
 */

public class GlobalFallback {

    public static String doFallback(Throwable t) {
        return "GlobalFallback:doFallback";
    }
}
