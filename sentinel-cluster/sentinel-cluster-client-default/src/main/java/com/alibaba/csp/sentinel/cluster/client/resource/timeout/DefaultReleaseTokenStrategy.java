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
package com.alibaba.csp.sentinel.cluster.client.resource.timeout;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenService;
import com.alibaba.csp.sentinel.cluster.client.TokenClientProvider;
import com.alibaba.csp.sentinel.cluster.server.EmbeddedClusterTokenServerProvider;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.ReleaseTokenStrategy;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author yunfeiyanggzq
 */
public class DefaultReleaseTokenStrategy implements ReleaseTokenStrategy {

    /**
     * the map to store the {@link Timeout}, if the {@link Timeout} is null, the ResourceTimeoutStrategy is RuleConstant.DEFAULT_RESOURCE_TIMEOUT_STRATEGY.
     */
    private final static ConcurrentHashMap<Long, Timeout> TimeoutMap = new ConcurrentHashMap<>();

    @Override
    public void doWithResourceTimeoutToken(TokenResult result, final FlowRule rule, Context context, DefaultNode node,
                                           int acquireCount, boolean prioritized) {
        Entry entry = context.getCurEntry();
        if (entry == null) {
            return;
        }
        Timeout timeout = null;
        final long tokenId = result.getTokenId();
        if (rule.getClusterConfig().getResourceTimeoutStrategy() != RuleConstant.DEFAULT_RESOURCE_TIMEOUT_STRATEGY) {
            timeout = TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) {
                    releaseToken(tokenId);
                }
            }, rule.getClusterConfig().getResourceTimeout(), TimeUnit.MILLISECONDS);
        }
        if (entry.getTokenId() != 0) {
            releaseTokenWhenExitSlot(context);
        }
        entry.setTokenId(tokenId);
        putTimeout(tokenId, timeout);
    }

    @Override
    public void releaseTokenWhenExitSlot(Context context) {
        Entry entry = context.getCurEntry();
        if (entry == null) {
            return;
        }
        long tokenId = entry.getTokenId();
        if (tokenId == 0) {
            // there is no cluster flow control or failed to get the tokenId.
            return;
        }
        Timeout timeout = getTimeout(tokenId);
        if (timeout == null) {
            // this flow choose to  release the timeout token in the server instead of the client.
            releaseToken(tokenId);
            return;
        }
        if (timeout.isCancelled() || timeout.isExpired()) {
            // the token has been released by client because of resource timeout.
            clearByTokenId(tokenId);
            return;
        }
        // release the token and clear the timeout cache.
        releaseToken(tokenId);
        timeout.cancel();
        clearByTokenId(tokenId);
    }


    private void releaseToken(long tokenId) {
        TokenService clusterService = pickClusterService();
        if (clusterService == null) {
            return;
        }
        clusterService.releaseConcurrentToken(tokenId);
    }

    private static TokenService pickClusterService() {
        if (ClusterStateManager.isClient()) {
            return TokenClientProvider.getClient();
        }
        if (ClusterStateManager.isServer()) {
            return EmbeddedClusterTokenServerProvider.getServer();
        }
        return null;
    }


    private void putTimeout(Long tokenId, Timeout timeout) {
        if (timeout != null) {
            TimeoutMap.put(tokenId, timeout);
        }
    }

    private Timeout getTimeout(Long tokenId) {
        return TimeoutMap.get(tokenId);
    }

    private void removeTimeout(Long tokenId) {
        TimeoutMap.remove(tokenId);
    }

    private void clearByTokenId(Long tokenId) {
        if (tokenId == null) {
            return;
        }
        removeTimeout(tokenId);
    }
}

