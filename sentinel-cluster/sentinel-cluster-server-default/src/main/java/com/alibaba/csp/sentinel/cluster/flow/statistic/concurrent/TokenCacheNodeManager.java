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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.expire.RegularExpireStrategy;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;


import java.util.Set;

/**
 * @author yunfeiyanggzq
 */
public class TokenCacheNodeManager {
    private static ConcurrentLinkedHashMapWrapper<Long, TokenCacheNode> TOKEN_CACHE_NODE_MAP;

    private static final int DEFAULT_CAPACITY = Integer.MAX_VALUE;

    static {
        prepare(DEFAULT_CAPACITY);
    }

    public static void prepare(int maximumWeightedCapacity) {
        AssertUtil.isTrue(maximumWeightedCapacity > 0, "maximumWeightedCapacity must be positive");

        Cache<Long, TokenCacheNode> map = Caffeine.newBuilder()
                .maximumWeight(maximumWeightedCapacity)
                .weigher(Weigher.singletonWeigher())
                .build();
        TOKEN_CACHE_NODE_MAP = new ConcurrentLinkedHashMapWrapper(map);
        // Start the task of regularly clearing expired keys
        RegularExpireStrategy strategy = new RegularExpireStrategy(TOKEN_CACHE_NODE_MAP);
        strategy.startClearTaskRegularly();
    }


    public static TokenCacheNode getTokenCacheNode(long tokenId) {
        return TOKEN_CACHE_NODE_MAP.get(tokenId);
    }

    public static void putTokenCacheNode(long tokenId, TokenCacheNode cacheNode) {
        TOKEN_CACHE_NODE_MAP.put(tokenId, cacheNode);
    }

    public static boolean isContainsTokenId(long tokenId) {
        return TOKEN_CACHE_NODE_MAP.containsKey(tokenId);
    }

    public static TokenCacheNode removeTokenCacheNode(long tokenId) {
        return TOKEN_CACHE_NODE_MAP.remove(tokenId);
    }

    public static long getSize() {
        return TOKEN_CACHE_NODE_MAP.size();
    }

    public static Set<Long> getCacheKeySet() {
        return TOKEN_CACHE_NODE_MAP.keySet();
    }

    public static boolean validToken(TokenCacheNode cacheNode) {
        return cacheNode.getTokenId() != null && cacheNode.getFlowId() != null && cacheNode.getClientTimeout() >= 0 && cacheNode.getResourceTimeout() >= 0;
    }
}
