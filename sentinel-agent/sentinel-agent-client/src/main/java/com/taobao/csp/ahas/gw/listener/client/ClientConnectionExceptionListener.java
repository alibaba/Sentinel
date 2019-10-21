package com.taobao.csp.ahas.gw.listener.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.logger.LoggerInit;

public class ClientConnectionExceptionListener extends ClientConnectionListenerEmptyImpl {
   public void exceptionCaught(Connection connection, Throwable cause) {
      LoggerInit.LOGGER.warn(String.format("exception caught and close the connection:%s, uuid:%s", connection.info(), connection.uuid()), cause);
      connection.close();
   }
}
