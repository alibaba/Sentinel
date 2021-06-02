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

import java.util.Collection;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterParamMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.limit.GlobalRequestLimiter;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterParamMetric;
import com.alibaba.csp.sentinel.cluster.server.log.ClusterServerStatLogUtil;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterParamFlowChecker {

    static boolean allowProceed(long flowId) {
        String namespace = ClusterParamFlowRuleManager.getNamespace(flowId);
        return GlobalRequestLimiter.tryPass(namespace);
    }

    static TokenResult acquireClusterToken(ParamFlowRule rule, int count, Collection<Object> values) {
        Long id = rule.getClusterConfig().getFlowId();

        if (!allowProceed(id)) {
            return new TokenResult(TokenResultStatus.TOO_MANY_REQUEST);
        }

        ClusterParamMetric metric = ClusterParamMetricStatistics.getMetric(id);
        if (metric == null) {
            // Unexpected state, return FAIL.
            return new TokenResult(TokenResultStatus.FAIL);
        }
        if (values == null || values.isEmpty()) {
            // Empty parameter list will always pass.
            return new TokenResult(TokenResultStatus.OK);
        }
        double remaining = -1;
        boolean hasPassed = true;
        Object blockObject = null;
        for (Object value : values) {
            double latestQps = metric.getAvg(value);
            double threshold = calcGlobalThreshold(rule, value);
            double nextRemaining = threshold - latestQps - count;
            remaining = nextRemaining;
            if (nextRemaining < 0) {
                hasPassed = false;
                blockObject = value;
                break;
            }
        }

        if (hasPassed) {
            for (Object value : values) {
                metric.addValue(value, count);
            }
            ClusterServerStatLogUtil.log(String.format("param|pass|%d", id));
        } else {
            ClusterServerStatLogUtil.log(String.format("param|block|%d|%s", id, blockObject));
        }
        if (values.size() > 1) {
            // Remaining field is unsupported for multi-values.
            remaining = -1;
        }

        return hasPassed ? newPassResponse((int)remaining): newBlockResponse();
    }

    private static TokenResult newPassResponse(int remaining) {
        return new TokenResult(TokenResultStatus.OK)
            .setRemaining(remaining)
            .setWaitInMs(0);
    }

    private static TokenResult newBlockResponse() {
        return new TokenResult(TokenResultStatus.BLOCKED)
            .setRemaining(0)
            .setWaitInMs(0);
    }

    private static double calcGlobalThreshold(ParamFlowRule rule, Object value) {
        double count = getRawThreshold(rule, value);
        switch (rule.getClusterConfig().getThresholdType()) {
            case ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL:
                return count;
            case ClusterRuleConstant.FLOW_THRESHOLD_AVG_LOCAL:
            default:
                int connectedCount = ClusterParamFlowRuleManager.getConnectedCount(rule.getClusterConfig().getFlowId());
                return count * connectedCount;
        }
    }

    private static double getRawThreshold(ParamFlowRule rule, Object value) {
        Integer itemCount = rule.retrieveExclusiveItemCount(value);
        if (itemCount == null) {
            return rule.getCount();
        } else {
            return itemCount;
        }
    }

    private ClusterParamFlowChecker() {}
}
