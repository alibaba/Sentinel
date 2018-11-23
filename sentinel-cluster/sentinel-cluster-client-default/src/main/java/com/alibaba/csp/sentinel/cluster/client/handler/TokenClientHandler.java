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
package com.alibaba.csp.sentinel.cluster.client.handler;

import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.cluster.client.ClientConstants;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class TokenClientHandler extends ChannelInboundHandlerAdapter {

    private final AtomicInteger currentState = new AtomicInteger(ClientConstants.CLIENT_STATUS_OFF);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        currentState.set(ClientConstants.CLIENT_STATUS_STARTED);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(String.format("[%s] Client message recv: %s", System.currentTimeMillis(), msg));
        if (msg instanceof ClusterResponse) {
            ClusterResponse<?> response = (ClusterResponse) msg;

            TokenClientPromiseHolder.completePromise(response.getId(), response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        currentState.set(ClientConstants.CLIENT_STATUS_OFF);
    }

    public int getCurrentState() {
        return currentState.get();
    }

    public boolean hasStarted() {
        return getCurrentState() == ClientConstants.CLIENT_STATUS_STARTED;
    }
}
