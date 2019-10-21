package com.taobao.csp.ahas.module;

import com.taobao.middleware.logger.Logger;
import com.taobao.csp.ahas.module.util.LoggerInit;
import java.util.HashSet;
import java.util.Set;

public class AopService {
   public static final int MAX_ENHANCER_RECORD = 1000;
   private static final Logger LOGGER;
   public static Set<String> enhancerSet;

   public static void addEnhancerClass(String className) {
      if (enhancerSet.size() < 1000) {
         enhancerSet.add(className);
         LOGGER.info("Record enhancer class: {}", className);
      } else {
         LOGGER.warn("Skip record enhancer class: {}", className);
      }

   }

   static {
      LOGGER = LoggerInit.LOGGER;
      enhancerSet = new HashSet();
   }
}
