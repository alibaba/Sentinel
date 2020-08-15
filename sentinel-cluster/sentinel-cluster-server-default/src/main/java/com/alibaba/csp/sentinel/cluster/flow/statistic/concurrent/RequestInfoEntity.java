package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.request.Request;
import com.alibaba.csp.sentinel.cluster.request.data.ConcurrentFlowAcquireRequestData;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yunfeiyanggzq
 */
public class RequestInfoEntity {
    private long creatTime;
    private ChannelHandlerContext ctx;
    private String address;
    private ClusterRequest<ConcurrentFlowAcquireRequestData> request;
    private boolean isServerRequest;
    private RequestFuture future;

    public RequestInfoEntity(ChannelHandlerContext ctx, String address, ClusterRequest<ConcurrentFlowAcquireRequestData> request) {
        this.ctx = ctx;
        this.address = address;
        this.request = request;
        this.creatTime = System.currentTimeMillis();
    }

    public RequestInfoEntity( String address, ClusterRequest<ConcurrentFlowAcquireRequestData> request,RequestFuture future) {
        this.address = address;
        this.request = request;
        this.future=future;
        this.isServerRequest=true;
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
