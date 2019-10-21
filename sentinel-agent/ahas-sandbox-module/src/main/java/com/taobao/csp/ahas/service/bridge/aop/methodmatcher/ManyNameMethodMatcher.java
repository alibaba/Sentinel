package com.taobao.csp.ahas.service.bridge.aop.methodmatcher;

import com.taobao.csp.ahas.service.bridge.aop.MethodInfo;
import java.util.Set;

public class ManyNameMethodMatcher implements com.taobao.csp.ahas.service.bridge.aop.methodmatcher.MethodMatcher {
   private Set<String> methodNames;

   public ManyNameMethodMatcher(Set<String> methodNames) {
      this.methodNames = methodNames;
   }

   public boolean isMatched(String methodName, MethodInfo methodInfo) {
      return this.methodNames.contains(methodName);
   }
}
