package com.taobao.csp.ahas.service.bridge.aop;

import com.taobao.csp.ahas.service.bridge.aop.classmatcher.ClassMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.MethodMatcher;

public interface PointCut {
   ClassMatcher getClassMatcher();

   MethodMatcher getMethodMatcher();
}
