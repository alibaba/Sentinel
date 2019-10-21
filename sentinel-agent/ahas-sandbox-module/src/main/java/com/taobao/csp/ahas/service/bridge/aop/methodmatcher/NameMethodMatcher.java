package com.taobao.csp.ahas.service.bridge.aop.methodmatcher;

import com.taobao.csp.ahas.service.bridge.aop.MethodInfo;

public class NameMethodMatcher implements MethodMatcher {
   private String methodName;

   public NameMethodMatcher(String methodName) {
      this.methodName = methodName;
   }

   public boolean isMatched(String methodName, MethodInfo methodInfo) {
      return this.methodName.equals(methodName);
   }
}
