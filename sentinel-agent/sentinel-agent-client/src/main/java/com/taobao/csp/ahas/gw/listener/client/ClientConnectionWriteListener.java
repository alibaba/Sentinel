package com.taobao.csp.ahas.gw.listener.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageHeader;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;

public class ClientConnectionWriteListener extends ClientConnectionListenerEmptyImpl {
   public void write(Connection connection, AgwMessage message) {
   }

   public void writeSuccess(Connection connection, AgwMessage message) {
   }

   public void writeFail(Connection connection, AgwMessage message, Throwable cause) {
      AgwMessageHeader header = message.getHeader();
      header.setRpcResultCodeEnum(RpcResultCodeEnum.RPC_INTERNAL_ERROR);
      connection.handleSyncWriteError(message);
      LoggerInit.LOGGER.warn(String.format("write fail, connection:%s, reqId:%d, outerReqId:%s", connection.info(), header.getReqId(), header.getOuterReqId()), cause);
   }
}
