package com.alibaba.csp.sentinel.fallback;

import java.lang.reflect.Method;

/**
 * Global fallback interface
 *
 * @author luffy
 */
public interface IGlobalFallback {


     Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable;

}
