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

import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.expire.ExpireStrategy;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.expire.RegularExpireStrategy;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

/**
 * @author yunfeiyanggzq
 */
public class TokenCacheNodeManager {
    private static ConcurrentLinkedHashMap<Long, TokenCacheNode> TOKEN_CACHE_NODE_MAP;
    /**
     * the strategy of removing expired token
     */
    private static ExpireStrategy expireStrategy;


    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    private static final int DEFAULT_CAPACITY = Integer.MAX_VALUE;

    static {
        ExpireStrategy expireStrategy = new RegularExpireStrategy();
        prepare(DEFAULT_CONCURRENCY_LEVEL, DEFAULT_CAPACITY, expireStrategy);
    }

    public static void prepare(int concurrencyLevel, int maximumWeightedCapacity, ExpireStrategy strategy) {
        AssertUtil.isTrue(concurrencyLevel > 0, "concurrencyLevel must be positive");
        AssertUtil.isTrue(maximumWeightedCapacity > 0, "maximumWeightedCapacity must be positive");
        AssertUtil.isTrue(strategy != null, "expireStrategy can;t be null");

        TOKEN_CACHE_NODE_MAP = new ConcurrentLinkedHashMap.Builder<Long, TokenCacheNode>()
                .concurrencyLevel(concurrencyLevel)
                .maximumWeightedCapacity(maximumWeightedCapacity)
                .weigher(Weighers.singleton())
                .build();
        // Start the task of regularly clearing expired keys
        expireStrategy = strategy;
        expireStrategy.removeExpireKey(TOKEN_CACHE_NODE_MAP);
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

    public static int getSize() {
        return TOKEN_CACHE_NODE_MAP.size();
    }

    public static ConcurrentLinkedHashMap<Long, TokenCacheNode> getCache() {
        return TOKEN_CACHE_NODE_MAP;
    }

    public static boolean validToken(TokenCacheNode cacheNode) {
        return cacheNode.getTokenId() != null && cacheNode.getFlowId() != null && cacheNode.getClientTimeout() >= 0 && cacheNode.getResourceTimeout() >= 0;
    }
}
