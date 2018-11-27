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

import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterMetricStatistics;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.data.ClusterFlowEvent;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterMetric;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterFlowChecker {

    static TokenResult tryAcquireOrBorrowFromRefResource(FlowRule rule, int acquireCount, boolean prioritized) {
        // 1. First try acquire its own count.

        // TokenResult ownResult = acquireClusterToken(rule, acquireCount, prioritized);
        ClusterMetric metric = ClusterMetricStatistics.getMetric(rule.getClusterConfig().getFlowId());
        if (metric == null) {
            return new TokenResult(TokenResultStatus.FAIL);
        }

        double latestQps = metric.getAvg(ClusterFlowEvent.PASS_REQUEST);
        double globalThreshold = calcGlobalThreshold(rule) * ClusterServerConfigManager.exceedCount;
        double nextRemaining = globalThreshold - latestQps - acquireCount;

        if (nextRemaining >= 0) {
            // TODO: checking logic and metric operation should be separated.
            metric.add(ClusterFlowEvent.PASS, acquireCount);
            metric.add(ClusterFlowEvent.PASS_REQUEST, 1);
            if (prioritized) {
                // Add prioritized pass.
                metric.add(ClusterFlowEvent.OCCUPIED_PASS, acquireCount);
            }
            // Remaining count is cut down to a smaller integer.
            return new TokenResult(TokenResultStatus.OK)
                .setRemaining((int) nextRemaining)
                .setWaitInMs(0);
        }

        if (prioritized) {
            double occupyAvg = metric.getAvg(ClusterFlowEvent.WAITING);
            if (occupyAvg <= ClusterServerConfigManager.maxOccupyRatio * globalThreshold) {
                int waitInMs = metric.tryOccupyNext(ClusterFlowEvent.PASS, acquireCount, globalThreshold);
                if (waitInMs > 0) {
                    return new TokenResult(TokenResultStatus.SHOULD_WAIT)
                        .setRemaining(0)
                        .setWaitInMs(waitInMs);
                }
                // Or else occupy failed, should be blocked.
            }
        }

        // 2. If failed, try to borrow from reference resource.

        // Assume it's valid as checked before.
        if (!ClusterServerConfigManager.borrowRefEnabled) {
            return new TokenResult(TokenResultStatus.NOT_AVAILABLE);
        }
        Long refFlowId = rule.getClusterConfig().getRefFlowId();
        FlowRule refFlowRule = ClusterFlowRuleManager.getFlowRuleById(refFlowId);
        if (refFlowRule == null) {
            return new TokenResult(TokenResultStatus.NO_REF_RULE_EXISTS);
        }
        // TODO: check here

        ClusterMetric refMetric = ClusterMetricStatistics.getMetric(refFlowId);
        if (refMetric == null) {
            return new TokenResult(TokenResultStatus.FAIL);
        }
        double refOrders = refMetric.getAvg(ClusterFlowEvent.PASS);
        double refQps = refMetric.getAvg(ClusterFlowEvent.PASS_REQUEST);

        double splitRatio = refQps > 0 ? refOrders / refQps : 1;

        double selfGlobalThreshold = ClusterServerConfigManager.exceedCount * calcGlobalThreshold(rule);
        double refGlobalThreshold = ClusterServerConfigManager.exceedCount * calcGlobalThreshold(refFlowRule);

        long currentTime = TimeUtil.currentTimeMillis();
        long latestRefTime = 0 /*refFlowRule.clusterQps.getStableWindowStartTime()*/;
        int sampleCount = 10;

        if (currentTime > latestRefTime
            && (refOrders / refGlobalThreshold + 1.0d / sampleCount >= ((double)(currentTime - latestRefTime)) / 1000)
            || refOrders == refGlobalThreshold) {
            return blockedResult();
        }

        // double latestQps = metric.getAvg(ClusterFlowEvent.PASS);
        double refRatio = rule.getClusterConfig().getRefRatio();

        if (refOrders / splitRatio + (acquireCount + latestQps) * refRatio
            <= refGlobalThreshold / splitRatio + selfGlobalThreshold * refRatio) {
            metric.add(ClusterFlowEvent.PASS, acquireCount);
            metric.add(ClusterFlowEvent.PASS_REQUEST, 1);

            return new TokenResult(TokenResultStatus.OK);
        }

        // TODO: log here?
        metric.add(ClusterFlowEvent.BLOCK, acquireCount);

        return blockedResult();
    }

    private static double calcGlobalThreshold(FlowRule rule) {
        double count = rule.getCount();
        switch (rule.getClusterConfig().getThresholdType()) {
            case ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL:
                return count;
            case ClusterRuleConstant.FLOW_THRESHOLD_AVG_LOCAL:
            default:
                // TODO: get real connected count grouped.
                int connectedCount = 1;
                return count * connectedCount;
        }
    }

    static TokenResult acquireClusterToken(/*@Valid*/ FlowRule rule, int acquireCount, boolean prioritized) {
        ClusterMetric metric = ClusterMetricStatistics.getMetric(rule.getClusterConfig().getFlowId());
        if (metric == null) {
            return new TokenResult(TokenResultStatus.FAIL);
        }

        double latestQps = metric.getAvg(ClusterFlowEvent.PASS_REQUEST);
        double globalThreshold = calcGlobalThreshold(rule) * ClusterServerConfigManager.exceedCount;
        double nextRemaining = globalThreshold - latestQps - acquireCount;

        if (nextRemaining >= 0) {
            // TODO: checking logic and metric operation should be separated.
            metric.add(ClusterFlowEvent.PASS, acquireCount);
            metric.add(ClusterFlowEvent.PASS_REQUEST, 1);
            if (prioritized) {
                // Add prioritized pass.
                metric.add(ClusterFlowEvent.OCCUPIED_PASS, acquireCount);
            }
            // Remaining count is cut down to a smaller integer.
            return new TokenResult(TokenResultStatus.OK)
                .setRemaining((int) nextRemaining)
                .setWaitInMs(0);
        } else {
            if (prioritized) {
                double occupyAvg = metric.getAvg(ClusterFlowEvent.WAITING);
                if (occupyAvg <= ClusterServerConfigManager.maxOccupyRatio * globalThreshold) {
                    int waitInMs = metric.tryOccupyNext(ClusterFlowEvent.PASS, acquireCount, globalThreshold);
                    if (waitInMs > 0) {
                        return new TokenResult(TokenResultStatus.SHOULD_WAIT)
                            .setRemaining(0)
                            .setWaitInMs(waitInMs);
                    }
                    // Or else occupy failed, should be blocked.
                }
            }
            // Blocked.
            metric.add(ClusterFlowEvent.BLOCK, acquireCount);
            metric.add(ClusterFlowEvent.BLOCK_REQUEST, 1);
            if (prioritized) {
                // Add prioritized block.
                metric.add(ClusterFlowEvent.OCCUPIED_BLOCK, acquireCount);
            }

            return blockedResult();
        }
    }

    private static TokenResult blockedResult() {
        return new TokenResult(TokenResultStatus.BLOCKED)
            .setRemaining(0)
            .setWaitInMs(0);
    }

    private ClusterFlowChecker() {}
}
