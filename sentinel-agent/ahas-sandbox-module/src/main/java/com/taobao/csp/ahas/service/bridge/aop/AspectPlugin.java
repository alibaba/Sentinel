package com.taobao.csp.ahas.service.bridge.aop;

public interface AspectPlugin {
   PointCut getPointCut();

   Enhancer getEnhancer();
}
