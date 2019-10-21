package com.taobao.csp.ahas.gw.listener.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.connection.client.ClientConnection;
import com.taobao.csp.ahas.gw.connection.client.ClientConnectionPool;
import com.taobao.csp.ahas.gw.logger.LoggerInit;

public class ClientConnectionCloseListener extends ClientConnectionListenerEmptyImpl {
   public void close(Connection connection) {
      ClientConnectionPool.getInstance().removeConnection(((ClientConnection)connection).getConnectionId());
      LoggerInit.LOGGER.warn(String.format("close connection : %s, uuid:%s", connection.info(), connection.uuid()));
      connection.close();
   }
}
