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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.queue;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.data.ConcurrentFlowAcquireRequestData;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yunfeiyanggzq
 */
public class RequestInfoEntity {
    /**
     * the time this entity created.
     */
    private long creatTime;
    /**
     * the {@link ChannelHandlerContext} of this request.
     */
    private ChannelHandlerContext ctx;
    /**
     * the client address of this client.
     */
    private String address;
    /**
     * the {@link ClusterRequest} data of this request.
     */
    private ClusterRequest<ConcurrentFlowAcquireRequestData> request;
    /**
     * the mark whether this request is from server.
     */
    private boolean isServerRequest = false;
    /**
     * the {@link RequestFuture} of the result.
     */
    private RequestFuture<TokenResult> future;

    public RequestInfoEntity(ChannelHandlerContext ctx, String address, ClusterRequest<ConcurrentFlowAcquireRequestData> request) {
        this.ctx = ctx;
        this.address = address;
        this.request = request;
        this.creatTime = System.currentTimeMillis();
    }

    public RequestInfoEntity(String address, ClusterRequest<ConcurrentFlowAcquireRequestData> request, RequestFuture<TokenResult> future) {
        this.address = address;
        this.request = request;
        this.future = future;
        this.isServerRequest = true;
        this.creatTime = System.currentTimeMillis();
    }


    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ClusterRequest<ConcurrentFlowAcquireRequestData> getRequest() {
        return request;
    }

    public void setRequest(ClusterRequest<ConcurrentFlowAcquireRequestData> request) {
        this.request = request;
    }

    public long getCreatTime() {
        return creatTime;
    }

    public boolean isServerRequest() {
        return isServerRequest;
    }

    public void setServerRequest(boolean serverRequest) {
        isServerRequest = serverRequest;
    }

    public RequestFuture getFuture() {
        return future;
    }

    public void setFuture(RequestFuture future) {
        this.future = future;
    }
}
