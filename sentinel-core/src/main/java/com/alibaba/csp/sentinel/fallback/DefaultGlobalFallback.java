package com.alibaba.csp.sentinel.fallback;

import java.lang.reflect.Method;

/**
 * @author luffy
 * @version 1.0
 * @date 2023/5/5 11:02 下午
 */
public class DefaultGlobalFallback implements IGlobalFallback {

    @Override
    public Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable{
        throw t;
    }
}
