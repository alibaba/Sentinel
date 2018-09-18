package com.alibaba.csp.sentinel.slots.hotspot;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link HotParamChecker}.
 *
 * @author Eric Zhao
 */
public class HotParamCheckerTest {

    @Test
    public void testHotParamCheckerPassCheckExceedArgs() {
        final String resourceName = "testHotParamCheckerPassCheckExceedArgs";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 1;

        HotParamRule rule = new HotParamRule();
        rule.setResource(resourceName);
        rule.setCount(10);
        rule.setParamIdx(paramIdx);

        assertTrue("The rule will pass if the paramIdx exceeds provided args",
            HotParamChecker.passCheck(resourceWrapper, rule, 1, "abc"));
    }

    @Test
    public void testSingleValueCheckQpsWithoutExceptionItems() {
        final String resourceName = "testSingleValueCheckQpsWithoutExceptionItems";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long threshold = 5L;

        HotParamRule rule = new HotParamRule();
        rule.setResource(resourceName);
        rule.setCount(threshold);
        rule.setParamIdx(paramIdx);

        String valueA = "valueA";
        String valueB = "valueB";
        HotParameterMetric metric = mock(HotParameterMetric.class);
        when(metric.getPassParamQps(paramIdx, valueA)).thenReturn(threshold - 1);
        when(metric.getPassParamQps(paramIdx, valueB)).thenReturn(threshold + 1);
        HotParamSlot.getMetricsMap().put(resourceWrapper, metric);

        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
    }

    @Test
    public void testSingleValueCheckQpsWithExceptionItems() {
        final String resourceName = "testSingleValueCheckQpsWithExceptionItems";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long globalThreshold = 5L;
        int thresholdB = 3;
        int thresholdD = 7;

        HotParamRule rule = new HotParamRule();
        rule.setResource(resourceName);
        rule.setCount(globalThreshold);
        rule.setParamIdx(paramIdx);

        String valueA = "valueA";
        String valueB = "valueB";
        String valueC = "valueC";
        String valueD = "valueD";

        // Directly set parsed map for test.
        Map<Object, Integer> map = new HashMap<Object, Integer>();
        map.put(valueB, thresholdB);
        map.put(valueD, thresholdD);
        rule.setParsedHotItems(map);

        HotParameterMetric metric = mock(HotParameterMetric.class);
        when(metric.getPassParamQps(paramIdx, valueA)).thenReturn(globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, valueB)).thenReturn(globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, valueC)).thenReturn(globalThreshold - 1);
        when(metric.getPassParamQps(paramIdx, valueD)).thenReturn(globalThreshold + 1);
        HotParamSlot.getMetricsMap().put(resourceWrapper, metric);

        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));

        when(metric.getPassParamQps(paramIdx, valueA)).thenReturn(globalThreshold);
        when(metric.getPassParamQps(paramIdx, valueB)).thenReturn(thresholdB - 1L);
        when(metric.getPassParamQps(paramIdx, valueC)).thenReturn(globalThreshold + 1);
        when(metric.getPassParamQps(paramIdx, valueD)).thenReturn(globalThreshold - 1)
            .thenReturn((long)thresholdD);

        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
    }

    @Test
    public void testSingleValueCheckThreadCountWithExceptionItems() {
        final String resourceName = "testSingleValueCheckThreadCountWithExceptionItems";
        final ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        int paramIdx = 0;

        long globalThreshold = 5L;
        int thresholdB = 3;
        int thresholdD = 7;

        HotParamRule rule = new HotParamRule();
        rule.setResource(resourceName);
        rule.setCount(globalThreshold);
        rule.setParamIdx(paramIdx);
        rule.setBlockGrade(RuleConstant.FLOW_GRADE_THREAD);

        String valueA = "valueA";
        String valueB = "valueB";
        String valueC = "valueC";
        String valueD = "valueD";

        // Directly set parsed map for test.
        Map<Object, Integer> map = new HashMap<Object, Integer>();
        map.put(valueB, thresholdB);
        map.put(valueD, thresholdD);
        rule.setParsedHotItems(map);

        HotParameterMetric metric = mock(HotParameterMetric.class);
        when(metric.getThreadCount(paramIdx, valueA)).thenReturn(globalThreshold - 1);
        when(metric.getThreadCount(paramIdx, valueB)).thenReturn(globalThreshold - 1);
        when(metric.getThreadCount(paramIdx, valueC)).thenReturn(globalThreshold - 1);
        when(metric.getThreadCount(paramIdx, valueD)).thenReturn(globalThreshold + 1);
        HotParamSlot.getMetricsMap().put(resourceWrapper, metric);

        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));

        when(metric.getThreadCount(paramIdx, valueA)).thenReturn(globalThreshold);
        when(metric.getThreadCount(paramIdx, valueB)).thenReturn(thresholdB - 1L);
        when(metric.getThreadCount(paramIdx, valueC)).thenReturn(globalThreshold + 1);
        when(metric.getThreadCount(paramIdx, valueD)).thenReturn(globalThreshold - 1)
            .thenReturn((long)thresholdD);

        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueA));
        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueB));
        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueC));
        assertTrue(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
        assertFalse(HotParamChecker.passSingleValueCheck(resourceWrapper, rule, 1, valueD));
    }

    @Before
    public void setUp() throws Exception {
        HotParamSlot.getMetricsMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        HotParamSlot.getMetricsMap().clear();
    }
}