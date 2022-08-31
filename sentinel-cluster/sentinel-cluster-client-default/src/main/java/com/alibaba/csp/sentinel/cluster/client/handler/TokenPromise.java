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

    private volatile long waiting = ClusterClientConfigManager.getRequestTimeout();
    private SynchronousQueue<Object> channel = new SynchronousQueue<>();

    public boolean setPromiseValue(ChannelPromise promise) throws InterruptedException {
        long nanoTime = System.nanoTime();
        boolean offered = channel.offer(promise, this.waiting, TimeUnit.MILLISECONDS);
        if (!offered) {
            this.waiting = 0;
        } else {
            this.waiting -= TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoTime);
        }
        return offered;
    }

    public ClusterResponse getResponseValue() throws InterruptedException {
        if (this.waiting <= 0) {
            return null;
        }
        Object promise = channel.poll(this.waiting, TimeUnit.MILLISECONDS);
        if (promise != null) {
            return (ClusterResponse) promise;
        }
        return null;
    }

    public ChannelPromise getPromiseValue() throws InterruptedException {
        if (this.waiting <= 0) {
            return null;
        }
        return (ChannelPromise) channel.poll(this.waiting, TimeUnit.MILLISECONDS);
    }

    public boolean setResponseValue(ClusterResponse response) throws InterruptedException {
        return channel.offer(response, this.waiting, TimeUnit.MILLISECONDS);
    }

}
