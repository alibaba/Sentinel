package com.taobao.csp.ahas.gw.io.client;

import com.taobao.csp.ahas.gw.connection.client.ClientConnection;
import com.taobao.csp.ahas.gw.io.client.codec.ProtocolHandler;
import com.taobao.csp.ahas.gw.io.protocol.util.ConnectionUtil;
import com.taobao.csp.ahas.gw.logger.LoggerInit;
import com.taobao.csp.ahas.gw.oss.OssCert;
import com.taobao.csp.ahas.gw.tls.SslContextFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;

public final class NettyClient {
   private static final String CONNECT_TIMEOUT_KEY = "_CONNECTTIMEOUT";
   public static NettyClient instance = new NettyClient();
   public static final NioEventLoopGroup WORKER_POOL = new NioEventLoopGroup();

   private NettyClient() {
   }

   public static NettyClient getInstance() {
      return instance;
   }

   public ClientConnection connect(String ip, int port, final Integer connectionId, final boolean tls) {
      ChannelFuture future = null;
      ClientConnection cc = null;

      try {
         Bootstrap bootstrap = new Bootstrap();
         ((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)bootstrap.group(WORKER_POOL)).option(ChannelOption.TCP_NODELAY, true)).option(ChannelOption.SO_REUSEADDR, true)).option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)).option(ChannelOption.AUTO_CLOSE, Boolean.TRUE)).option(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.FALSE)).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)).channel(NioSocketChannel.class)).handler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
               if (tls) {
                  SSLEngine engine = SslContextFactory.getClientContext(OssCert.clientCertPath, OssCert.clientCertPath).createSSLEngine();
                  engine.setUseClientMode(true);
                  ch.pipeline().addLast((String)"ssl", (ChannelHandler)(new SslHandler(engine)));
               }

               ch.pipeline().addLast((String)"protocol", (ChannelHandler)(new ProtocolHandler())).addLast((String)"clientIdleHandler", (ChannelHandler)(new IdleStateHandler(0, 0, 10))).addLast((String)"clientHandler", (ChannelHandler)(new NettyClientHandler(connectionId)));
            }
         });
         future = bootstrap.connect(new InetSocketAddress(ip, port));
         future.awaitUninterruptibly();
         if (future.isSuccess()) {
            if (ConnectionUtil.connectionOfChannel(future.channel()) == null) {
               ClientConnection connection = new ClientConnection(future.channel());
               connection.setConnectionId(connectionId);
               ConnectionUtil.bindConnection(future.channel(), connection);
            }

            cc = (ClientConnection)ConnectionUtil.connectionOfChannel(future.channel());
         } else {
            LoggerInit.LOGGER.warn(String.format("connect %s:%d wrong", ip, port), future.cause());
         }
      } catch (Throwable var9) {
         LoggerInit.LOGGER.warn(String.format("connect to %s:%d wrong", ip, port), var9);
      }

      return cc;
   }
}
