package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.reflect.Method;

/**
 * Simple interface that defines API used to filter out irrelevant
 * methods
 */
public interface MethodFilter
{
    public boolean includeMethod(Method m);
}
