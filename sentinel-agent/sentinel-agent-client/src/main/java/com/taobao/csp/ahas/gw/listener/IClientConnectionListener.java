package com.taobao.csp.ahas.gw.listener;

import com.taobao.csp.ahas.gw.connection.client.ClientConnection;

import java.net.SocketAddress;

public interface IClientConnectionListener extends IConnectionListener {
   void connectFail(SocketAddress var1, SocketAddress var2, int var3, Throwable var4);

   void connectSuccess(ClientConnection var1);
}
