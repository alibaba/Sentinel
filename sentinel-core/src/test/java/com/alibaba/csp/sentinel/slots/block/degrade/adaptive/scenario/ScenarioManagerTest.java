package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ScenarioManager}
 *
 * Adapted for resource-scoped configs and new null/unsupported handling.
 */
public class ScenarioManagerTest {

    private static final String TEST_RESOURCE = "testResource";

    @Before
    public void setUp() {
        ScenarioManager.reset();
    }

    @Test
    public void testStaticDefaultRegistrationExistence() {
        ScenarioConfig cfg = ScenarioManager.getConfig(TEST_RESOURCE, Scenario.SystemScenario.OVER_LOAD);
        Scenario scenario = ScenarioManager.getScenario(Scenario.SystemScenario.OVER_LOAD);

        assertNotNull("OverloadScenarioConfig should be created on demand", cfg);
        assertNotNull("OverLoadScenario should be registered by default", scenario);

        assertTrue("Config should be instance of OverloadScenarioConfig", cfg instanceof OverloadScenarioConfig);
        assertTrue("Scenario should be instance of OverLoadScenario", scenario instanceof OverLoadScenario);
        assertEquals("Scenario type should match", Scenario.SystemScenario.OVER_LOAD, scenario.getScenarioType());
    }

    @Test
    public void testRegisterNullScenario() {
        int originalSize = ScenarioManager.getAllScenarios().size();
        ScenarioManager.registerScenario(null);
        assertEquals(originalSize, ScenarioManager.getAllScenarios().size());
    }

    @Test
    public void testRegisterOverride() {
        Scenario customScenario = new Scenario() {
            @Override
            public boolean matchScenario(String resourceName,
                                         WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow,
                                         List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows,
                                         AdaptiveServerMetric adaptiveServerMetric) {
                return false;
            }

            @Override
            public SystemScenario getScenarioType() {
                return SystemScenario.OVER_LOAD;
            }
        };

        ScenarioManager.registerScenario(customScenario);
        Scenario retrieved = ScenarioManager.getScenario(Scenario.SystemScenario.OVER_LOAD);
        assertSame("Should return the custom scenario instance", customScenario, retrieved);
    }

    @Test
    public void testGetScenarioWithNull() {
        Scenario scenario = ScenarioManager.getScenario(null);
        assertNull("getScenario(null) should return null", scenario);
    }

    @Test
    public void testDefensiveCopyForGetAllScenarios() {
        // Ensure one scenario exists
        ScenarioManager.getConfig(TEST_RESOURCE, Scenario.SystemScenario.OVER_LOAD);

        Map<Scenario.SystemScenario, Scenario> m1 = ScenarioManager.getAllScenarios();
        assertNotNull("getAllScenarios should not return null", m1);
        int size1 = m1.size();

        Scenario mockScenario = mock(Scenario.class);
        when(mockScenario.getScenarioType()).thenReturn(Scenario.SystemScenario.NORMAL);
        m1.put(Scenario.SystemScenario.NORMAL, mockScenario);

        assertEquals("Modified copy size should change", size1 + 1, m1.size());

        Map<Scenario.SystemScenario, Scenario> m2 = ScenarioManager.getAllScenarios();
        assertEquals("Fresh copy should have original size", size1, m2.size());
        assertNotSame("Should be different instances", m1, m2);
    }

    @Test
    public void testDefensiveCopyForGetAllConfigs() {
        ScenarioConfig created = ScenarioManager.getConfig(TEST_RESOURCE, Scenario.SystemScenario.OVER_LOAD);
        assertNotNull(created);

        Map<String, Map<Scenario.SystemScenario, ScenarioConfig>> copy1 = ScenarioManager.getAllConfigs();
        assertNotNull("getAllConfigs should not return null", copy1);

        Map<Scenario.SystemScenario, ScenarioConfig> resourceMap = copy1.get(TEST_RESOURCE);
        assertNotNull("Resource map should exist in copy", resourceMap);
        int originalSize = resourceMap.size();

        ScenarioConfig mockCfg = mock(ScenarioConfig.class);
        resourceMap.put(Scenario.SystemScenario.NORMAL, mockCfg);
        assertEquals("Modified copy size should change", originalSize + 1, resourceMap.size());

        Map<String, Map<Scenario.SystemScenario, ScenarioConfig>> copy2 = ScenarioManager.getAllConfigs();
        Map<Scenario.SystemScenario, ScenarioConfig> resourceMap2 = copy2.get(TEST_RESOURCE);
        assertNotNull(resourceMap2);
        assertEquals("Fresh copy should keep original size", originalSize, resourceMap2.size());
        assertNotSame("Should be different map instances", resourceMap, resourceMap2);
    }

