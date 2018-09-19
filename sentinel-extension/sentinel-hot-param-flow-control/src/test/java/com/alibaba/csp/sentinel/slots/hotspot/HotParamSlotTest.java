package com.alibaba.csp.sentinel.slots.hotspot;

import java.util.Collections;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link HotParamSlot}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotParamSlotTest {

    private final HotParamSlot hotParamSlot = new HotParamSlot();

    @Test
    public void testEntryWhenHotRuleNotExists() throws Throwable {
        String resourceName = "testEntryWhenHotRuleNotExists";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        hotParamSlot.entry(null, resourceWrapper, null, 1, "abc");
        // The hot parameter metric instance will not be created.
        assertNull(HotParamSlot.getHotParamMetric(resourceWrapper));
    }

    @Test
    public void testEntryWhenHotRuleExists() throws Throwable {
        String resourceName = "testEntryWhenHotRuleExists";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        long argToGo = 1L;
        long count = 10;
        HotParamRule rule = new HotParamRule(resourceName)
            .setCount(count)
            .setParamIdx(0);
        HotParamRuleManager.loadRules(Collections.singletonList(rule));

        HotParameterMetric metric = mock(HotParameterMetric.class);
        // First pass, then blocked.
        when(metric.getPassParamQps(rule.getParamIdx(), argToGo))
            .thenReturn(count - 1)
            .thenReturn(count);
        // Insert the mock metric to control pass or block.
        HotParamSlot.getMetricsMap().put(resourceWrapper, metric);

        // The first entry will pass.
        hotParamSlot.entry(null, resourceWrapper, null, 1, argToGo);
        // The second entry will be blocked.
        try {
            hotParamSlot.entry(null, resourceWrapper, null, 1, argToGo);
        } catch (HotParamException ex) {
            assertEquals(String.valueOf(argToGo), ex.getMessage());
            assertEquals(resourceName, ex.getResourceName());
            return;
        }
        fail("The second entry should be blocked");
    }

    @Test
    public void testGetNullHotParamMetric() {
        assertNull(HotParamSlot.getHotParamMetric(null));
    }

    @Test
    public void testInitHotParamMetrics() {
        int index = 1;
        String resourceName = "res-" + System.currentTimeMillis();
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);

        assertNull(HotParamSlot.getHotParamMetric(resourceWrapper));

        hotParamSlot.initHotParamMetricsFor(resourceWrapper, index);
        HotParameterMetric metric = HotParamSlot.getHotParamMetric(resourceWrapper);
        assertNotNull(metric);
        assertNotNull(metric.getRollingParameters().get(index));
        assertNotNull(metric.getThreadCountMap().get(index));

        // Duplicate init.
        hotParamSlot.initHotParamMetricsFor(resourceWrapper, index);
        assertSame(metric, HotParamSlot.getHotParamMetric(resourceWrapper));
    }

    @Before
    public void setUp() throws Exception {
        HotParamRuleManager.loadRules(null);
        HotParamSlot.getMetricsMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        // Clean the metrics map.
        HotParamSlot.getMetricsMap().clear();
        HotParamRuleManager.loadRules(null);
    }
}