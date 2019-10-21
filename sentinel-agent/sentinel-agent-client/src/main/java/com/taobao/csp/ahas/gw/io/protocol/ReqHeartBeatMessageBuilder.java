package com.taobao.csp.ahas.gw.io.protocol;

import com.taobao.csp.ahas.gw.io.protocol.util.UUIDGenerator;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;

public final class ReqHeartBeatMessageBuilder extends AbstractMessageBuilder {
   public static final String HEARTBEAT_HANDLER_NAME = "HB";
   public static final String HEARTBEAT_APP_NAME = "HBGATEWAY";
   public static final String HEARTBEAT_MESSAGE_BODY = "HBMSGBODY";
   public static final int HEARTBEAT_TIMEOUT_MS = 3000;
   private static ReqHeartBeatMessageBuilder instance = new ReqHeartBeatMessageBuilder();

   private ReqHeartBeatMessageBuilder() {
   }

   public static ReqHeartBeatMessageBuilder getInstance() {
      return instance;
   }

   protected AgwMessageHeader buildMessageHeader0(AgwMessageHeader header) {
      if (header == null) {
         return null;
      } else {
         header.setCaller((byte)0);
         header.setMessageType((byte)1);
         header.setMessageDirection((byte)1);
         header.setInnerCode(RpcResultCodeEnum.RPC_OK.getCode());
         header.setInnerMsg(RpcResultCodeEnum.RPC_OK.getMessage());
         header.setReqId(UUIDGenerator.next());
         return header;
      }
   }

   public String buildMessageBody(String body) {
      return body;
   }
}
