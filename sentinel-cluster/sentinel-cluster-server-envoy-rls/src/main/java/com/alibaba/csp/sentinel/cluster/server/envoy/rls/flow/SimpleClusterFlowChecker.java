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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls.flow;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.data.ClusterFlowEvent;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterMetric;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.log.ClusterServerStatLogUtil;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public final class SimpleClusterFlowChecker {

    public static TokenResult acquireClusterToken(/*@Valid*/ FlowRule rule, int acquireCount) {
        Long id = rule.getClusterConfig().getFlowId();

        ClusterMetric metric = ClusterMetricStatistics.getMetric(id);
        if (metric == null) {
            return new TokenResult(TokenResultStatus.FAIL);
        }

        double latestQps = metric.getAvg(ClusterFlowEvent.PASS);
        double globalThreshold = rule.getCount() * ClusterServerConfigManager.getExceedCount();
        double nextRemaining = globalThreshold - latestQps - acquireCount;

        if (nextRemaining >= 0) {
            metric.add(ClusterFlowEvent.PASS, acquireCount);
            metric.add(ClusterFlowEvent.PASS_REQUEST, 1);

            ClusterServerStatLogUtil.log("flow|pass|" + id, acquireCount);
            ClusterServerStatLogUtil.log("flow|pass_request|" + id, 1);

            // Remaining count is cut down to a smaller integer.
            return new TokenResult(TokenResultStatus.OK)
                .setRemaining((int) nextRemaining)
                .setWaitInMs(0);
        } else {
            // Blocked.
            metric.add(ClusterFlowEvent.BLOCK, acquireCount);
            metric.add(ClusterFlowEvent.BLOCK_REQUEST, 1);
            ClusterServerStatLogUtil.log("flow|block|" + id, acquireCount);
            ClusterServerStatLogUtil.log("flow|block_request|" + id, 1);

            return blockedResult();
        }
    }

    private static TokenResult blockedResult() {
        return new TokenResult(TokenResultStatus.BLOCKED)
            .setRemaining(0)
            .setWaitInMs(0);
    }

    private SimpleClusterFlowChecker() {}
}
