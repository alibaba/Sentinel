package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OverLoadScenario}
 *
 * Adapted for matchScenario(String resourceName, ...) and resource-scoped config.
 */
public class OverLoadScenarioTest {

    private static final String RESOURCE = "scenarioRes";

    private OverLoadScenario scenario;
    private OverloadScenarioConfig config;
    private AdaptiveServerMetric mockMetric;

    @Before
    public void setUp() {
        scenario = new OverLoadScenario();
        config = (OverloadScenarioConfig) ScenarioManager.getConfig(RESOURCE, Scenario.SystemScenario.OVER_LOAD);
        mockMetric = mock(AdaptiveServerMetric.class);
    }

    private WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> createWindow(long total, long error, long rt) {
        AdaptiveCircuitBreaker.AdaptiveCounter counter = new AdaptiveCircuitBreaker.AdaptiveCounter();
        counter.getTotalCount().add(total);
        counter.getErrorCount().add(error);
        counter.getOverallRTTime().add(rt);
        return new WindowWrap<>(1000, System.currentTimeMillis(), counter);
    }

    @Test
    public void testWindowCountLessThanThree_shouldReturnFalse() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(10, 1, 100);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(5, 0, 50));
        assertFalse(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testHistoricalAvgGreaterThanCurrent_shouldEarlyExit() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(3, 0, 30);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(5, 0, 50));
        windows.add(createWindow(5, 0, 50));
        windows.add(current);
        assertFalse(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testHistoricalAvgEqualsCurrent_shouldNotEarlyExit() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(3, 0, 30);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(3, 0, 30));
        windows.add(createWindow(3, 0, 30));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(0.0);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(0.0);
        config.setResponseTimeMultiple(1.5);
        assertFalse(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testOnlyRTConditionTriggered_noMetrics_shouldReturnFalse() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(10, 0, 2000);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(10, 0, 1000));
        windows.add(createWindow(10, 0, 1000));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(0.0);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(0.0);
        config.setResponseTimeMultiple(1.5);
        assertFalse(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testOnlyRTConditionTriggered_withMetrics_shouldReturnTrue() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(10, 0, 2000);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(10, 0, 1000));
        windows.add(createWindow(10, 0, 1000));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(config.getOverloadCpuThreshold() + 0.1);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(0.0);
        config.setResponseTimeMultiple(1.5);
        assertTrue(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testOnlyErrorRateConditionTriggered_cpuOverThreshold_shouldReturnTrue() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(100, 60, 1000);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(100, 30, 1000));
        windows.add(createWindow(100, 30, 1000));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(config.getOverloadCpuThreshold() + 0.1);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(0.0);
        config.setErrorRateMultiple(1.5);
        assertTrue(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testOnlyErrorRateConditionTriggered_tomcatOverThreshold_shouldReturnTrue() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(100, 60, 1000);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(100, 30, 1000));
        windows.add(createWindow(100, 30, 1000));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(0.0);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(config.getTomcatUsageRate() + 0.1);
        config.setErrorRateMultiple(1.5);
        assertTrue(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testConditionsMet_butMetricsNotExceedThreshold_shouldReturnFalse() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(100, 60, 2000);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(100, 30, 1000));
        windows.add(createWindow(100, 30, 1000));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(config.getOverloadCpuThreshold() - 0.1);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(config.getTomcatUsageRate() - 0.1);
        config.setErrorRateMultiple(1.5);
        config.setResponseTimeMultiple(1.5);
        assertFalse(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testNoConditionMet_shouldReturnFalse() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(100, 10, 1000);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(100, 10, 1000));
        windows.add(createWindow(100, 10, 1000));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(0.9);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(0.9);
        assertFalse(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testHistoricalTotalIsZero_shouldStillTriggerCondition() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(10, 5, 1000);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(0, 0, 0));
        windows.add(createWindow(0, 0, 0));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(config.getOverloadCpuThreshold() + 0.1);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(0.0);
        config.setErrorRateMultiple(1.5);
        assertTrue(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testIntegerDivisionEdgeCase() {
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> current = createWindow(4, 0, 40);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = new ArrayList<>();
        windows.add(createWindow(4, 0, 40));
        windows.add(createWindow(5, 0, 50));
        windows.add(current);
        when(mockMetric.getServerCpuUsage()).thenReturn(0.0);
        when(mockMetric.getServerTomcatUsageRate()).thenReturn(0.0);
        config.setResponseTimeMultiple(1.5);
        assertFalse(scenario.matchScenario(RESOURCE, current, windows, mockMetric));
    }

    @Test
    public void testGetScenarioType() {
        assertEquals(Scenario.SystemScenario.OVER_LOAD, scenario.getScenarioType());
    }
}
