package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AdaptiveDegradeRuleManager}.
 *
 * @author ylnxwlp
 */
public class AdaptiveDegradeRuleManagerTest {

    private static final String RESOURCE_A = "resourceA";
    private static final String RESOURCE_B = "resourceB";

    @Before
    public void setUp() throws Exception {
        Field ruleMapField = AdaptiveDegradeRuleManager.class.getDeclaredField("adaptiveRuleMap");
        ruleMapField.setAccessible(true);
        ((java.util.concurrent.ConcurrentHashMap<?, ?>) ruleMapField.get(null)).clear();
        Field metricMapField = AdaptiveDegradeRuleManager.class.getDeclaredField("adaptiveMetricMap");
        metricMapField.setAccessible(true);
        ((java.util.concurrent.ConcurrentHashMap<?, ?>) metricMapField.get(null)).clear();
    }

    @After
    public void tearDown() throws Exception {
        Field ruleMapField = AdaptiveDegradeRuleManager.class.getDeclaredField("adaptiveRuleMap");
        ruleMapField.setAccessible(true);
        ((java.util.concurrent.ConcurrentHashMap<?, ?>) ruleMapField.get(null)).clear();
        Field metricMapField = AdaptiveDegradeRuleManager.class.getDeclaredField("adaptiveMetricMap");
        metricMapField.setAccessible(true);
        ((java.util.concurrent.ConcurrentHashMap<?, ?>) metricMapField.get(null)).clear();
    }

    @Test
    public void testGetRule_LazyCreationAndIdempotent() {
        AdaptiveDegradeRule rule1 = AdaptiveDegradeRuleManager.getRule(RESOURCE_A);
        AdaptiveDegradeRule rule2 = AdaptiveDegradeRuleManager.getRule(RESOURCE_A);
        assertNotNull("Rule should not be null", rule1);
        assertNotNull("Rule should not be null", rule2);
        assertSame("Same resource should return same instance", rule1, rule2);
        assertFalse("Default enabled should be false", rule1.isEnabled());
        AdaptiveDegradeRule ruleB = AdaptiveDegradeRuleManager.getRule(RESOURCE_B);
        assertNotNull("Rule should not be null", ruleB);
        assertNotSame("Different resources should return different instances", rule1, ruleB);
    }

    @Test
    public void testGetServerMetric_LazyCreationAndIdempotent() {
        AdaptiveServerMetric metric1 = AdaptiveDegradeRuleManager.getServerMetric(RESOURCE_A);
        AdaptiveServerMetric metric2 = AdaptiveDegradeRuleManager.getServerMetric(RESOURCE_A);
        assertNotNull("Metric should not be null", metric1);
        assertNotNull("Metric should not be null", metric2);
        assertSame("Same resource should return same instance", metric1, metric2);
        assertEquals("Default serverCpuUsage should be -1.0", -1.0, metric1.getServerCpuUsage(), 0.0);
        assertEquals("Default serverTomcatUsageRate should be -1.0", -1.0, metric1.getServerTomcatUsageRate(), 0.0);
        assertEquals("Default serverTomcatQueueSize should be -1", -1, metric1.getServerTomcatQueueSize());
        AdaptiveServerMetric metricB = AdaptiveDegradeRuleManager.getServerMetric(RESOURCE_B);
        assertNotNull("Metric should not be null", metricB);
        assertNotSame("Different resources should return different instances", metric1, metricB);
    }

    @Test
    public void testPropertyListener_ConfigLoad() {
        AdaptiveDegradeRule rule = new AdaptiveDegradeRule(RESOURCE_A);
        rule.setEnabled(false);
        DynamicSentinelProperty<AdaptiveDegradeRule> propertyWithInitialValue = new DynamicSentinelProperty<>(rule);
        AdaptiveDegradeRuleManager.register2Property(propertyWithInitialValue);
        AdaptiveDegradeRule retrievedRule = AdaptiveDegradeRuleManager.getRule(RESOURCE_A);
        assertNotNull("Rule should not be null", retrievedRule);
        assertFalse("Rule should have enabled=false from configLoad", retrievedRule.isEnabled());
    }

