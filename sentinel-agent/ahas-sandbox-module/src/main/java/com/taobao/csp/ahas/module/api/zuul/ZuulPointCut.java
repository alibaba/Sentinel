package com.taobao.csp.ahas.module.api.zuul;

import com.taobao.csp.ahas.service.bridge.aop.PointCut;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.ClassMatcher;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.NameClassMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.MethodMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.NameMethodMatcher;

public class ZuulPointCut implements PointCut {
   public ClassMatcher getClassMatcher() {
      return new NameClassMatcher("org.springframework.cloud.netflix.zuul.ZuulFilterInitializer");
   }

   public MethodMatcher getMethodMatcher() {
      return new NameMethodMatcher("contextInitialized");
   }
}
