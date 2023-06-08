package com.alibaba.csp.sentinel.annotation.cdi.interceptor.integration.service;

import com.alibaba.csp.sentinel.fallback.IGlobalFallback;

import java.lang.reflect.Method;

/**
 * @author luffy
 */
public class InterceptorGlobalFallback implements IGlobalFallback {

    @Override
    public Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable{
        return "InterceptorGlobalFallback";
    }
}
