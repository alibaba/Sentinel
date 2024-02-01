package com.alibaba.csp.sentinel.annotation.aspectj.integration.fallback;

import com.alibaba.csp.sentinel.fallback.SentinelAnnotationGlobalFallback;

import java.lang.reflect.Method;

/**
 * @author luffy
 */
public class AnnotationGlobalFallback implements SentinelAnnotationGlobalFallback {

    @Override
    public Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable{
        return "AnnotationGlobalFallback";
    }
}
