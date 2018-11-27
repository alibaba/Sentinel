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
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterParamMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterParamMetric;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

/**
 * @author Eric Zhao
 */
public final class ClusterParamFlowChecker {

    static TokenResult acquireClusterToken(ParamFlowRule rule, int count, Collection<Object> values) {
        ClusterParamMetric metric = ClusterParamMetricStatistics.getMetric(rule.getClusterConfig().getFlowId());
        if (metric == null) {
            // Unexpected state, return FAIL.
            return new TokenResult(TokenResultStatus.FAIL);
        }
        boolean hasPassed = true;
        Object blockObject = null;
        for (Object value : values) {
            // TODO: origin is int * int, but current double!
            double curCount = metric.getAvg(value);

            double threshold = calcGlobalThreshold(rule);
            if (++curCount > threshold) {
                hasPassed = false;
                blockObject = value;
                break;
            }
        }

        if (hasPassed) {
            for (Object value : values) {
                metric.addValue(value, count);
            }
        } else {
            // TODO: log <blocked object> here?
        }

        return hasPassed ? newRawResponse(TokenResultStatus.OK): newRawResponse(TokenResultStatus.BLOCKED);
    }

    private static TokenResult newRawResponse(int status) {
        return new TokenResult(status)
            .setRemaining(0)
            .setWaitInMs(0);
    }

    private static double calcGlobalThreshold(ParamFlowRule rule) {
        double count = rule.getCount();
        switch (rule.getClusterConfig().getThresholdType()) {
            case ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL:
                return count;
            case ClusterRuleConstant.FLOW_THRESHOLD_AVG_LOCAL:
            default:
                int connectedCount = 1; // TODO: get real connected count grouped.
                return count * connectedCount;
        }
    }

    private ClusterParamFlowChecker() {}
}
