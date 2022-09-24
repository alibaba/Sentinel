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
package com.alibaba.csp.sentinel.cluster.flow.statistic.metric;

import com.alibaba.csp.sentinel.cluster.flow.statistic.data.ClusterFlowEvent;
import com.alibaba.csp.sentinel.cluster.test.AbstractTimeBasedTest;
import org.junit.Assert;
import org.junit.Test;

public class ClusterMetricTest extends AbstractTimeBasedTest {

    @Test
    public void testTryOccupyNext() {
        setCurrentMillis(System.currentTimeMillis());
        ClusterMetric metric = new ClusterMetric(5, 25);
        metric.add(ClusterFlowEvent.PASS, 1);
        metric.add(ClusterFlowEvent.PASS, 2);
        metric.add(ClusterFlowEvent.PASS, 1);
        metric.add(ClusterFlowEvent.BLOCK, 1);
        Assert.assertEquals(4, metric.getSum(ClusterFlowEvent.PASS));
        Assert.assertEquals(1, metric.getSum(ClusterFlowEvent.BLOCK));
        Assert.assertEquals(160, metric.getAvg(ClusterFlowEvent.PASS), 0.01);
        Assert.assertEquals(200, metric.tryOccupyNext(ClusterFlowEvent.PASS, 111, 900));
        metric.add(ClusterFlowEvent.PASS, 1);
        metric.add(ClusterFlowEvent.PASS, 2);
        metric.add(ClusterFlowEvent.PASS, 1);
        Assert.assertEquals(200, metric.tryOccupyNext(ClusterFlowEvent.PASS, 222, 900));
        metric.add(ClusterFlowEvent.PASS, 1);
        metric.add(ClusterFlowEvent.PASS, 2);
        metric.add(ClusterFlowEvent.PASS, 1);
        Assert.assertEquals(0, metric.tryOccupyNext(ClusterFlowEvent.PASS, 333, 900));
    }
}
