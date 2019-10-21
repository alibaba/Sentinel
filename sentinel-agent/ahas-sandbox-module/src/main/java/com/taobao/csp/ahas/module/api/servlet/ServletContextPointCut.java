package com.taobao.csp.ahas.module.api.servlet;

import com.taobao.csp.ahas.service.bridge.aop.PointCut;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.ClassMatcher;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.InterfaceMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.MethodMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.NameMethodMatcher;

public class ServletContextPointCut implements PointCut {
   public ClassMatcher getClassMatcher() {
      return new InterfaceMatcher("javax.servlet.ServletContext");
   }

   public MethodMatcher getMethodMatcher() {
      return new NameMethodMatcher("<init>");
   }
}
