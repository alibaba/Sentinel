package com.taobao.csp.ahas.gw.io.protocol;

public final class AgwMessageFactory {
   public static final int MESSAGE_TYPE_HEARTBEAT = 1;
   public static final int MESSAGE_TYPE_BIZ = 1;

   public static AgwMessage createAgwMessage(AbstractMessageBuilder builder, String userVpcId, long userIp, String userProcessFlag, String handlerName, int connectionId, String appName, int readTimeoutMs, boolean requestCompress, boolean responseCompress, String body) {
      if (builder == null) {
         return null;
      } else {
         AgwMessageHeader header = builder.buildMessageHeader(userVpcId, userIp, userProcessFlag, handlerName, connectionId, appName, readTimeoutMs, requestCompress, responseCompress);
         String body0 = builder.buildMessageBody(body);
         if (header != null && body0 != null) {
            AgwMessage message = new AgwMessage();
            message.setHeader(header);
            message.setBody(body0);
            return message;
         } else {
            return null;
         }
      }
   }
}
