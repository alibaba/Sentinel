package com.taobao.csp.ahas.gw.io.protocol;

import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;
import com.taobao.csp.ahas.gw.utils.perf.RpcTraceLogger;
import com.taobao.csp.ahas.gw.utils.perf.TimestatmpUtil;

public final class AgwMessage {
   private AgwMessageHeader header;
   private String body;
   private TimestatmpUtil timestampUtil;
   private RpcTraceLogger rpcTraceLogger;

   public AgwMessageHeader getHeader() {
      return this.header;
   }

   public void setHeader(AgwMessageHeader header) {
      this.header = header;
   }

   public String getBody() {
      return this.body;
   }

   public void setBody(String body) {
      this.body = body;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(256);
      sb.append("header  ===>  ").append(this.header).append("\n");
      sb.append("body  ===>  ").append(this.body).append("\n");
      return sb.toString();
   }

   public void mark(String phase) {
      if (this.timestampUtil != null) {
         this.timestampUtil.mark(phase);
      }

      if (this.timestampUtil != null || this.header != null) {
         if (this.timestampUtil != null || this.header == null || this.header.getReqId() != 0L) {
            if (this.timestampUtil == null) {
               this.timestampUtil = new TimestatmpUtil(String.valueOf(this.header.getReqId()), this.header.getOuterReqId());
               this.timestampUtil.mark(phase);
            }

         }
      }
   }

   public String getPerf() {
      return this.timestampUtil == null ? "no perf data" : this.timestampUtil.getResult();
   }

   public void markV2(String phase) {
      if (this.rpcTraceLogger != null || this.header != null) {
         if (this.rpcTraceLogger == null) {
            this.rpcTraceLogger = new RpcTraceLogger(String.valueOf(this.header.getReqId()), this.header.getOuterReqId());
         }

         this.rpcTraceLogger.mark(phase);
      }
   }

   public String getPerfV2() {
      return this.rpcTraceLogger == null ? "no rpc trace data" : this.rpcTraceLogger.getResult();
   }

   public void init(int bodySize, int compressBodySize, int version) {
      this.rpcTraceLogger = null;
      this.markV2("begin");
      this.rpcTraceLogger.setBodySize(bodySize);
      this.rpcTraceLogger.setCompressBodySize(compressBodySize);
      this.rpcTraceLogger.setVersion(version);
   }

   public void setRequestTypeForRpcLoggerV2(String requestType) {
      this.rpcTraceLogger.setRequestTypeForRpcLoggerV2(requestType);
   }

   public void initRpcTraceLogger(String requestType) {
      this.rpcTraceLogger = null;
      this.markV2("begin");
      this.rpcTraceLogger.setRequestTypeForRpcLoggerV2(requestType);
   }

   public AgwMessage transformToResponse() {
      this.getHeader().setMessageDirection((byte)2);
      return this;
   }

   public AgwMessage transformToResponseWithOk(AgwMessage response) {
      this.setBody(response.getBody());
      this.getHeader().setRpcResultCodeEnum(RpcResultCodeEnum.RPC_OK);
      return this.transformToResponse();
   }

   public AgwMessage transformToResponseWithError(RpcResultCodeEnum rpcResultCodeEnum) {
      this.getHeader().setRpcResultCodeEnum(rpcResultCodeEnum);
      this.rpcTraceLogger.exception(rpcResultCodeEnum);
      return this.transformToResponse();
   }

   public void markErrorV2(RpcResultCodeEnum rpcResultCodeEnum) {
      this.rpcTraceLogger.exception(rpcResultCodeEnum);
   }

   public AgwMessage transformToResponseWithError(int code, String message) {
      this.getHeader().setInnerCode(code);
      this.getHeader().setInnerMsg(message);
      this.rpcTraceLogger.exception(code, message);
      return this.transformToResponse();
   }

   public void setRpcTraceLoggerTo(AgwMessage message) {
      message.rpcTraceLogger = this.rpcTraceLogger;
   }

   public boolean checkFormat() {
      return true;
   }

   public void clearMark() {
      this.timestampUtil = null;
   }
}
