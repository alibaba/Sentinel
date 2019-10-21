package com.taobao.csp.ahas.gw.io.client;

import com.taobao.csp.ahas.gw.connection.client.ClientConnection;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.util.ConnectionUtil;
import com.taobao.csp.ahas.gw.listener.IClientConnectionListener;
import com.taobao.csp.ahas.gw.listener.client.ClientConnectionListenerFactory;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;

public class NettyClientHandler extends ChannelDuplexHandler {
   private List<IClientConnectionListener> listeners = ClientConnectionListenerFactory.getInstance().getListeners();
   private Integer connectionId;

   public NettyClientHandler(Integer connectionId) {
      this.connectionId = connectionId;
   }

   public void connect(ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress, ChannelPromise future) throws Exception {
      future.addListener(new ChannelFutureListener() {
         public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
               NettyClientHandler.this.callConnectFailListeners(localAddress, remoteAddress, (Integer)future.channel().config().getOption(ChannelOption.CONNECT_TIMEOUT_MILLIS), future.cause());
            }

         }
      });
      super.connect(ctx, remoteAddress, localAddress, future);
   }

   public void channelActive(ChannelHandlerContext ctx) throws Exception {
      if (ConnectionUtil.connectionOfChannel(ctx.channel()) == null) {
         ClientConnection connection = new ClientConnection(ctx.channel());
         connection.setConnectionId(this.connectionId);
         ConnectionUtil.bindConnection(ctx.channel(), connection);
      }

      this.callConnectSuccessListeners((ClientConnection)ConnectionUtil.connectionOfChannel(ctx.channel()));
   }

   public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      this.callConnectionCloseListeners((ClientConnection)ConnectionUtil.connectionOfChannel(ctx.channel()));
   }

   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      this.callConnectionExceptionListeners((ClientConnection)ConnectionUtil.connectionOfChannel(ctx.channel()), cause);
   }

   public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
         this.callConnectionIdleListeners((ClientConnection)ConnectionUtil.connectionOfChannel(ctx.channel()));
      } else {
         super.userEventTriggered(ctx, evt);
      }

   }

   public void write(ChannelHandlerContext ctx, Object writeRequest, ChannelPromise promise) throws Exception {
      final ClientConnection connection = (ClientConnection)ConnectionUtil.connectionOfChannel(ctx.channel());
      final AgwMessage message = (AgwMessage)writeRequest;
      this.callConnectionWriteListeners(connection, message);
      promise.addListener(new ChannelFutureListener() {
         public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
               NettyClientHandler.this.callConnectionWriteSuccessListeners(connection, message);
            } else {
               NettyClientHandler.this.callConnectionWriteFailedListeners(connection, message, future.cause());
            }

         }
      });
      ctx.write(writeRequest, promise);
   }

   public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
      this.callConnectionReadListeners((ClientConnection)ConnectionUtil.connectionOfChannel(ctx.channel()), (AgwMessage)message);
   }

   private void callConnectFailListeners(SocketAddress localAddress, SocketAddress remoteAddress, int timeout, Throwable cause) {
      Iterator var5 = this.listeners.iterator();

      while(var5.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var5.next();

         try {
            listener.connectFail(localAddress, remoteAddress, timeout, cause);
         } catch (Exception var8) {
            LoggerInit.LOGGER.warn("run listener wrong", (Throwable)var8);
         }
      }

   }

   private void callConnectSuccessListeners(ClientConnection connection) {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var2.next();

         try {
            listener.connectSuccess(connection);
         } catch (Exception var5) {
            LoggerInit.LOGGER.warn("run listener wrong", (Throwable)var5);
         }
      }

   }

   private void callConnectionCloseListeners(ClientConnection connection) {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var2.next();

         try {
            listener.close(connection);
         } catch (Exception var5) {
            LoggerInit.LOGGER.warn("run listener wrong", (Throwable)var5);
         }
      }

   }

   private void callConnectionExceptionListeners(ClientConnection connection, Throwable cause) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var3.next();

         try {
            listener.exceptionCaught(connection, cause);
         } catch (Exception var6) {
            LoggerInit.LOGGER.warn("run listener wrong", (Throwable)var6);
         }
      }

   }

   private void callConnectionIdleListeners(ClientConnection connection) {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var2.next();

         try {
            listener.idle(connection);
         } catch (Exception var5) {
            LoggerInit.LOGGER.warn("run listener wrong", (Throwable)var5);
         }
      }

   }

   private void callConnectionWriteListeners(ClientConnection connection, AgwMessage message) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var3.next();

         try {
            listener.write(connection, message);
         } catch (Exception var6) {
            LoggerInit.LOGGER.warn(String.format("run write listener wrong, connection:%d, reqId:%d, outerReqId:%s", connection.info(), message.getHeader().getReqId(), message.getHeader().getOuterReqId()), (Throwable)var6);
         }
      }

   }

   private void callConnectionWriteSuccessListeners(ClientConnection connection, AgwMessage message) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var3.next();

         try {
            listener.writeSuccess(connection, message);
         } catch (Exception var6) {
            LoggerInit.LOGGER.warn(String.format("run write success listener wrong, connection:%d, reqId:%d, outerReqId:%s", connection.info(), message.getHeader().getReqId(), message.getHeader().getOuterReqId()), (Throwable)var6);
         }
      }

   }

   private void callConnectionWriteFailedListeners(ClientConnection connection, AgwMessage message, Throwable cause) {
      Iterator var4 = this.listeners.iterator();

      while(var4.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var4.next();

         try {
            listener.writeFail(connection, message, cause);
         } catch (Exception var7) {
            LoggerInit.LOGGER.warn(String.format("run write fail listener wrong, connection:%d, reqId:%d, outerReqId:%s", connection.info(), message.getHeader().getReqId(), message.getHeader().getOuterReqId()), (Throwable)var7);
         }
      }

   }

   private void callConnectionReadListeners(ClientConnection connection, AgwMessage message) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         IClientConnectionListener listener = (IClientConnectionListener)var3.next();

         try {
            listener.read(connection, message);
         } catch (Exception var6) {
            LoggerInit.LOGGER.warn(String.format("run read listener wrong, connection:%d, reqId:%d, outerReqId:%s", connection.info(), message.getHeader().getReqId(), message.getHeader().getOuterReqId()), (Throwable)var6);
         }
      }

   }
}
