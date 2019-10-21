package com.taobao.csp.ahas.module.api.gateway;

import com.taobao.csp.ahas.service.bridge.aop.PointCut;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.ClassMatcher;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.NameClassMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.AndMethodMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.MethodMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.NameMethodMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.ParameterMethodMatcher;

public class ScGatewayPointCut implements PointCut {
   public ClassMatcher getClassMatcher() {
      return new NameClassMatcher("org.springframework.cloud.gateway.config.GatewayAutoConfiguration");
   }

   public MethodMatcher getMethodMatcher() {
      return (new AndMethodMatcher()).and(new NameMethodMatcher("filteringWebHandler")).and(new ParameterMethodMatcher(new String[]{"java.util.List"}, 1, 0));
   }
}
