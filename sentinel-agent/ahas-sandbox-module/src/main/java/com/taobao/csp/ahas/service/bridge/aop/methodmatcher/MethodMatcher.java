package com.taobao.csp.ahas.service.bridge.aop.methodmatcher;

import com.taobao.csp.ahas.service.bridge.aop.MethodInfo;

public interface MethodMatcher {
   boolean isMatched(String var1, MethodInfo var2);
}
