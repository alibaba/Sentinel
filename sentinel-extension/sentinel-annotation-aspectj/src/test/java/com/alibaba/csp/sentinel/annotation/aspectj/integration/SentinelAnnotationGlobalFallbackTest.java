package com.alibaba.csp.sentinel.annotation.aspectj.integration;

import com.alibaba.csp.sentinel.annotation.aspectj.integration.config.AopGlobalFallBackConfig;
import com.alibaba.csp.sentinel.annotation.aspectj.integration.service.FooService;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author luffy
 */

@ContextConfiguration(classes = {SentinelAnnotationGlobalFallbackTest.class, AopGlobalFallBackConfig.class})
public class SentinelAnnotationGlobalFallbackTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private FooService fooService;

    @Test
    public void testAnnotationGlobalFallback() throws Exception {
        assertThat(fooService.fooWithAnnotationGlobalFallback(1)).isEqualTo("Hello for 1");
        // Fallback should take effect.
        assertThat(fooService.fooWithAnnotationGlobalFallback(5758)).isEqualTo("AnnotationGlobalFallback");

    }



    @Before
    public void setUp() throws Exception {
        FlowRuleManager.loadRules(new ArrayList<FlowRule>());
        ClusterBuilderSlot.resetClusterNodes();
    }

    @After
    public void tearDown() throws Exception {
        FlowRuleManager.loadRules(new ArrayList<FlowRule>());
        ClusterBuilderSlot.resetClusterNodes();
    }
}
