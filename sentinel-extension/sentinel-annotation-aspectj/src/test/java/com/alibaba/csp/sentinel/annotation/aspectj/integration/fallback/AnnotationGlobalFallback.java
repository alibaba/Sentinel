package com.alibaba.csp.sentinel.annotation.aspectj.integration.fallback;

import com.alibaba.csp.sentinel.fallback.IGlobalFallback;

import java.lang.reflect.Method;

/**
 * @author luffy
 * @version 1.0
 * @date 2023/5/5 5:18 下午
 */
public class AnnotationGlobalFallback implements IGlobalFallback {

    @Override
    public Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable{
        return "AnnotationGlobalFallback";
    }
}
