package com.alibaba.csp.sentinel.annotation.aspectj.integration.service;

/**
 * @author zhaoyuguang
 */

public class GlobalFallback {

    public static String doFallback(int i) {
        return "GlobalFallback:doFallback:" + i;
    }
}
