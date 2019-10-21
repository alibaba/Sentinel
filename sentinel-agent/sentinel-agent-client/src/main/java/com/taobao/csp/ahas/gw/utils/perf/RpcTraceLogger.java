package com.taobao.csp.ahas.gw.utils.perf;

import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RpcTraceLogger {
   public static final String PHASE_BEGIN = "begin";
   public static final String PHASE_CONNECTION_INIT = "connection_init";
   private Map<String, Long> map = new HashMap();
   private long lastTimeStamp = 0L;
   private String reqId;
   private String outerReqId;
   private long firstMarkDate;
   private int code;
   private String message;
   private String requestType;
   private int bodySize;
   private int compressBodySize;
   private int version;

   private boolean needMark() {
      return true;
   }

   public RpcTraceLogger(String reqId, String outerReqId) {
      this.reqId = reqId;
      this.outerReqId = outerReqId;
      this.code = RpcResultCodeEnum.RPC_OK.getCode();
      this.message = RpcResultCodeEnum.RPC_OK.getMessage();
   }

   public void mark(String phase) {
      if (this.needMark()) {
         if (this.lastTimeStamp == 0L) {
            this.firstMarkDate = System.currentTimeMillis();
            this.map.put(phase, 0L);
            this.lastTimeStamp = this.firstMarkDate;
         } else {
            long now = System.currentTimeMillis();
            this.map.put(phase, now - this.lastTimeStamp);
            this.lastTimeStamp = now;
         }
      }
   }

   public void setRequestTypeForRpcLoggerV2(String requestType) {
      this.requestType = requestType;
   }

   public void exception(RpcResultCodeEnum rpcResultCodeEnum) {
      this.code = rpcResultCodeEnum.getCode();
      this.message = rpcResultCodeEnum.getMessage();
   }

   public void exception(int code, String message) {
      this.code = code;
      this.message = message;
   }

   public int getBodySize() {
      return this.bodySize;
   }

   public void setBodySize(int bodySize) {
      this.bodySize = bodySize;
   }

   public int getCompressBodySize() {
      return this.compressBodySize;
   }

   public void setCompressBodySize(int compressBodySize) {
      this.compressBodySize = compressBodySize;
   }

   public int getVersion() {
      return this.version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public String getResult() {
      if (!this.needMark()) {
         return "do not need mark";
      } else {
         StringBuffer sb = new StringBuffer(128);
         sb.append("[reqId:").append(this.reqId).append("|outerReqId:").append(this.outerReqId).append("|request_type:").append(this.requestType).append("|code:").append(this.code).append("|message:").append(this.message).append("|firstMark:").append(this.firstMarkDate).append("|total:").append(System.currentTimeMillis() - this.firstMarkDate).append("|bodySize:").append(this.bodySize).append("|compressBodySize:").append(this.compressBodySize).append("|version:").append(this.version).append("|detail:").append(this.map.toString()).append("]");
         return sb.toString();
      }
   }

   public static void main(String[] args) {
      RpcTraceLogger rpcTraceLogger = new RpcTraceLogger("123", "id1234");
      rpcTraceLogger.setRequestTypeForRpcLoggerV2("client_call_server");
      rpcTraceLogger.mark("begin");

      try {
         TimeUnit.MILLISECONDS.sleep(20L);
      } catch (InterruptedException var4) {
         var4.printStackTrace();
      }

      rpcTraceLogger.mark("ph1");

      try {
         TimeUnit.MILLISECONDS.sleep(30L);
      } catch (InterruptedException var3) {
         var3.printStackTrace();
      }

      rpcTraceLogger.mark("ph2");
      rpcTraceLogger.mark("ph3");
      System.out.println(rpcTraceLogger.getResult());
   }
}
