package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import org.junit.*;

import java.lang.reflect.Field;

/**
 * Unit test for {@link AdaptiveStatisticsConfig}.
 *
 * @author ylnxwlp
 */
public class AdaptiveStatisticsConfigTest {

    private String oldSampleCountCfg;
    private String oldIntervalCfg;

    private static final String SAMPLE_PROP = AdaptiveStatisticsConfig.SAMPLE_COUNT;
    private static final String INTERVAL_PROP = AdaptiveStatisticsConfig.INTERVAL_IN_MS;

    @Before
    public void setUp() throws Exception {
        oldSampleCountCfg = SentinelConfig.getConfig(SAMPLE_PROP);
        oldIntervalCfg = SentinelConfig.getConfig(INTERVAL_PROP);

        clearSentinelKey(SAMPLE_PROP);
        clearSentinelKey(INTERVAL_PROP);

        resetAdaptiveStatisticsSingleton();
    }

    @After
    public void tearDown() throws Exception {
        if (oldSampleCountCfg != null) {
            SentinelConfig.setConfig(SAMPLE_PROP, oldSampleCountCfg);
        } else {
            clearSentinelKey(SAMPLE_PROP);
        }
        if (oldIntervalCfg != null) {
            SentinelConfig.setConfig(INTERVAL_PROP, oldIntervalCfg);
        } else {
            clearSentinelKey(INTERVAL_PROP);
        }

        resetAdaptiveStatisticsSingleton();
    }

    @Test
    public void testDefaultsWhenNoPropertiesPresent() {
        AdaptiveStatisticsConfig cfg = AdaptiveStatisticsConfig.getInstance();
        cfg.loadConfig();

        Assert.assertEquals(20, cfg.getSampleCount());
        Assert.assertEquals(20000, cfg.getIntervalInMs());
    }

    @Test
    public void testLoadsValidProperties() {
        SentinelConfig.setConfig(SAMPLE_PROP, "30");
        SentinelConfig.setConfig(INTERVAL_PROP, "45000");

        AdaptiveStatisticsConfig cfg = AdaptiveStatisticsConfig.getInstance();
        cfg.loadConfig();

        Assert.assertEquals(30, cfg.getSampleCount());
        Assert.assertEquals(45000, cfg.getIntervalInMs());
    }

    @Test
    public void testReloadOverridesPreviousValues() {
        AdaptiveStatisticsConfig cfg = AdaptiveStatisticsConfig.getInstance();

        SentinelConfig.setConfig(SAMPLE_PROP, "10");
        SentinelConfig.setConfig(INTERVAL_PROP, "30000");
        cfg.loadConfig();
        Assert.assertEquals(10, cfg.getSampleCount());
        Assert.assertEquals(30000, cfg.getIntervalInMs());

        SentinelConfig.setConfig(SAMPLE_PROP, "40");
        SentinelConfig.setConfig(INTERVAL_PROP, "60000");
        cfg.loadConfig();
        Assert.assertEquals(40, cfg.getSampleCount());
        Assert.assertEquals(60000, cfg.getIntervalInMs());
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidSampleCountThrows() {
        SentinelConfig.setConfig(SAMPLE_PROP, "abc");
        clearSentinelKey(INTERVAL_PROP);

        AdaptiveStatisticsConfig.getInstance().loadConfig();
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidIntervalThrows() {
        clearSentinelKey(SAMPLE_PROP);
        SentinelConfig.setConfig(INTERVAL_PROP, "notANumber");

        AdaptiveStatisticsConfig.getInstance().loadConfig();
    }

    @Test
    public void testZeroAndNegativeValuesAreAccepted() {
        SentinelConfig.setConfig(SAMPLE_PROP, "0");
        SentinelConfig.setConfig(INTERVAL_PROP, "-1");

        AdaptiveStatisticsConfig cfg = AdaptiveStatisticsConfig.getInstance();
        cfg.loadConfig();

        Assert.assertEquals(0, cfg.getSampleCount());
        Assert.assertEquals(-1, cfg.getIntervalInMs());
    }

    @Test
    public void testEmptyStringsAreIgnored() {
        SentinelConfig.setConfig(SAMPLE_PROP, "11");
        SentinelConfig.setConfig(INTERVAL_PROP, "11111");
        AdaptiveStatisticsConfig cfg = AdaptiveStatisticsConfig.getInstance();
        cfg.loadConfig();
        Assert.assertEquals(11, cfg.getSampleCount());
        Assert.assertEquals(11111, cfg.getIntervalInMs());

        SentinelConfig.setConfig(SAMPLE_PROP, "");
        SentinelConfig.setConfig(INTERVAL_PROP, "");
        cfg.loadConfig();
        Assert.assertEquals(11, cfg.getSampleCount());
        Assert.assertEquals(11111, cfg.getIntervalInMs());
    }

    private static void clearSentinelKey(String key) {
        try {
            SentinelConfig.setConfig(key, null);
        } catch (Throwable ignore) {
            SentinelConfig.setConfig(key, "");
        }
    }

    private static void resetAdaptiveStatisticsSingleton() throws Exception {
        AdaptiveStatisticsConfig inst = AdaptiveStatisticsConfig.getInstance();
        setIntField(inst, "sampleCount", 20);
        setIntField(inst, "intervalInMs", 20000);
    }

    private static void setIntField(Object target, String fieldName, int value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(target, value);
    }
}