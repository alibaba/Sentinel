package com.taobao.csp.ahas.gw.connection.client;

import com.taobao.csp.ahas.gw.io.client.ClientConfig;
import com.taobao.csp.ahas.gw.io.client.NettyClient;
import com.taobao.csp.ahas.gw.utils.ds.Ring;

import java.util.concurrent.ConcurrentHashMap;

public class ClientConnectionPool {
   private final ConcurrentHashMap<Integer, ClientConnection> pool = new ConcurrentHashMap(4);
   private final ConcurrentHashMap<Integer, Object> parallelLockMap = new ConcurrentHashMap();
   public static ClientConnectionPool instance = new ClientConnectionPool();
   private Ring<Integer> ring = new Ring();
   private Ring<Integer> tls_ring = new Ring();

   private ClientConnectionPool() {
      int i;
      for(i = 0; i < 2; ++i) {
         this.ring.addItem(i);
      }

      for(i = 2; i < 4; ++i) {
         this.tls_ring.addItem(i);
      }

   }

   public static ClientConnectionPool getInstance() {
      return instance;
   }

   public ClientConnection removeConnection(Integer connectionId) {
      return (ClientConnection)this.pool.remove(connectionId);
   }

   public ClientConnection getConnection(boolean tls) {
      Integer connectionId;
      if (tls) {
         connectionId = (Integer)this.tls_ring.pollItem();
      } else {
         connectionId = (Integer)this.ring.pollItem();
      }

      ClientConnection connection = null;
      if (connectionId != null) {
         connection = (ClientConnection)this.pool.get(connectionId);
         if (connection == null) {
            synchronized(connectionId) {
               connection = (ClientConnection)this.pool.get(connectionId);
               if (connection == null) {
                  connection = NettyClient.getInstance().connect(ClientConfig.CLIENT_CONNECT_IP, ClientConfig.CLIENT_CONNECT_PORT, connectionId, tls);
                  if (connection != null) {
                     ClientConnection oldStream = (ClientConnection)this.pool.putIfAbsent(connectionId, connection);
                     connection.setConnectionId(connectionId);
                     if (oldStream != null) {
                        connection.close();
                        connection = oldStream;
                     }
                  }
               }
            }
         }
      }

      return connection;
   }

   private Object getStreamConnectLock(Integer connectionId) {
      Object newLock = new Object();
      Object lock = this.parallelLockMap.putIfAbsent(connectionId, newLock);
      if (lock == null) {
         lock = newLock;
      }

      return lock;
   }
}
