package com.taobao.csp.ahas.transport.api;

public class TimestampInterceptor extends RequestInterceptorChain {
   public static final int MAX_INVALID_TIME = 120000;
   public static final String TIMESTAMP_KEY = "ts";

   public void doHandle(Request request) throws RequestException {
      String ts = request.getParam("ts");
      if (ts != null && ts.length() != 0) {
         try {
            long clientTime = Long.valueOf(ts);
            long serverTime = System.currentTimeMillis();
            if (serverTime - clientTime > 120000L) {
               throw new InvalidTimestampException("exceeding maximum failure time, server time: " + serverTime + ", " + "client time: " + clientTime);
            }
         } catch (NumberFormatException var8) {
            throw new InvalidTimestampException(ts + ", " + var8.getMessage());
         }
      } else {
         throw new InvalidTimestampException("missing timestamp");
      }
   }

   public void doInvoke(Request request) throws RequestException {
      request.addParam("ts", System.currentTimeMillis() + "");
   }
}
