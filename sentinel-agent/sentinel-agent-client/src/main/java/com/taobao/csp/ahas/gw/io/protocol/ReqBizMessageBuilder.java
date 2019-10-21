package com.taobao.csp.ahas.gw.io.protocol;

import com.taobao.csp.ahas.gw.io.protocol.util.UUIDGenerator;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;

public final class ReqBizMessageBuilder extends AbstractMessageBuilder {
   private static ReqBizMessageBuilder instance = new ReqBizMessageBuilder();

   private ReqBizMessageBuilder() {
   }

   public static ReqBizMessageBuilder getInstance() {
      return instance;
   }

   protected AgwMessageHeader buildMessageHeader0(AgwMessageHeader header) {
      if (header == null) {
         return null;
      } else {
         header.setCaller((byte)0);
         header.setMessageType((byte)2);
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
