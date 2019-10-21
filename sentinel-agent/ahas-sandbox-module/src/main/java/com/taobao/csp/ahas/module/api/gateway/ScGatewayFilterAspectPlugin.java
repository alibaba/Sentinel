package com.taobao.csp.ahas.module.api.gateway;

import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import com.taobao.csp.ahas.service.bridge.aop.Enhancer;
import com.taobao.csp.ahas.service.bridge.aop.PointCut;

public class ScGatewayFilterAspectPlugin implements AspectPlugin {
   public PointCut getPointCut() {
      return new ScGatewayPointCut();
   }

   public Enhancer getEnhancer() {
      return new ScGatewayFilterEnhancer();
   }
}
