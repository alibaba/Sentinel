package com.taobao.csp.ahas.gw.logger;

import com.taobao.middleware.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class LoggerUtil {
   public static void activateAsync(Logger logger, int queueSize, int discardingThreshold, boolean includeCallerData, int maxFlushTime, boolean neverBlock) {
      List<Object[]> args = new ArrayList();
      args.add(new Object[]{"setQueueSize", new Class[]{Integer.TYPE}, queueSize});
      args.add(new Object[]{"setDiscardingThreshold", new Class[]{Integer.TYPE}, discardingThreshold});
      args.add(new Object[]{"setIncludeCallerData", new Class[]{Boolean.TYPE}, includeCallerData});
      args.add(new Object[]{"setMaxFlushTime", new Class[]{Integer.TYPE}, maxFlushTime});
      args.add(new Object[]{"setNeverBlock", new Class[]{Boolean.TYPE}, neverBlock});

      try {
         logger.activateAsync(args);
      } catch (Throwable var8) {
         logger.warn("[AGW-LOGGER] activate async failed! Please use logback and upgrade logger.api to 0.2.3 or above" + var8.getMessage());
      }

   }
}