    @Test
    public void testGetAllScenariosReturnsCopy() {
        Map<Scenario.SystemScenario, Scenario> s1 = ScenarioManager.getAllScenarios();
        Map<Scenario.SystemScenario, Scenario> s2 = ScenarioManager.getAllScenarios();
        assertNotNull(s1);
        assertNotNull(s2);
        assertNotSame("Should return different instances", s1, s2);
        assertEquals("Should have same content size", s1.size(), s2.size());
        for (Scenario.SystemScenario key : s1.keySet()) {
            assertSame("Scenario instances should be identical objects", s1.get(key), s2.get(key));
        }
    }

    @Test
    public void testGetAllConfigsReturnsCopy() {
        // Ensure at least one config exists
        ScenarioManager.getConfig(TEST_RESOURCE, Scenario.SystemScenario.OVER_LOAD);

        Map<String, Map<Scenario.SystemScenario, ScenarioConfig>> c1 = ScenarioManager.getAllConfigs();
        Map<String, Map<Scenario.SystemScenario, ScenarioConfig>> c2 = ScenarioManager.getAllConfigs();

        assertNotNull(c1);
        assertNotNull(c2);
        assertNotSame("Should return different instances", c1, c2);
        assertEquals("Top-level size should match", c1.size(), c2.size());

        if (c1.containsKey(TEST_RESOURCE) && c2.containsKey(TEST_RESOURCE)) {
            Map<Scenario.SystemScenario, ScenarioConfig> r1 = c1.get(TEST_RESOURCE);
            Map<Scenario.SystemScenario, ScenarioConfig> r2 = c2.get(TEST_RESOURCE);
            assertNotSame("Nested map should be different instances", r1, r2);
            assertEquals("Nested map size should match", r1.size(), r2.size());
            for (Scenario.SystemScenario key : r1.keySet()) {
                assertSame("Config instances should be identical objects", r1.get(key), r2.get(key));
            }
        }
    }

    @Test
    public void testDifferentResourceNameReturnsDifferentConfigs() {
        String resource1 = "resource1";
        String resource2 = "resource2";

        ScenarioConfig c1 = ScenarioManager.getConfig(resource1, Scenario.SystemScenario.OVER_LOAD);
        ScenarioConfig c2 = ScenarioManager.getConfig(resource2, Scenario.SystemScenario.OVER_LOAD);

        assertNotNull(c1);
        assertNotNull(c2);
        assertNotSame("Configs for different resources should be different instances", c1, c2);
        assertTrue(c1 instanceof OverloadScenarioConfig);
        assertTrue(c2 instanceof OverloadScenarioConfig);
    }

    @Test
    public void testSameResourceReturnsSameConfig() {
        String resource = "sameResource";

        ScenarioConfig c1 = ScenarioManager.getConfig(resource, Scenario.SystemScenario.OVER_LOAD);
        ScenarioConfig c2 = ScenarioManager.getConfig(resource, Scenario.SystemScenario.OVER_LOAD);

        assertNotNull(c1);
        assertNotNull(c2);
        assertSame("Configs for same resource should be same instance", c1, c2);
    }

    @Test
    public void testGetConfigWithNullParameters() {
        assertTrue("null resource -> DefaultScenarioConfig",
                ScenarioManager.getConfig(null, Scenario.SystemScenario.OVER_LOAD) instanceof DefaultScenarioConfig);
        assertTrue("null scenario -> DefaultScenarioConfig",
                ScenarioManager.getConfig(TEST_RESOURCE, null) instanceof DefaultScenarioConfig);
        assertTrue("both null -> DefaultScenarioConfig",
                ScenarioManager.getConfig(null, null) instanceof DefaultScenarioConfig);
    }

    @Test
    public void testGetConfigWithUnsupportedScenario() {
        // A mock enum value won't match OVER_LOAD, so default branch is used.
        Scenario.SystemScenario unsupported = mock(Scenario.SystemScenario.class);
        ScenarioConfig cfg = ScenarioManager.getConfig(TEST_RESOURCE, unsupported);
        assertTrue("Unsupported scenario -> DefaultScenarioConfig", cfg instanceof DefaultScenarioConfig);
    }
}
