package com.alibaba.csp.sentinel.slots.hotspot;

import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.metric.HotParameterLeapArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link HotParameterMetric}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotParameterMetricTest {

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