package com.alibaba.csp.sentinel.cluster.client.handler;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import io.netty.channel.ChannelPromise;

/**
 * @author Shako Xie
 */
public class TokenPromise {

    private final long startTime = System.currentTimeMillis();
    private SynchronousQueue<Object> channel = new SynchronousQueue<>();

    public boolean setPromiseValue(ChannelPromise promise) throws InterruptedException {
        return channel.offer(promise, remaining(), TimeUnit.MILLISECONDS);
    }

    public ClusterResponse getResponseValue() throws InterruptedException {
        Object response = channel.poll(remaining(), TimeUnit.MILLISECONDS);
        if (response != null) {
            return (ClusterResponse) response;
        }
        return null;
    }

    public ChannelPromise getPromiseValue() throws InterruptedException {
        Object promise = channel.poll(remaining(), TimeUnit.MILLISECONDS);
        if (promise != null) {
            return (ChannelPromise) promise;
        }
        return null;
    }

    public boolean setResponseValue(ClusterResponse response) throws InterruptedException {
        return channel.offer(response, remaining(), TimeUnit.MILLISECONDS);
    }

    private long remaining() {
        long remaining = ClusterClientConfigManager.getRequestTimeout() - (System.currentTimeMillis() - startTime);
        return remaining < 0 ? 0 : remaining;
    }
}
