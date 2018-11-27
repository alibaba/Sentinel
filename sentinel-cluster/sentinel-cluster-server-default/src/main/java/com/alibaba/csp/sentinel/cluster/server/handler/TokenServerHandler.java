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
package com.alibaba.csp.sentinel.cluster.server.handler;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.response.data.FlowTokenResponseData;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionPool;
import com.alibaba.csp.sentinel.cluster.server.processor.RequestProcessor;
import com.alibaba.csp.sentinel.cluster.server.processor.RequestProcessorRegistry;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Netty server handler for Sentinel token server.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class TokenServerHandler extends ChannelInboundHandlerAdapter {

    private final ConnectionPool connectionPool;

    public TokenServerHandler(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[TokenServerHandler] Connection established");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[TokenServerHandler] Connection inactive");
        super.channelInactive(ctx);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        connectionPool.refreshLastReadTime(ctx.channel());
        System.out.println(String.format("[%s] Server message recv: %s", System.currentTimeMillis(), msg));
        if (msg instanceof ClusterRequest) {
            ClusterRequest request = (ClusterRequest)msg;

            RequestProcessor<?, ?> processor = RequestProcessorRegistry.getProcessor(request.getType());
            if (processor == null) {
                System.out.println("[TokenServerHandler] No processor for request type: " + request.getType());
                writeNoProcessorResponse(ctx, request);
            } else {
                ClusterResponse<?> response = processor.processRequest(request);
                writeResponse(ctx, response);
            }
        }
    }

    private void writeNoProcessorResponse(ChannelHandlerContext ctx, ClusterRequest request) {
        ClusterResponse<?> response = new ClusterResponse<>(request.getId(), request.getType(),
            ClusterConstants.RESPONSE_STATUS_BAD, null);
        writeResponse(ctx, response);
    }

    private void writeResponse(ChannelHandlerContext ctx, ClusterResponse response) {
        ctx.writeAndFlush(response);
    }
}
