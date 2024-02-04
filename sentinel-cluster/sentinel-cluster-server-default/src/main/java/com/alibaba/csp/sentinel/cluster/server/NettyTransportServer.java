/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.cluster.server.codec.netty.NettyRequestDecoder;
import com.alibaba.csp.sentinel.cluster.server.codec.netty.NettyResponseEncoder;
import com.alibaba.csp.sentinel.cluster.server.connection.Connection;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionPool;
import com.alibaba.csp.sentinel.cluster.server.handler.TokenServerHandler;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.SystemPropertyUtil;

import static com.alibaba.csp.sentinel.cluster.server.ServerConstants.*;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class NettyTransportServer implements ClusterTokenServer {

    private static final int DEFAULT_BOSS_EVENT_LOOP_THREADS = 1;
    private static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1,
        SystemPropertyUtil.getInt("io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
    private static final int MAX_RETRY_TIMES = 3;
    private static final int RETRY_SLEEP_MS = 2000;

    private final int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final ConnectionPool connectionPool = new ConnectionPool();

    private final AtomicInteger currentState = new AtomicInteger(SERVER_STATUS_OFF);
    private final AtomicInteger failedTimes = new AtomicInteger(0);

    public NettyTransportServer(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        if (!currentState.compareAndSet(SERVER_STATUS_OFF, SERVER_STATUS_STARTING)) {
            return;
        }
        ServerBootstrap b = new ServerBootstrap();
        EventLoopInfo eventLoopInfo = new EventLoopInfo(DEFAULT_BOSS_EVENT_LOOP_THREADS, DEFAULT_EVENT_LOOP_THREADS);
        this.bossGroup = eventLoopInfo.bossEventLoopGroup;
        this.workerGroup = eventLoopInfo.workEventLoopGroup;
        b.group(bossGroup, workerGroup)
            .channel(eventLoopInfo.serverSocketChannelClass)
            .option(ChannelOption.SO_BACKLOG, 128)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
                    p.addLast(new NettyRequestDecoder());
                    p.addLast(new LengthFieldPrepender(2));
                    p.addLast(new NettyResponseEncoder());
                    p.addLast(new TokenServerHandler(connectionPool));
                }
            })
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
            .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .childOption(ChannelOption.SO_TIMEOUT, 10)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_RCVBUF, 32 * 1024);
        b.bind(port).addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.cause() != null) {
                    RecordLog.info("[NettyTransportServer] Token server start failed (port=" + port + "), failedTimes: " + failedTimes.get(),
                        future.cause());
                    currentState.compareAndSet(SERVER_STATUS_STARTING, SERVER_STATUS_OFF);
                    int failCount = failedTimes.incrementAndGet();
                    if (failCount > MAX_RETRY_TIMES) {
                        return;
                    }

                    try {
                        Thread.sleep(failCount * RETRY_SLEEP_MS);
                        start();
                    } catch (Throwable e) {
                        RecordLog.info("[NettyTransportServer] Failed to start token server when retrying", e);
                    }
                } else {
                    RecordLog.info("[NettyTransportServer] Token server started success at port {}", port);
                    currentState.compareAndSet(SERVER_STATUS_STARTING, SERVER_STATUS_STARTED);
                }
            }
        });
    }

    @Override
    public void stop() {
        // If still initializing, wait for ready.
        while (currentState.get() == SERVER_STATUS_STARTING) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore.
            }
        }

        if (currentState.compareAndSet(SERVER_STATUS_STARTED, SERVER_STATUS_OFF)) {
            try {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                connectionPool.shutdownAll();

                failedTimes.set(0);

                RecordLog.info("[NettyTransportServer] Sentinel token server stopped");
            } catch (Exception ex) {
                RecordLog.warn("[NettyTransportServer] Failed to stop token server (port=" + port + ")", ex);
            }
        }
    }

    public void refreshRunningServer() {
        connectionPool.refreshIdleTask();
    }

    public void closeConnection(String clientIp, int clientPort) throws Exception {
        Connection connection = connectionPool.getConnection(clientIp, clientPort);
        connection.close();
    }

    public void closeAll() throws Exception {
        List<Connection> connections = connectionPool.listAllConnection();
        for (Connection connection : connections) {
            connection.close();
        }
    }

    public List<String> listAllClient() {
        List<String> clients = new ArrayList<String>();
        List<Connection> connections = connectionPool.listAllConnection();
        for (Connection conn : connections) {
            clients.add(conn.getConnectionKey());
        }
        return clients;
    }

    public int getCurrentState() {
        return currentState.get();
    }

    public int clientCount() {
        return connectionPool.count();
    }

    static class EventLoopInfo {
        private static final String EPOLL_CLASS_NAME = "io.netty.channel.epoll.Epoll";
        private static final String EPOLL_EVENT_LOOP_GROUP_CLASS_NAME = "io.netty.channel.epoll.EpollEventLoopGroup";
        private static final String EPOLL_SERVER_SOCKET_CHANNEL_CLASS_NAME = "io.netty.channel.epoll.EpollServerSocketChannel";
        private static final String KQUEUE_CLASS_NAME = "io.netty.channel.kqueue.KQueue";
        private static final String KQUEUE_EVENT_LOOP_GROUP_CLASS_NAME = "io.netty.channel.kqueue.KQueueEventLoopGroup";
        private static final String KQUEUE_SERVER_SOCKET_CHANNEL_CLASS_NAME = "io.netty.channel.kqueue.KQueueServerSocketChannel";
        private Class eventLoopGroupClass = NioEventLoopGroup.class;
        private Class serverSocketChannelClass = NioServerSocketChannel.class;
        private EventLoopGroup bossEventLoopGroup = null;
        private EventLoopGroup workEventLoopGroup = null;
        private boolean mathPlatform = false;
        EventLoopInfo(int bossGroupThreadCount, int workGroupThreadCount) {
            //check for epoll
            checkPlatform(EPOLL_CLASS_NAME, EPOLL_EVENT_LOOP_GROUP_CLASS_NAME, EPOLL_SERVER_SOCKET_CHANNEL_CLASS_NAME);
            //check for kqueue
            checkPlatform(KQUEUE_CLASS_NAME, KQUEUE_EVENT_LOOP_GROUP_CLASS_NAME, KQUEUE_SERVER_SOCKET_CHANNEL_CLASS_NAME);
            createEventLoopInstance(bossGroupThreadCount, workGroupThreadCount);

        }

        private void checkPlatform(String checkClassName, String eventLoopGroupClassName, String serverSocketChannelClassName) {
            if (!mathPlatform) {
                try {
                    Class checkClass = Class.forName(checkClassName);
                    Method isAvailableMethod = checkClass.getMethod("isAvailable");
                    if (isAvailableMethod != null) {
                        Boolean platSupport = (Boolean)isAvailableMethod.invoke(null);
                        if (platSupport) {
                            Class platEventLoopGroupClass = Class.forName(eventLoopGroupClassName);
                            if (platEventLoopGroupClass == null) {
                                return;
                            }
                            Constructor platEventLoopConstructor = platEventLoopGroupClass.getConstructor(int.class);
                            Class platServerSocketChannel = Class.forName(serverSocketChannelClassName);
                            if (platServerSocketChannel != null && platEventLoopConstructor != null) {
                                eventLoopGroupClass = platEventLoopGroupClass;
                                serverSocketChannelClass = platServerSocketChannel;
                                mathPlatform = true;
                            }
                        }
                    }
                } catch (Exception ignored) {

                }
            }
        }

        public void createEventLoopInstance(int bossThreadCount, int workThreadCount) {
            try {
                Constructor constructor = this.eventLoopGroupClass.getConstructor(int.class);
                this.bossEventLoopGroup = (EventLoopGroup)constructor.newInstance(bossThreadCount);
                this.workEventLoopGroup = (EventLoopGroup)constructor.newInstance(workThreadCount);
            } catch (Exception ignored) {
                this.eventLoopGroupClass = NioEventLoopGroup.class;
                this.serverSocketChannelClass = NioServerSocketChannel.class;
                this.bossEventLoopGroup = new NioEventLoopGroup(bossThreadCount);
                this.workEventLoopGroup = new NioEventLoopGroup(workThreadCount);
            }
        }
    }
}
