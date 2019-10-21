package com.taobao.csp.ahas.gw.listener.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.connection.client.ClientConnection;
import com.taobao.csp.ahas.gw.task.HeartBeatTask;
import com.taobao.csp.ahas.gw.utils.thread.ThreadPoolManager;

public class ClientConnectionIdleListener extends ClientConnectionListenerEmptyImpl {
   public void idle(Connection connection) {
      ThreadPoolManager.getInstance().getClientThreadPool().execute(new HeartBeatTask((ClientConnection)connection));
   }
}
