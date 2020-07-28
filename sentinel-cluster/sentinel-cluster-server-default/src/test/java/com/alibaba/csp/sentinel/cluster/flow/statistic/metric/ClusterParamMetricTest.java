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

import com.alibaba.csp.sentinel.cluster.test.AbstractTimeBasedTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ClusterParamMetricTest extends AbstractTimeBasedTest {

    @Test
    public void testClusterParamMetric() {
        setCurrentMillis(System.currentTimeMillis());
        Map<Object, Double> topMap = new HashMap<Object, Double>();
        ClusterParamMetric metric = new ClusterParamMetric(5, 25, 100);
        metric.addValue("e1", -1);
        metric.addValue("e1", -2);
        metric.addValue("e2", 100);
        metric.addValue("e2", 23);
        metric.addValue("e3", 100);
        metric.addValue("e3", 230);
        Assert.assertEquals(-3, metric.getSum("e1"));
        Assert.assertEquals(-120, metric.getAvg("e1"), 0.01);
        topMap.put("e3", (double) 13200);
        Assert.assertEquals(topMap, metric.getTopValues(1));
        topMap.put("e2", (double) 4920);
        topMap.put("e1", (double) -120);
        Assert.assertEquals(topMap, metric.getTopValues(5));
        metric.addValue("e2", 100);
        metric.addValue("e2", 23);
        Assert.assertEquals(246, metric.getSum("e2"));
        Assert.assertEquals(9840, metric.getAvg("e2"), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument() {
        ClusterParamMetric metric = new ClusterParamMetric(5, 25, 100);
        metric.getTopValues(-1);
    }
}
