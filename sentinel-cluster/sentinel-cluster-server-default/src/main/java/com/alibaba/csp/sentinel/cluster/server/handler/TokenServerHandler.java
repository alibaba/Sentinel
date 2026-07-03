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

import java.net.InetSocketAddress;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionManager;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionPool;
import com.alibaba.csp.sentinel.cluster.server.processor.RequestProcessor;
import com.alibaba.csp.sentinel.cluster.server.processor.RequestProcessorProvider;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Netty server handler for Sentinel token server.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class TokenServerHandler extends ChannelInboundHandlerAdapter {

    private final ConnectionPool globalConnectionPool;

    public TokenServerHandler(ConnectionPool globalConnectionPool) {
        this.globalConnectionPool = globalConnectionPool;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        globalConnectionPool.createConnection(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddress = getRemoteAddress(ctx);
        globalConnectionPool.remove(ctx.channel());
        ConnectionManager.removeConnection(remoteAddress);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        globalConnectionPool.refreshLastReadTime(ctx.channel());
        if (msg instanceof ClusterRequest) {
            ClusterRequest request = (ClusterRequest)msg;

            // Client ping with its namespace, add to connection manager.
            if (request.getType() == ClusterConstants.MSG_TYPE_PING) {
                handlePingRequest(ctx, request);
                return;
            }

            // Pick request processor for request type.
            RequestProcessor<?, ?> processor = RequestProcessorProvider.getProcessor(request.getType());
            if (processor == null) {
                RecordLog.warn("[TokenServerHandler] No processor for request type: " + request.getType());
                writeBadResponse(ctx, request);
            } else {
                ClusterResponse<?> response = processor.processRequest(request);
                writeResponse(ctx, response);
            }
        }
    }

    private void writeBadResponse(ChannelHandlerContext ctx, ClusterRequest request) {
        ClusterResponse<?> response = new ClusterResponse<>(request.getId(), request.getType(),
            ClusterConstants.RESPONSE_STATUS_BAD, null);
        writeResponse(ctx, response);
    }

    private void writeResponse(ChannelHandlerContext ctx, ClusterResponse response) {
        ctx.writeAndFlush(response);
    }

    private void handlePingRequest(ChannelHandlerContext ctx, ClusterRequest request) {
        if (request.getData() == null || StringUtil.isBlank((String)request.getData())) {
            writeBadResponse(ctx, request);
            return;
        }
        String namespace = (String)request.getData();
        String clientAddress = getRemoteAddress(ctx);
        // Add the remote namespace to connection manager.
        int curCount = ConnectionManager.addConnection(namespace, clientAddress).getConnectedCount();
        int status = ClusterConstants.RESPONSE_STATUS_OK;
        ClusterResponse<Integer> response = new ClusterResponse<>(request.getId(), request.getType(), status, curCount);
        writeResponse(ctx, response);
    }

    private String getRemoteAddress(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() == null) {
            return null;
        }
        InetSocketAddress inetAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        return inetAddress.getAddress().getHostAddress() + ":" + inetAddress.getPort();
    }
}
