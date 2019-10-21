package com.taobao.csp.ahas.gw.io.protocol;

import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;

public class AgwMessageHeader {
   public static final String REQUEST_TYPE_AGW_FROM_CLIENT_BIZ = "agw_from_client_biz";
   public static final String REQUEST_TYPE_AGW_FROM_CLIENT_HB = "agw_from_client_hb";
   public static final String REQUEST_TYPE_AGW_FROM_SERVER_BIZ = "agw_from_server_biz";
   public static final String REQUEST_TYPE_AGW_FROM_SERVER_HB = "agw_from_server_hb";
   private int bodyLength;
   private long reqId;
   private byte messageType;
   private byte messageDirection;
   private byte caller;
   private long clientIp;
   private int clientVpcIdLength;
   private String clientVpcId;
   private int serverNameLength;
   private String serverName;
   private int timeoutMs;
   private int clientProcessFlagLength;
   private String clientProcessFlag;
   private int innerCode;
   private int innerMsgLength;
   private String innerMsg;
   private int connectionId;
   private int handlerNameLength;
   private String handlerName;
   private int outerReqIdLength;
   private String outerReqId;
   private int version;

   public AgwMessageHeader() {
      this.innerCode = RpcResultCodeEnum.RPC_OK.getCode();
      this.innerMsgLength = 0;
      this.innerMsg = RpcResultCodeEnum.RPC_OK.getMessage();
      this.connectionId = 1;
      this.handlerNameLength = 1;
      this.outerReqIdLength = 0;
      this.version = 1;
   }

   public int getHeadLength() {
      return 63 + this.clientVpcIdLength + this.serverNameLength + this.clientProcessFlagLength + this.innerMsgLength + this.handlerNameLength + this.outerReqIdLength;
   }

   public int getBodyLength() {
      return this.bodyLength;
   }

   public void setBodyLength(int bodyLength) {
      this.bodyLength = bodyLength;
   }

   public long getReqId() {
      return this.reqId;
   }

   public void setReqId(long reqId) {
      this.reqId = reqId;
   }

   public byte getMessageType() {
      return this.messageType;
   }

   public void setMessageType(byte messageType) {
      this.messageType = messageType;
   }

   public int getTimeoutMs() {
      return this.timeoutMs;
   }

   public void setTimeoutMs(int timeoutMs) {
      this.timeoutMs = timeoutMs;
   }

   public byte getCaller() {
      return this.caller;
   }

   public void setCaller(byte caller) {
      this.caller = caller;
   }

   public long getClientIp() {
      return this.clientIp;
   }

   public void setClientIp(long clientIp) {
      this.clientIp = clientIp;
   }

   public int getClientVpcIdLength() {
      return this.clientVpcIdLength;
   }

   public void setClientVpcIdLength(int clientVpcIdLength) {
      this.clientVpcIdLength = clientVpcIdLength;
   }

   public byte getMessageDirection() {
      return this.messageDirection;
   }

   public void setMessageDirection(byte messageDirection) {
      this.messageDirection = messageDirection;
   }

   public int getServerNameLength() {
      return this.serverNameLength;
   }

   public void setServerNameLength(int serverNameLength) {
      this.serverNameLength = serverNameLength;
   }

   public String getClientVpcId() {
      return this.clientVpcId;
   }

   public void setClientVpcId(String clientVpcId) {
      this.clientVpcId = clientVpcId;
   }

   public String getServerName() {
      return this.serverName;
   }

   public void setServerName(String serverName) {
      this.serverName = serverName;
   }

   public int getClientProcessFlagLength() {
      return this.clientProcessFlagLength;
   }

   public void setClientProcessFlagLength(int clientProcessFlagLength) {
      this.clientProcessFlagLength = clientProcessFlagLength;
   }

   public String getClientProcessFlag() {
      return this.clientProcessFlag;
   }

   public void setClientProcessFlag(String clientProcessFlag) {
      this.clientProcessFlag = clientProcessFlag;
   }

   public int getConnectionId() {
      return this.connectionId;
   }

   public void setConnectionId(int connectionId) {
      this.connectionId = connectionId;
   }

   public void setRpcResultCodeEnum(RpcResultCodeEnum result) {
      if (result == null) {
         this.innerCode = RpcResultCodeEnum.RPC_INTERNAL_ERROR.getCode();
         this.innerMsg = RpcResultCodeEnum.RPC_INTERNAL_ERROR.getMessage();
      } else {
         this.innerCode = result.getCode();
         this.innerMsg = result.getMessage();
      }
   }

   public int getInnerCode() {
      return this.innerCode;
   }

   public void setInnerCode(int innerCode) {
      this.innerCode = innerCode;
   }

   public String getInnerMsg() {
      return this.innerMsg;
   }

   public void setInnerMsg(String innerMsg) {
      this.innerMsg = innerMsg;
   }

   public int getInnerMsgLength() {
      return this.innerMsgLength;
   }

   public void setInnerMsgLength(int innerMsgLength) {
      this.innerMsgLength = innerMsgLength;
   }

   public int getHandlerNameLength() {
      return this.handlerNameLength;
   }

   public void setHandlerNameLength(int handlerNameLength) {
      this.handlerNameLength = handlerNameLength;
   }

   public String getHandlerName() {
      return this.handlerName;
   }

   public void setHandlerName(String handlerName) {
      this.handlerName = handlerName;
   }

   public int getOuterReqIdLength() {
      return this.outerReqIdLength;
   }

   public void setOuterReqIdLength(int outerReqIdLength) {
      this.outerReqIdLength = outerReqIdLength;
   }

   public String getOuterReqId() {
      return this.outerReqId;
   }

   public void setOuterReqId(String outerReqId) {
      this.outerReqId = outerReqId;
   }

   public int getVersion() {
      return this.version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(256);
      sb.append("bodyLength: ").append(this.bodyLength).append(", ");
      sb.append("reqId: ").append(this.reqId).append(", ");
      sb.append("messageType: ").append(this.messageType).append(", ");
      sb.append("messageDirection: ").append(this.messageDirection).append(", ");
      sb.append("caller: ").append(this.caller).append(", ");
      sb.append("clientIp: ").append(this.clientIp).append(", ");
      sb.append("clientVpcIdLength: ").append(this.clientVpcIdLength).append(", ");
      sb.append("clientVpcId: ").append(this.clientVpcId).append(", ");
      sb.append("serverNameLength: ").append(this.serverNameLength).append(",");
      sb.append("serverName: ").append(this.serverName).append(",");
      sb.append("timeoutMs: ").append(this.timeoutMs).append(",");
      sb.append("clientProcessFlag: ").append(this.clientProcessFlag).append(", ");
      sb.append("innerCode: ").append(this.innerCode).append(", ");
      sb.append("innerMsg: ").append(this.innerMsg).append(", ");
      sb.append("innerMsgLength: ").append(this.innerMsgLength).append(", ");
      sb.append("connectionId: ").append(this.connectionId).append(", ");
      sb.append("handlerName: ").append(this.handlerName).append(", ");
      sb.append("outerReqid: ").append(this.outerReqId).append(", ");
      sb.append("version: ").append(this.version).append(", ");
      return sb.toString();
   }
}
