package com.taobao.csp.ahas.service.bridge.aop.classmatcher;

import com.taobao.csp.ahas.service.bridge.aop.ClassInfo;

public class NameClassMatcher implements ClassMatcher {
   private String className;

   public NameClassMatcher(String className) {
      this.className = className;
   }

   public boolean isMatched(String className, ClassInfo classInfo) {
      return this.className.equals(className);
   }
}
