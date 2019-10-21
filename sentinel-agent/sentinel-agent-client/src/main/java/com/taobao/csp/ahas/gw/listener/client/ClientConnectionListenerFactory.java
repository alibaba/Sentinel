package com.taobao.csp.ahas.gw.listener.client;

import com.taobao.csp.ahas.gw.listener.IClientConnectionListener;

import java.util.ArrayList;
import java.util.List;

public class ClientConnectionListenerFactory {
   private static final ClientConnectionListenerFactory instance = new ClientConnectionListenerFactory();
   private List<IClientConnectionListener> listeners = new ArrayList();

   private ClientConnectionListenerFactory() {
      this.listeners.add(new ClientConnectionCloseListener());
      this.listeners.add(new ClientConnectionConnectListener());
      this.listeners.add(new ClientConnectionExceptionListener());
      this.listeners.add(new ClientConnectionIdleListener());
      this.listeners.add(new ClientConnectionReadListener());
      this.listeners.add(new ClientConnectionWriteListener());
   }

   public static ClientConnectionListenerFactory getInstance() {
      return instance;
   }

   public List<IClientConnectionListener> getListeners() {
      return this.listeners;
   }
}
