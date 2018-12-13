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
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterMetric;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.alibaba.csp.sentinel.cluster.ClusterFlowTestUtil.*;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterFlowCheckerTest {

    //@Test
    public void testAcquireClusterTokenOccupyPass() {
        long flowId = 98765L;
        final int threshold = 5;
        FlowRule clusterRule = new FlowRule("abc")
            .setCount(threshold)
            .setClusterMode(true)
            .setClusterConfig(new ClusterFlowConfig()
                .setFlowId(flowId)
                .setThresholdType(ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL));
        int sampleCount = 5;
        int intervalInMs = 1000;
        int bucketLength = intervalInMs / sampleCount;
        ClusterMetric metric = new ClusterMetric(sampleCount, intervalInMs);
        ClusterMetricStatistics.putMetric(flowId, metric);

        System.out.println(System.currentTimeMillis());
        assertResultPass(tryAcquire(clusterRule, false));
        assertResultPass(tryAcquire(clusterRule, false));
        sleep(bucketLength);
        assertResultPass(tryAcquire(clusterRule, false));
        sleep(bucketLength);
        assertResultPass(tryAcquire(clusterRule, true));
        assertResultPass(tryAcquire(clusterRule, false));
        assertResultBlock(tryAcquire(clusterRule, true));
        sleep(bucketLength);
        assertResultBlock(tryAcquire(clusterRule, false));
        assertResultBlock(tryAcquire(clusterRule, false));
        sleep(bucketLength);
        assertResultBlock(tryAcquire(clusterRule, false));
        assertResultWait(tryAcquire(clusterRule, true), bucketLength);
        assertResultBlock(tryAcquire(clusterRule, false));
        sleep(bucketLength);
        assertResultPass(tryAcquire(clusterRule, false));

        ClusterMetricStatistics.removeMetric(flowId);
    }

    private TokenResult tryAcquire(FlowRule clusterRule, boolean occupy) {
        return ClusterFlowChecker.acquireClusterToken(clusterRule, 1, occupy);
    }
}