/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.param;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class ParameterMetricStorageTest {

    @Test
    public void testGetNullParamMetric() {
        assertNull(ParameterMetricStorage.getParamMetric(null));
    }

    @Test
    public void testInitParamMetrics() {
        ParamFlowRule rule = new ParamFlowRule();
        rule.setParamIdx(1);
        int index = 1;
        String resourceName = "res-" + System.currentTimeMillis();
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);

        assertNull(ParameterMetricStorage.getParamMetric(resourceWrapper));

        ParameterMetricStorage.initParamMetricsFor(resourceWrapper, rule);
        ParameterMetric metric = ParameterMetricStorage.getParamMetric(resourceWrapper);
        assertNotNull(metric);
        assertNotNull(metric.getRuleTimeCounterMap().get(rule));
        assertNotNull(metric.getThreadCountMap().get(index));

        // Duplicate init.
        ParameterMetricStorage.initParamMetricsFor(resourceWrapper, rule);
        assertSame(metric, ParameterMetricStorage.getParamMetric(resourceWrapper));

        ParamFlowRule rule2 = new ParamFlowRule();
        rule2.setParamIdx(1);
        assertSame(metric, ParameterMetricStorage.getParamMetric(resourceWrapper));
    }

    @Before
    public void setUp() {
        ParameterMetricStorage.getMetricsMap().clear();
    }

    @After
    public void tearDown() {
        ParameterMetricStorage.getMetricsMap().clear();
    }
}
