package com.taobao.csp.ahas.module.api.servlet;

import com.taobao.csp.ahas.service.bridge.aop.AspectPlugin;
import com.taobao.csp.ahas.service.bridge.aop.Enhancer;
import com.taobao.csp.ahas.service.bridge.aop.PointCut;

public class ServletContextAspectPlugin implements AspectPlugin {
   public PointCut getPointCut() {
      return new ServletContextPointCut();
   }

   public Enhancer getEnhancer() {
      return new ServletContextEnhancer();
   }
}
