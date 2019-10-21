package com.taobao.csp.ahas.gw.utils.perf;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimestatmpUtil {
   private Map<String, Long> map = new HashMap();
   private long lastTimeStamp = 0L;
   private String reqId;
   private String outerReqId;
   private String firstMarkDate;

   private boolean needMark() {
      return true;
   }

   public TimestatmpUtil(String reqId, String outerReqId) {
      this.reqId = reqId;
      this.outerReqId = outerReqId;
   }

   public void mark(String phase) {
      if (this.needMark()) {
         if (this.lastTimeStamp == 0L) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            this.firstMarkDate = sdf.format(new Date());
            this.map.put(phase, 0L);
            this.lastTimeStamp = System.currentTimeMillis();
         } else {
            long now = System.currentTimeMillis();
            this.map.put(phase, now - this.lastTimeStamp);
            this.lastTimeStamp = now;
         }
      }
   }

   public String getResult() {
      if (!this.needMark()) {
         return "do not need mark";
      } else {
         StringBuffer sb = new StringBuffer(128);
         sb.append("time statistics [reqId:").append(this.reqId).append(", outerReqId:").append(this.outerReqId).append(", firstMark:").append(this.firstMarkDate).append(", info:").append(this.map.toString()).append("]");
         return sb.toString();
      }
   }
}
