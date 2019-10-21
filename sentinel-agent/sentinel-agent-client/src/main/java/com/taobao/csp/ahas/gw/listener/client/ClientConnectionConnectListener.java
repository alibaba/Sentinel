package com.taobao.csp.ahas.gw.listener.client;

import com.taobao.csp.ahas.gw.connection.client.ClientConnection;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.task.HeartBeatTask;
import com.taobao.csp.ahas.gw.utils.thread.ThreadPoolManager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientConnectionConnectListener extends ClientConnectionListenerEmptyImpl {
   public void connectFail(SocketAddress localAddress, SocketAddress remoteAddress, int timeout, Throwable cause) {
      if (localAddress == null || remoteAddress == null) {
         LoggerInit.LOGGER.warn("empty address");
      }

      InetSocketAddress remote = (InetSocketAddress)localAddress;
      LoggerInit.LOGGER.warn(String.format("connection %s failed, timeout:%d", remote.getAddress().getHostAddress(), timeout), cause);
   }

   public void connectSuccess(ClientConnection connection) {
      ThreadPoolManager.getInstance().getClientThreadPool().execute(new HeartBeatTask(connection));
   }
}
