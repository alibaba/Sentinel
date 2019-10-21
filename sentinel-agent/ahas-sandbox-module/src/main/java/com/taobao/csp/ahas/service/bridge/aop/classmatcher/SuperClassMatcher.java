package com.taobao.csp.ahas.service.bridge.aop.classmatcher;

import com.taobao.csp.ahas.service.bridge.aop.ClassInfo;

public class SuperClassMatcher implements ClassMatcher {
   private String className;
   private String superClass;

   public SuperClassMatcher(String className, String superClass) {
      this.className = className;
      this.superClass = superClass;
   }

   public boolean isMatched(String className, ClassInfo classInfo) {
      return this.superClass.equals(classInfo.getSuperName()) && !this.className.equals(className);
   }
}
