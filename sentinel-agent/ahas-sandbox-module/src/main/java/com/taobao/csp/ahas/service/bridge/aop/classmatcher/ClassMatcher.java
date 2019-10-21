package com.taobao.csp.ahas.service.bridge.aop.classmatcher;

import com.taobao.csp.ahas.service.bridge.aop.ClassInfo;

public interface ClassMatcher {
   boolean isMatched(String var1, ClassInfo var2);
}
