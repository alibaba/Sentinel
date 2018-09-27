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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.statistic.metric.HotParameterLeapArray;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link ParameterMetric}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParameterMetricTest {

    @Test
    public void testGetTopParamCount() {
        ParameterMetric metric = new ParameterMetric();
        int index = 1;
        int n = 10;
        RollingParamEvent event = RollingParamEvent.REQUEST_PASSED;
        HotParameterLeapArray leapArray = mock(HotParameterLeapArray.class);
        Map<Object, Double> topValues = new HashMap<Object, Double>() {{
            put("a", 3d);
            put("b", 7d);
        }};
        when(leapArray.getTopValues(event, n)).thenReturn(topValues);

        // Get when not initialized.
        assertEquals(0, metric.getTopPassParamCount(index, n).size());

        metric.getRollingParameters().put(index, leapArray);
        assertEquals(topValues, metric.getTopPassParamCount(index, n));
    }

    @Test
    public void testInitAndClearHotParameterMetric() {
        ParameterMetric metric = new ParameterMetric();
        int index = 1;
        metric.initializeForIndex(index);
        HotParameterLeapArray leapArray = metric.getRollingParameters().get(index);
        assertNotNull(leapArray);

        metric.initializeForIndex(index);
        assertSame(leapArray, metric.getRollingParameters().get(index));

        metric.clear();
        assertEquals(0, metric.getRollingParameters().size());
    }

    private static final int PARAM_TYPE_NORMAL = 0;
    private static final int PARAM_TYPE_ARRAY = 1;
    private static final int PARAM_TYPE_COLLECTION = 2;
}