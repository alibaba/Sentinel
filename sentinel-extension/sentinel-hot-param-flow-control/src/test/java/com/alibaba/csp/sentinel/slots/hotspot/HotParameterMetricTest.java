package com.alibaba.csp.sentinel.slots.hotspot;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.metric.HotParameterLeapArray;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link HotParameterMetric}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotParameterMetricTest {

    @Test
    public void testGetTopParamCount() {
        HotParameterMetric metric = new HotParameterMetric();
        int index = 1;
        int n = 10;
        RollingParamEvent event = RollingParamEvent.REQUEST_PASSED;
        HotParameterLeapArray leapArray = mock(HotParameterLeapArray.class);
        Map<Object, Double> topValues = new HashMap<Object, Double>() {{put("a", 3d); put("b", 7d);}};
        when(leapArray.getTopValues(event, n)).thenReturn(topValues);

        // Get when not initialized.
        assertEquals(0, metric.getTopPassParamCount(index, n).size());

        metric.getRollingParameters().put(index, leapArray);
        assertEquals(topValues, metric.getTopPassParamCount(index, n));
    }

    @Test
    public void testInitAndClearHotParameterMetric() {
        HotParameterMetric metric = new HotParameterMetric();
        int index = 1;
        metric.initializeForIndex(index);
        HotParameterLeapArray leapArray = metric.getRollingParameters().get(index);
        CacheMap cacheMap = metric.getThreadCountMap().get(index);
        assertNotNull(leapArray);
        assertNotNull(cacheMap);

        metric.initializeForIndex(index);
        assertSame(leapArray, metric.getRollingParameters().get(index));
        assertSame(cacheMap, metric.getThreadCountMap().get(index));

        metric.clear();
        assertEquals(0, metric.getRollingParameters().size());
        assertEquals(0, metric.getThreadCountMap().size());
    }
}