package com.alibaba.csp.sentinel.fallback;

import java.lang.reflect.Method;

/**
 * Default global fallback
 *
 * @author luffy
 */
public class DefaultGlobalFallback implements IGlobalFallback {

    @Override
    public Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable{
        throw t;
    }
}
