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
package com.alibaba.csp.sentinel.cluster.client;

import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.cluster.ClusterErrorMessages;
import com.alibaba.csp.sentinel.cluster.ClusterTransportClient;
import com.alibaba.csp.sentinel.cluster.client.codec.netty.NettyRequestEncoder;
import com.alibaba.csp.sentinel.cluster.client.codec.netty.NettyResponseDecoder;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.client.handler.TokenClientHandler;
import com.alibaba.csp.sentinel.cluster.client.handler.TokenClientPromiseHolder;
import com.alibaba.csp.sentinel.cluster.exception.SentinelClusterException;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.Request;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Netty transport client implementation for Sentinel cluster transport.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class NettyTransportClient implements ClusterTransportClient {

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1,
        new NamedThreadFactory("sentinel-cluster-transport-client-scheduler"));

    public static final int RECONNECT_DELAY_MS = 2000;

    private final String host;
    private final int port;

    private Channel channel;
    private NioEventLoopGroup eventLoopGroup;
    private TokenClientHandler clientHandler;

    private final AtomicInteger idGenerator = new AtomicInteger(0);
    private final AtomicInteger currentState = new AtomicInteger(ClientConstants.CLIENT_STATUS_OFF);
    private final AtomicInteger failConnectedTime = new AtomicInteger(0);

    private final AtomicBoolean shouldRetry = new AtomicBoolean(true);

    public NettyTransportClient(String host, int port) {
        AssertUtil.assertNotBlank(host, "remote host cannot be blank");
        AssertUtil.isTrue(port > 0, "port should be positive");
        this.host = host;
        this.port = port;
    }

    private Bootstrap initClientBootstrap() {
        Bootstrap b = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        b.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ClusterClientConfigManager.getConnectTimeout())
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    clientHandler = new TokenClientHandler(currentState, disconnectCallback);

                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
                    pipeline.addLast(new NettyResponseDecoder());
                    pipeline.addLast(new LengthFieldPrepender(2));
                    pipeline.addLast(new NettyRequestEncoder());
                    pipeline.addLast(clientHandler);
                }
            });

        return b;
    }

    private void connect(Bootstrap b) {
        if (currentState.compareAndSet(ClientConstants.CLIENT_STATUS_OFF, ClientConstants.CLIENT_STATUS_PENDING)) {
            b.connect(host, port)
                .addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.cause() != null) {
                        RecordLog.warn(
                            String.format("[NettyTransportClient] Could not connect to <%s:%d> after %d times",
                                host, port, failConnectedTime.get()), future.cause());
                        failConnectedTime.incrementAndGet();
                        channel = null;
                    } else {
                        failConnectedTime.set(0);
                        channel = future.channel();
                        RecordLog.info("[NettyTransportClient] Successfully connect to server <{}:{}>", host, port);
                    }
                }
            });
        }
    }

    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            if (!shouldRetry.get()) {
                return;
            }
            SCHEDULER.schedule(new Runnable() {
                @Override
                public void run() {
                    if (shouldRetry.get()) {
                        RecordLog.info("[NettyTransportClient] Reconnecting to server <{}:{}>", host, port);
                        try {
                            startInternal();
                        } catch (Exception e) {
                            RecordLog.warn("[NettyTransportClient] Failed to reconnect to server", e);
                        }
                    }
                }
            }, RECONNECT_DELAY_MS * (failConnectedTime.get() + 1), TimeUnit.MILLISECONDS);
            cleanUp();
        }
    };

    @Override
    public void start() throws Exception {
        shouldRetry.set(true);
        startInternal();
    }

    private void startInternal() {
        connect(initClientBootstrap());
    }

    private void cleanUp() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() throws Exception {
        // Stop retrying for connection.
        shouldRetry.set(false);

        while (currentState.get() == ClientConstants.CLIENT_STATUS_PENDING) {
            try {
                Thread.sleep(200);
            } catch (Exception ex) {
                // Ignore.
            }
        }

        cleanUp();
        failConnectedTime.set(0);

        RecordLog.info("[NettyTransportClient] Cluster transport client stopped");
    }

    private boolean validRequest(Request request) {
        return request != null && request.getType() >= 0;
    }

    @Override
    public boolean isReady() {
        return channel != null && clientHandler != null && clientHandler.hasStarted();
    }

    @Override
    public ClusterResponse sendRequest(ClusterRequest request) throws Exception {
        if (!isReady()) {
            throw new SentinelClusterException(ClusterErrorMessages.CLIENT_NOT_READY);
        }
        if (!validRequest(request)) {
            throw new SentinelClusterException(ClusterErrorMessages.BAD_REQUEST);
        }
        int xid = getCurrentId();
        try {
            request.setId(xid);

            channel.writeAndFlush(request);

            ChannelPromise promise = channel.newPromise();
            TokenClientPromiseHolder.putPromise(xid, promise);

            if (!promise.await(ClusterClientConfigManager.getRequestTimeout())) {
                throw new SentinelClusterException(ClusterErrorMessages.REQUEST_TIME_OUT);
            }

            SimpleEntry<ChannelPromise, ClusterResponse> entry = TokenClientPromiseHolder.getEntry(xid);
            if (entry == null || entry.getValue() == null) {
                // Should not go through here.
                throw new SentinelClusterException(ClusterErrorMessages.UNEXPECTED_STATUS);
            }
            return entry.getValue();
        } finally {
            TokenClientPromiseHolder.remove(xid);
        }
    }

    private int getCurrentId() {
        int pre, next;
        do {
            pre = idGenerator.get();
            next = pre >= MAX_ID ? MIN_ID : pre + 1;
        } while (!idGenerator.compareAndSet(pre, next));
        return next;
    }

    /*public CompletableFuture<ClusterResponse> sendRequestAsync(ClusterRequest request) {
        // Uncomment this when min target JDK is 1.8.
        if (!validRequest(request)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Bad request"));
        }
        int xid = getCurrentId();
        request.setId(xid);

        CompletableFuture<ClusterResponse> future = new CompletableFuture<>();
        channel.writeAndFlush(request)
            .addListener(f -> {
                if (f.isSuccess()) {
                    future.complete(someResult);
                } else if (f.cause() != null) {
                    future.completeExceptionally(f.cause());
                } else {
                    future.cancel(false);
                }
            });
        return future;
    }*/

    private static final int MIN_ID = 1;
    private static final int MAX_ID = 999_999_999;
}
