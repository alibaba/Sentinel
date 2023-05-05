package com.alibaba.csp.sentinel.fallback;

import java.lang.reflect.Method;

/**
 * @author luffy
 * @version 1.0
 * @date 2023/5/5 10:56 上午
 */
public interface IGlobalFallback {


     Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable;

}
