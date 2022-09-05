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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;

import io.netty.channel.ChannelPromise;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class TokenClientPromiseHolder {

    private static final Map<Integer, TokenPromise> PROMISE_MAP = new ConcurrentHashMap<>();
    private static final int CPU_SIZE = Runtime.getRuntime().availableProcessors();
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(CPU_SIZE, CPU_SIZE, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    public static void putPromise(int xid, TokenPromise promise) {
        PROMISE_MAP.put(xid, promise);
    }

    public static void remove(int xid) {
        PROMISE_MAP.remove(xid);
    }

    public static <T> void completePromise(int xid, ClusterResponse<T> response) {
        final TokenPromise tokenPromise = PROMISE_MAP.get(xid);
        if (tokenPromise == null) {
            // timeout
            return;
        }
        EXECUTOR.submit(() -> {
            try {
                ChannelPromise promise = tokenPromise.getPromiseValue();
                if (promise == null) {
                    return;
                }
                if (promise.isDone() || promise.isCancelled()) {
                    return;
                }
                tokenPromise.setResponseValue(response);
                promise.setSuccess();
            } catch (InterruptedException e) {
            }
        });
    }

    private TokenClientPromiseHolder() {
    }
}
