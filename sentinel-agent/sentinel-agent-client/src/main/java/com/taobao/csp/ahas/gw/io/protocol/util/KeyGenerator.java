package com.taobao.csp.ahas.gw.io.protocol.util;

import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageHeader;
import com.taobao.csp.ahas.gw.utils.misc.IpUtil;

public class KeyGenerator {
   private static final String CONNECTIONS_GROUP_KEY_PREFIX = "agw_conn";
   private static final String GATEWAY_KEY_PREFIX = "agw_server";
   private static final String GATEWAY_KEY = "agw_server_" + IpUtil.getIp();

   public static String getConnectionGroupKeyFromMsg(AgwMessage msg) {
      if (msg == null) {
         return null;
      } else {
         AgwMessageHeader header = msg.getHeader();
         return getConnectionGroupKey(header.getClientVpcId(), header.getClientIp(), header.getClientProcessFlag());
      }
   }

   public static String getConnectionGroupKey(String clientVpcId, long clientIpLong, String clientProcessFlag) {
      StringBuilder sb = new StringBuilder();
      sb.append("agw_conn").append("_").append(clientVpcId).append("_").append(clientIpLong).append("_").append(clientProcessFlag);
      return sb.toString();
   }

   public static String generateMsgIdForConnection(AgwMessage msg) {
      StringBuilder sb = new StringBuilder(128);
      sb.append(getConnectionGroupKeyFromMsg(msg)).append("_").append(msg.getHeader().getReqId());
      return sb.toString();
   }

   public static String getGatewayKey() {
      long now = System.currentTimeMillis();
      return GATEWAY_KEY;
   }
}
