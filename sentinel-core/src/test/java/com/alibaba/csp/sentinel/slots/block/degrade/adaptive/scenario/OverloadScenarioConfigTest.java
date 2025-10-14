package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link OverloadScenarioConfig}
 *
 * Adapted for resource-scoped keys and ctor OverloadScenarioConfig(String resourceName).
 */
public class OverloadScenarioConfigTest {

    private static final String RESOURCE = "testRes";

    private OverloadScenarioConfig config;

    private String RECOVERY_TIMEOUT_MS() { return "csp.sentinel.adaptive.overload." + RESOURCE + ".recovery"; }
    private String HALFOPEN_TIMEOUT_MS() { return "csp.sentinel.adaptive.overload." + RESOURCE + ".halfopen"; }
    private String OVERLOAD_CPU_THRESHOLD() { return "csp.sentinel.adaptive.overload." + RESOURCE + ".cpu"; }
    private String RESPONSE_TIME_MULTIPLE() { return "csp.sentinel.adaptive.overload." + RESOURCE + ".rt"; }
    private String ERROR_RATE_MULTIPLE() { return "csp.sentinel.adaptive.overload." + RESOURCE + ".error"; }
    private String TOMCAT_USAGE_RATE() { return "csp.sentinel.adaptive.overload." + RESOURCE + ".tomcat.usage"; }

    @Before
    public void setUp() {
        config = new OverloadScenarioConfig(RESOURCE);
        clearConfig();
    }

    @After
    public void tearDown() {
        clearConfig();
    }

    private void clearConfig() {
        SentinelConfig.removeConfig(RECOVERY_TIMEOUT_MS());
        SentinelConfig.removeConfig(HALFOPEN_TIMEOUT_MS());
        SentinelConfig.removeConfig(OVERLOAD_CPU_THRESHOLD());
        SentinelConfig.removeConfig(RESPONSE_TIME_MULTIPLE());
        SentinelConfig.removeConfig(ERROR_RATE_MULTIPLE());
        SentinelConfig.removeConfig(TOMCAT_USAGE_RATE());
    }

    @Test
    public void testDefaultValues() {
        assertEquals(10000, config.getRecoveryTimeoutMs());
        assertEquals(20000L, config.getHalfOpenTimeoutMs());
        assertEquals(0.6, config.getOverloadCpuThreshold(), 0.001);
        assertEquals(2.0, config.getResponseTimeMultiple(), 0.001);
        assertEquals(5.0, config.getErrorRateMultiple(), 0.001);
        assertEquals(0.70, config.getTomcatUsageRate(), 0.001);
    }

    @Test
    public void testLoadConfigFromSentinelConfig() {
        SentinelConfig.setConfig(RECOVERY_TIMEOUT_MS(), "10000");
        SentinelConfig.setConfig(HALFOPEN_TIMEOUT_MS(), "30000");
        SentinelConfig.setConfig(OVERLOAD_CPU_THRESHOLD(), "0.6");
        SentinelConfig.setConfig(RESPONSE_TIME_MULTIPLE(), "3.0");
        SentinelConfig.setConfig(ERROR_RATE_MULTIPLE(), "2.0");
        SentinelConfig.setConfig(TOMCAT_USAGE_RATE(), "0.9");

        OverloadScenarioConfig newConfig = new OverloadScenarioConfig(RESOURCE);

        assertEquals(10000, newConfig.getRecoveryTimeoutMs());
        assertEquals(30000L, newConfig.getHalfOpenTimeoutMs());
        assertEquals(0.6, newConfig.getOverloadCpuThreshold(), 0.001);
        assertEquals(3.0, newConfig.getResponseTimeMultiple(), 0.001);
        assertEquals(2.0, newConfig.getErrorRateMultiple(), 0.001);
        assertEquals(0.9, newConfig.getTomcatUsageRate(), 0.001);
    }

