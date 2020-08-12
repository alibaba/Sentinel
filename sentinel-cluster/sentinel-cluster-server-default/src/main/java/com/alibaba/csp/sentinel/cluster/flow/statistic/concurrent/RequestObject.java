package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.data.ConcurrentFlowAcquireRequestData;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yunfeiyanggzq
 */
public class RequestObject {
    long creatTime;
    ChannelHandlerContext ctx;
    String address;
    ClusterRequest<ConcurrentFlowAcquireRequestData> request;

    public RequestObject(ChannelHandlerContext ctx, String address, ClusterRequest<ConcurrentFlowAcquireRequestData> request) {
        this.ctx = ctx;
        this.address = address;
        this.request = request;
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
}
