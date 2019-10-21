package com.taobao.csp.ahas.gw.listener.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.connection.client.ClientConnection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.listener.IClientConnectionListener;

import java.net.SocketAddress;

public class ClientConnectionListenerEmptyImpl implements IClientConnectionListener {
   public void connectFail(SocketAddress localAddress, SocketAddress remoteAddress, int timeout, Throwable cause) {
   }

   public void connectSuccess(ClientConnection connection) {
   }

   public void close(Connection connection) {
   }

   public void exceptionCaught(Connection connection, Throwable cause) {
   }

   public void idle(Connection connection) {
   }

   public void write(Connection connection, AgwMessage message) {
   }

   public void writeSuccess(Connection connection, AgwMessage message) {
   }

   public void writeFail(Connection connection, AgwMessage message, Throwable cause) {
   }

   public void read(Connection connection, AgwMessage message) {
   }
}
