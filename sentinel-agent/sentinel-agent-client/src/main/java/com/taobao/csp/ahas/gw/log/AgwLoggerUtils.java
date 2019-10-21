package com.taobao.csp.ahas.gw.log;

import java.util.logging.Handler;
import java.util.logging.Logger;

class AgwLoggerUtils {
   static void disableOtherHandlers(Logger logger, Handler handler) {
      if (logger != null) {
         synchronized(logger) {
            Handler[] handlers = logger.getHandlers();
            if (handlers != null) {
               if (handlers.length != 1 || !handlers[0].equals(handler)) {
                  logger.setUseParentHandlers(false);
                  Handler[] var4 = handlers;
                  int var5 = handlers.length;

                  for(int var6 = 0; var6 < var5; ++var6) {
                     Handler h = var4[var6];
                     logger.removeHandler(h);
                  }

                  logger.addHandler(handler);
               }
            }
         }
      }
   }
}
