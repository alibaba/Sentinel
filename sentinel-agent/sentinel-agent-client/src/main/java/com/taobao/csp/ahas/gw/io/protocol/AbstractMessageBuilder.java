package com.taobao.csp.ahas.gw.io.protocol;

import com.taobao.csp.ahas.gw.utils.AgwStringUtil;

public abstract class AbstractMessageBuilder {
   public AgwMessageHeader buildMessageHeader(String userVpcId, long userIp, String userProcessFlag, String handlerName, int connectionId, String appName, int readTimeoutMs, boolean requestCompress, boolean responseCompress) {
      if (AgwStringUtil.isBlank(userVpcId)) {
         return null;
      } else if (userIp < 0L) {
         return null;
      } else if (AgwStringUtil.isBlank(userProcessFlag)) {
         return null;
      } else if (AgwStringUtil.isBlank(handlerName)) {
         return null;
      } else if (connectionId < 0) {
         return null;
      } else if (AgwStringUtil.isBlank(appName)) {
         return null;
      } else if (readTimeoutMs < 0) {
         return null;
      } else {
         AgwMessageHeader header = new AgwMessageHeader();
         header.setClientVpcId(userVpcId);
         header.setClientIp(userIp);
         header.setConnectionId(connectionId);
         header.setServerName(appName);
         header.setTimeoutMs(readTimeoutMs);
         header.setHandlerName(handlerName);
         header.setClientProcessFlag(userProcessFlag);
         if (!requestCompress && !responseCompress) {
            header.setVersion(1);
         } else if (requestCompress && responseCompress) {
            header.setVersion(2);
         } else if (requestCompress && !responseCompress) {
            header.setVersion(3);
         } else if (!requestCompress && responseCompress) {
            header.setVersion(4);
         }

         return this.buildMessageHeader0(header);
      }
   }

   protected abstract AgwMessageHeader buildMessageHeader0(AgwMessageHeader var1);

   public abstract String buildMessageBody(String var1);
}
