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
package com.alibaba.csp.sentinel.cluster.flow;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.CurrentConcurrencyManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNode;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNodeManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.queue.BlockRequestWaitQueue;
import com.alibaba.csp.sentinel.cluster.server.log.ClusterServerStatLogUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.HostNameUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yunfeiyanggzq
 */
final public class ConcurrentClusterFlowChecker {

    public static double calcGlobalThreshold(FlowRule rule) {
        double count = rule.getCount();
        switch (rule.getClusterConfig().getThresholdType()) {
            case ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL:
                return count;
            case ClusterRuleConstant.FLOW_THRESHOLD_AVG_LOCAL:
            default:
                int connectedCount = ClusterFlowRuleManager.getConnectedCount(rule.getClusterConfig().getFlowId());
                return count * connectedCount;
        }
    }

    public static TokenResult acquireConcurrentToken(/*@Valid*/ String clientAddress, FlowRule rule, int acquireCount, boolean prioritized) {
        long flowId = rule.getClusterConfig().getFlowId();
        boolean block = false;
        AtomicInteger nowCalls = CurrentConcurrencyManager.get(flowId);
        if (nowCalls == null) {
            RecordLog.warn("[ConcurrentClusterFlowChecker] Fail to get nowCalls by flowId<{}>", flowId);
            return new TokenResult(TokenResultStatus.FAIL);
        }

        // check before enter the lock to improve the efficiency
        if (nowCalls.get() + acquireCount > calcGlobalThreshold(rule)) {
            ClusterServerStatLogUtil.log("concurrent|block|" + flowId, acquireCount);
            return applyBlockResult(clientAddress, rule, acquireCount, prioritized);
        }

        // ensure the atomicity of operations
        // lock different nowCalls to improve the efficiency
        synchronized (nowCalls) {
            // check again whether the request can pass.
            if (nowCalls.get() + acquireCount > calcGlobalThreshold(rule)) {
                ClusterServerStatLogUtil.log("concurrent|block|" + flowId, acquireCount);
                // The purpose is to let the operations give up the lock to avoid deadlock.
                block = true;
            } else {
                nowCalls.getAndAdd(acquireCount);
            }
        }

        if (block) {
            ClusterServerStatLogUtil.log("concurrent|block|" + flowId, acquireCount);
            return applyBlockResult(clientAddress, rule, acquireCount, prioritized);
        }

        ClusterServerStatLogUtil.log("concurrent|pass|" + flowId, acquireCount);
        TokenCacheNode node = TokenCacheNode.generateTokenCacheNode(rule, acquireCount, clientAddress);
        TokenCacheNodeManager.putTokenCacheNode(node.getTokenId(), node);
        TokenResult tokenResult = new TokenResult(TokenResultStatus.OK);
        tokenResult.setTokenId(node.getTokenId());
        return tokenResult;
    }

    public static void releaseConcurrentToken(/*@Valid*/ long tokenId) {
        TokenCacheNode node = TokenCacheNodeManager.getTokenCacheNode(tokenId);
        if (node == null) {
            RecordLog.info("[ConcurrentClusterFlowChecker] Token<{}> is already released", tokenId);
            return;
        }
        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(node.getFlowId());
        if (rule == null) {
            RecordLog.info("[ConcurrentClusterFlowChecker] Fail to get rule by flowId<{}>", node.getFlowId());
            return;
        }
        if (TokenCacheNodeManager.removeTokenCacheNode(tokenId) == null) {
            RecordLog.info("[ConcurrentClusterFlowChecker] Token<{}> is already released for flowId<{}>", tokenId, node.getFlowId());
            return;
        }
        int acquireCount = node.getAcquireCount();
        AtomicInteger nowCalls = CurrentConcurrencyManager.get(node.getFlowId());
        nowCalls.getAndAdd(-1 * acquireCount);
        ClusterServerStatLogUtil.log("concurrent|release|" + rule.getClusterConfig().getFlowId(), acquireCount);
    }

    private static TokenResult applyBlockResult(String clientAddress,/*@Valid*/ FlowRule rule, int acquireCount, boolean prioritized) {
        if (prioritized && clientAddress.equals(HostNameUtil.getIp())
                && rule.getClusterConfig().getAcquireRefuseStrategy() == RuleConstant.QUEUE_BLOCK_STRATEGY) {
            long flowId = rule.getClusterConfig().getFlowId();
            return BlockRequestWaitQueue.addServerRequestToWaitQueue(clientAddress, acquireCount, flowId, true);
        } else {
            return new TokenResult(TokenResultStatus.BLOCKED);
        }
    }
}
