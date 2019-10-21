package com.taobao.csp.ahas.gw.task;

import com.taobao.csp.ahas.gw.client.bootstrap.outer.AgwClient;
import com.taobao.csp.ahas.gw.connection.client.ClientConnection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageFactory;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageHeader;
import com.taobao.csp.ahas.gw.io.protocol.ReqHeartBeatMessageBuilder;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;

public class HeartBeatTask implements Runnable {
   private ClientConnection connection;

   public HeartBeatTask(ClientConnection connection) {
      this.connection = connection;
   }

   public void run() {
      AgwClient client = AgwClient.getInstance();
      AgwMessage request = AgwMessageFactory.createAgwMessage(ReqHeartBeatMessageBuilder.getInstance(), client.getClientVpcId(), client.getClientIpLong(), client.getClientProcessFlag(), "HB", this.connection.getConnectionId(), "HBGATEWAY", 3000, false, false, "HBMSGBODY");
      if (request == null) {
         LoggerInit.LOGGER.warn("build heartbeat req message fail");
      } else {
         request.getHeader().setOuterReqId("noReqIdForHB");
         LoggerInit.LOGGER.info(String.format("send heartbeat, reqId:%d, outerReqId:%s", request.getHeader().getReqId(), request.getHeader().getOuterReqId()));
         AgwMessage response = this.connection.writeAndFlushSync(request);
         AgwMessageHeader responseHeader = response.getHeader();
         if (responseHeader != null && responseHeader.getInnerCode() != RpcResultCodeEnum.RPC_OK.getCode()) {
            LoggerInit.LOGGER.warn(String.format("client send hb to gateway wrong, code:%d, msg:%s, reqId:%d, outerReqId:%s", responseHeader.getInnerCode(), responseHeader.getInnerMsg(), request.getHeader().getReqId(), request.getHeader().getOuterReqId()));
         }

      }
   }
}
