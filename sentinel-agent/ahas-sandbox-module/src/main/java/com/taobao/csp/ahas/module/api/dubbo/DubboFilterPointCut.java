package com.taobao.csp.ahas.module.api.dubbo;

import com.taobao.csp.ahas.service.bridge.aop.PointCut;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.ClassMatcher;
import com.taobao.csp.ahas.service.bridge.aop.classmatcher.NameClassMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.AndMethodMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.MethodMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.NameMethodMatcher;
import com.taobao.csp.ahas.service.bridge.aop.methodmatcher.ParameterMethodMatcher;

public class DubboFilterPointCut implements PointCut {
   public ClassMatcher getClassMatcher() {
      return new NameClassMatcher("com.alibaba.dubbo.common.extension.ExtensionLoader");
   }

   public MethodMatcher getMethodMatcher() {
      return (new AndMethodMatcher()).and(new NameMethodMatcher("loadFile")).and(new ParameterMethodMatcher(new String[]{"java.util.Map", "java.lang.String"}, 2, 0));
   }
}