    @Test
    public void testLoadConfigAfterConstruction() {
        SentinelConfig.setConfig(RECOVERY_TIMEOUT_MS(), "15000");
        SentinelConfig.setConfig(HALFOPEN_TIMEOUT_MS(), "40000");
        SentinelConfig.setConfig(OVERLOAD_CPU_THRESHOLD(), "0.7");
        SentinelConfig.setConfig(RESPONSE_TIME_MULTIPLE(), "4.0");
        SentinelConfig.setConfig(ERROR_RATE_MULTIPLE(), "2.5");
        SentinelConfig.setConfig(TOMCAT_USAGE_RATE(), "0.95");

        config.loadConfig();

        assertEquals(15000, config.getRecoveryTimeoutMs());
        assertEquals(40000L, config.getHalfOpenTimeoutMs());
        assertEquals(0.7, config.getOverloadCpuThreshold(), 0.001);
        assertEquals(4.0, config.getResponseTimeMultiple(), 0.001);
        assertEquals(2.5, config.getErrorRateMultiple(), 0.001);
        assertEquals(0.95, config.getTomcatUsageRate(), 0.001);
    }

    @Test
    public void testEmptyStringDoesNotOverride() {
        SentinelConfig.setConfig(RECOVERY_TIMEOUT_MS(), "10000");
        SentinelConfig.setConfig(HALFOPEN_TIMEOUT_MS(), "30000");
        SentinelConfig.setConfig(OVERLOAD_CPU_THRESHOLD(), "0.6");
        SentinelConfig.setConfig(RESPONSE_TIME_MULTIPLE(), "3.0");
        SentinelConfig.setConfig(ERROR_RATE_MULTIPLE(), "2.0");
        SentinelConfig.setConfig(TOMCAT_USAGE_RATE(), "0.9");

        OverloadScenarioConfig newConfig = new OverloadScenarioConfig(RESOURCE);

        SentinelConfig.setConfig(RECOVERY_TIMEOUT_MS(), "");
        SentinelConfig.setConfig(OVERLOAD_CPU_THRESHOLD(), "");
        SentinelConfig.setConfig(RESPONSE_TIME_MULTIPLE(), "");

        newConfig.loadConfig();

        assertEquals(10000, newConfig.getRecoveryTimeoutMs()); 
        assertEquals(30000L, newConfig.getHalfOpenTimeoutMs()); 
        assertEquals(0.6, newConfig.getOverloadCpuThreshold(), 0.001); 
        assertEquals(3.0, newConfig.getResponseTimeMultiple(), 0.001); 
        assertEquals(2.0, newConfig.getErrorRateMultiple(), 0.001); 
        assertEquals(0.9, newConfig.getTomcatUsageRate(), 0.001); 
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidNumberThrowsException() {
        SentinelConfig.setConfig(RECOVERY_TIMEOUT_MS(), "abc");
        config.loadConfig();
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidDoubleThrowsException() {
        SentinelConfig.setConfig(OVERLOAD_CPU_THRESHOLD(), "not_a_number");
        config.loadConfig();
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidLongThrowsException() {
        SentinelConfig.setConfig(HALFOPEN_TIMEOUT_MS(), "xyz");
        config.loadConfig();
    }

    @Test
    public void testRepeatedLoading() {
        SentinelConfig.setConfig(RECOVERY_TIMEOUT_MS(), "10000");
        SentinelConfig.setConfig(OVERLOAD_CPU_THRESHOLD(), "0.6");
        config.loadConfig();

        assertEquals(10000, config.getRecoveryTimeoutMs());
        assertEquals(0.6, config.getOverloadCpuThreshold(), 0.001);

        SentinelConfig.setConfig(RECOVERY_TIMEOUT_MS(), "20000");
        SentinelConfig.setConfig(OVERLOAD_CPU_THRESHOLD(), "0.8");
        config.loadConfig();

        assertEquals(20000, config.getRecoveryTimeoutMs());
        assertEquals(0.8, config.getOverloadCpuThreshold(), 0.001);
    }

    @Test
    public void testSetterAndGetters() {
        config.setRecoveryTimeoutMs(8000);
        assertEquals(8000, config.getRecoveryTimeoutMs());

        config.setHalfOpenTimeoutMs(25000L);
        assertEquals(25000L, config.getHalfOpenTimeoutMs());

        config.setOverloadCpuThreshold(0.5);
        assertEquals(0.5, config.getOverloadCpuThreshold(), 0.001);

        config.setResponseTimeMultiple(2.5);
        assertEquals(2.5, config.getResponseTimeMultiple(), 0.001);

        config.setErrorRateMultiple(1.8);
        assertEquals(1.8, config.getErrorRateMultiple(), 0.001);

        config.setTomcatUsageRate(0.85);
        assertEquals(0.85, config.getTomcatUsageRate(), 0.001);
    }
}
