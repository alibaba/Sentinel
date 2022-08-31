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

import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;

import io.netty.channel.ChannelPromise;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class TokenClientPromiseHolder {

    private static final Map<Integer, TokenPromise> PROMISE_MAP = new ConcurrentHashMap<>();

    public static void putPromise(int xid, TokenPromise promise) {
        PROMISE_MAP.put(xid, promise);
    }

    public static void remove(int xid) {
        PROMISE_MAP.remove(xid);
    }

    public static <T> boolean completePromise(int xid, ClusterResponse<T> response) throws InterruptedException {
        TokenPromise tokenPromise = PROMISE_MAP.get(xid);
        if (tokenPromise == null) {
            // should not reach here
            return false;
        }
        ChannelPromise promise = tokenPromise.getPromiseValue();
        if (promise == null) {
            return false;
        }
        if (promise.isDone() || promise.isCancelled()) {
            return false;
        }
        promise.setSuccess();
        return tokenPromise.setResponseValue(response);
    }

    private TokenClientPromiseHolder() {
    }
}