    @Test
    public void testPropertyListener_ConfigUpdate() {
        DynamicSentinelProperty<AdaptiveDegradeRule> property = new DynamicSentinelProperty<>();
        AdaptiveDegradeRuleManager.register2Property(property);
        AdaptiveDegradeRule rule = new AdaptiveDegradeRule(RESOURCE_A);
        rule.setEnabled(false);
        property.updateValue(rule);
        AdaptiveDegradeRule retrievedRule = AdaptiveDegradeRuleManager.getRule(RESOURCE_A);
        assertNotNull("Rule should not be null", retrievedRule);
        assertFalse("Rule should have enabled=false from configUpdate", retrievedRule.isEnabled());
    }

    @Test
    public void testRegister2Property_Effectiveness() {
        DynamicSentinelProperty<AdaptiveDegradeRule> prop1 = new DynamicSentinelProperty<>();
        DynamicSentinelProperty<AdaptiveDegradeRule> prop2 = new DynamicSentinelProperty<>();
        AdaptiveDegradeRuleManager.register2Property(prop1);
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_A);
        rule1.setEnabled(false);
        prop1.updateValue(rule1);
        AdaptiveDegradeRule retrievedRule1 = AdaptiveDegradeRuleManager.getRule(RESOURCE_A);
        assertNotNull("Rule should not be null", retrievedRule1);
        assertFalse("Rule should have enabled=false from prop1", retrievedRule1.isEnabled());
        AdaptiveDegradeRuleManager.register2Property(prop2);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule(RESOURCE_B);
        rule2.setEnabled(true);
        prop2.updateValue(rule2);
        AdaptiveDegradeRule retrievedRule2 = AdaptiveDegradeRuleManager.getRule(RESOURCE_B);
        assertNotNull("Rule should not be null", retrievedRule2);
        assertTrue("Rule should have enabled=true from prop2", retrievedRule2.isEnabled());
        AdaptiveDegradeRule rule1Update = new AdaptiveDegradeRule(RESOURCE_A);
        rule1Update.setEnabled(true);
        prop1.updateValue(rule1Update);
        AdaptiveDegradeRule retrievedRule1Again = AdaptiveDegradeRuleManager.getRule(RESOURCE_A);
        assertFalse("Rule A should still have enabled=false due to putIfAbsent", retrievedRule1Again.isEnabled());
    }

    @Test
    public void testRegister2Property_NullValidation() {
        try {
            AdaptiveDegradeRuleManager.register2Property(null);
            fail("Should throw IllegalArgumentException for null property");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetRule_NullValidation() {
        try {
            AdaptiveDegradeRuleManager.getRule(null);
            fail("Should throw IllegalArgumentException for null resource name");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetServerMetric_NullValidation() {
        try {
            AdaptiveDegradeRuleManager.getServerMetric(null);
            fail("Should throw IllegalArgumentException for null resource name");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testConcurrentGetRule_CreationUniqueness() throws InterruptedException {
        final int threadCount = 50;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AdaptiveDegradeRule[] rules = new AdaptiveDegradeRule[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    rules[index] = AdaptiveDegradeRuleManager.getRule(RESOURCE_A);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();
        AdaptiveDegradeRule firstRule = rules[0];
        assertNotNull("First rule should not be null", firstRule);
        for (int i = 1; i < threadCount; i++) {
            assertSame("All rules should be the same instance", firstRule, rules[i]);
        }
    }

    @Test
    public void testConcurrentGetServerMetric_CreationUniqueness() throws InterruptedException {
        final int threadCount = 50;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AdaptiveServerMetric[] metrics = new AdaptiveServerMetric[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    metrics[index] = AdaptiveDegradeRuleManager.getServerMetric(RESOURCE_A);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();
        AdaptiveServerMetric firstMetric = metrics[0];
        assertNotNull("First metric should not be null", firstMetric);
        for (int i = 1; i < threadCount; i++) {
            assertSame("All metrics should be the same instance", firstMetric, metrics[i]);
        }
    }

    @Test
    public void testPutIfAbsentBehavior_NoOverwrite() {
        DynamicSentinelProperty<AdaptiveDegradeRule> property = new DynamicSentinelProperty<>();
        AdaptiveDegradeRuleManager.register2Property(property);
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_A);
        rule1.setEnabled(false);
        property.updateValue(rule1);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule(RESOURCE_A);
        rule2.setEnabled(true);
        property.updateValue(rule2);
        AdaptiveDegradeRule retrievedRule = AdaptiveDegradeRuleManager.getRule(RESOURCE_A);
        assertNotNull("Rule should not be null", retrievedRule);
        assertFalse("Rule should still have enabled=false due to putIfAbsent behavior", retrievedRule.isEnabled());
    }
}