package com.taobao.csp.ahas.gw.connection.client;

import com.taobao.csp.ahas.gw.connection.Connection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageHeader;
import com.taobao.csp.ahas.gw.io.protocol.util.KeyGenerator;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.upstream.RpcResultCodeEnum;
import com.taobao.csp.ahas.gw.utils.thread.SettableFuture;
import io.netty.channel.Channel;

import java.util.*;

public class ClientConnection implements Connection {
   private Channel channel;
   private int connectionId;
   private String innerId;
   private Map<String, SettableFuture<AgwMessage>> id2Handler = new HashMap();

   public ClientConnection(Channel channel) {
      this.channel = channel;
      this.innerId = UUID.randomUUID().toString();
   }

   public boolean isInited() {
      return false;
   }

   public boolean init(AgwMessage agwMessage) {
      return true;
   }

   public void writeAndFlush(AgwMessage msg) {
      this.channel.writeAndFlush(msg);
   }

   public AgwMessage writeAndFlushSync(AgwMessage param1) {
      // $FF: Couldn't be decompiled
      return null;
   }

   public void notifySyncWrite(AgwMessage msg) {
      SettableFuture<AgwMessage> future = (SettableFuture)this.id2Handler.remove(KeyGenerator.generateMsgIdForConnection(msg));
      if (future == null) {
         LoggerInit.LOGGER.info("future is blank");
      } else {
         future.set(msg);
      }
   }

   public void handleSyncWriteError(AgwMessage msg) {
      this.id2Handler.remove(KeyGenerator.generateMsgIdForConnection(msg));
   }

   public void close() {
      Set<String> removeSet = new HashSet(this.id2Handler.keySet());
      Iterator var2 = removeSet.iterator();

      while(var2.hasNext()) {
         String id = (String)var2.next();
         if (id != null) {
            SettableFuture<AgwMessage> future = (SettableFuture)this.id2Handler.remove(id);
            if (future != null) {
               AgwMessage msg = new AgwMessage();
               AgwMessageHeader header = new AgwMessageHeader();
               msg.setHeader(header);
               header.setInnerCode(RpcResultCodeEnum.RPC_CLIENT_CLOSE_EXCEPTION.getCode());
               header.setInnerMsg(RpcResultCodeEnum.RPC_CLIENT_CLOSE_EXCEPTION.getMessage());
               future.set(msg);
            }
         }
      }

      if (this.channel != null) {
         this.channel.close();
      }

   }

   public String info() {
      return String.valueOf(this.connectionId);
   }

   public String uuid() {
      return this.innerId;
   }

   public int getConnectionId() {
      return this.connectionId;
   }

   public void setConnectionId(int connectionId) {
      this.connectionId = connectionId;
   }
}
