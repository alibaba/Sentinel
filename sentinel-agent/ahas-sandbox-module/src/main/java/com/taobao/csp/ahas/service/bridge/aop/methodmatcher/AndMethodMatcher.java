package com.taobao.csp.ahas.service.bridge.aop.methodmatcher;

import com.taobao.csp.ahas.service.bridge.aop.MethodInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AndMethodMatcher implements com.taobao.csp.ahas.service.bridge.aop.methodmatcher.MethodMatcher {
   private List<MethodMatcher> matchers = new ArrayList(2);

   public AndMethodMatcher and(MethodMatcher methodMatcher) {
      if (methodMatcher != null) {
         this.matchers.add(methodMatcher);
      }

      return this;
   }

   public boolean isMatched(String methodName, MethodInfo methodInfo) {
      Iterator var3 = this.matchers.iterator();

      MethodMatcher matcher;
      do {
         if (!var3.hasNext()) {
            return true;
         }

         matcher = (MethodMatcher)var3.next();
      } while(matcher.isMatched(methodName, methodInfo));

      return false;
   }
}
