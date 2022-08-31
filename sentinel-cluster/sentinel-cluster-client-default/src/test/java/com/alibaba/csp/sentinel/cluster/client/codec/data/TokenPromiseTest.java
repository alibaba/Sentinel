package com.alibaba.csp.sentinel.cluster.client.codec.data;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.client.handler.TokenPromise;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author Shako Xie
 */
public class TokenPromiseTest {

    @Test
    public void normalTest() throws InterruptedException {
        final TokenPromise promise = new TokenPromise();
        final AtomicBoolean providerResult = new AtomicBoolean(false);
        final AtomicBoolean consumerResult = new AtomicBoolean(false);
        Thread provider = genProvider(promise, providerResult);
        Thread consumer = genConsumer(promise, consumerResult);
        provider.start();
        // remote cost, TP99:3ms
        Thread.sleep(3);
        consumer.start();
        provider.join();
        consumer.join();
        Assert.assertTrue(providerResult.get() && consumerResult.get());
    }

    @Test
    public void FastConsumerTest() throws InterruptedException {
        final TokenPromise promise = new TokenPromise();
        final AtomicBoolean providerResult = new AtomicBoolean(false);
        final AtomicBoolean consumerResult = new AtomicBoolean(false);
        Thread provider = genProvider(promise, providerResult);
        Thread consumer = genConsumer(promise, consumerResult);
        // remote cost, 0ms
        consumer.start();
        provider.start();
        provider.join();
        consumer.join();
        Assert.assertTrue(providerResult.get() && consumerResult.get());
    }

    @Test
    public void timeoutTest() throws InterruptedException {
        final TokenPromise promise = new TokenPromise();
        final AtomicBoolean providerResult = new AtomicBoolean(false);
        final AtomicBoolean consumerResult = new AtomicBoolean(false);
        Thread provider = genProvider(promise, providerResult);
        Thread consumer = genConsumer(promise, consumerResult);
        provider.start();
        // remote cost and timeout
        Thread.sleep(ClusterClientConfigManager.getRequestTimeout());
        consumer.start();
        provider.join();
        consumer.join();
        Assert.assertTrue(!providerResult.get());
    }

    private Thread genProvider(TokenPromise promise, AtomicBoolean ret) {
        return new Thread(() -> {
            try {
                if (promise.setPromiseValue(new DefaultChannelPromise(new NioSocketChannel()))) {
                    ret.set(true);
                } else {
                    return;
                }
                Thread.sleep(ThreadLocalRandom.current().nextInt(5));
                if (null != promise.getResponseValue()) {
                    ret.set(true);
                }
            } catch (Exception e) {
            }
        });
    }

    private Thread genConsumer(TokenPromise promise, AtomicBoolean ret) {
        return new Thread(() -> {
            try {
                if (null != promise.getPromiseValue()) {
                    ret.set(true);
                } else {
                    return;
                }
                Thread.sleep(ThreadLocalRandom.current().nextInt(5));
                if (promise.setResponseValue(new ClusterResponse<>())) {
                    ret.set(true);
                }
            } catch (InterruptedException e) {
            }
        });
    }
}
