package com.taobao.csp.ahas.module.api.zuul;

import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import com.taobao.csp.ahas.service.bridge.aop.Enhancer;
import com.taobao.csp.ahas.service.bridge.aop.PointCut;

public class ZuulFilterAspectPlugin implements AspectPlugin {
   public PointCut getPointCut() {
      return new ZuulPointCut();
   }

   public Enhancer getEnhancer() {
      return new ZuulFilterEnhancer();
   }
}
