package com.alibaba.csp.sentinel.adapter.reactor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class MonoSentinelOperatorIntegrationTest {

    @Test
    public void testTransformMonoWithSentinelContextEnter() {
        String resourceName = createResourceName("testTransformMonoWithSentinelContextEnter");
        String contextName = "test_reactive_context";
        String origin = "originA";
        FlowRuleManager.loadRules(Collections.singletonList(
            new FlowRule(resourceName).setCount(0).setLimitApp(origin).as(FlowRule.class)
        ));
        StepVerifier.create(Mono.just(2)
            .transform(new SentinelReactorTransformer<>(
                // Customized context with origin.
                new EntryConfig(resourceName, EntryType.OUT, new ContextConfig(contextName, origin))))
        )
            .expectError(BlockException.class)
            .verify();

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
        assertEquals(1, cn.blockRequest());
        assertTrue(Constants.ROOT.getChildList()
            .stream()
            .filter(node -> node instanceof EntranceNode)
            .map(e -> (EntranceNode)e)
            .anyMatch(e -> e.getId().getName().equals(contextName))
        );

        FlowRuleManager.loadRules(new ArrayList<>());
    }

    @Test
    public void testFluxToMonoNextThenCancelSuccess() {
        String resourceName = createResourceName("testFluxToMonoNextThenCancelSuccess");
        StepVerifier.create(Flux.range(1, 10)
            .map(e -> e * 2)
            .next()
            .transform(new SentinelReactorTransformer<>(resourceName)))
            .expectNext(2)
            .verifyComplete();

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testEmitSingleLongTimeRt() {
        String resourceName = createResourceName("testEmitSingleLongTimeRt");
        StepVerifier.create(Mono.just(2)
            .delayElement(Duration.ofMillis(1000))
            .map(e -> e * 2)
            .transform(new SentinelReactorTransformer<>(resourceName)))
            .expectNext(4)
            .verifyComplete();

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1000, cn.avgRt(), 20);
    }

    @Test
    public void testEmitEmptySuccess() {
        String resourceName = createResourceName("testEmitEmptySuccess");
        StepVerifier.create(Mono.empty()
            .transform(new SentinelReactorTransformer<>(resourceName)))
            .verifyComplete();

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testEmitSingleSuccess() {
        String resourceName = createResourceName("testEmitSingleSuccess");
        StepVerifier.create(Mono.just(1)
            .transform(new SentinelReactorTransformer<>(resourceName)))
            .expectNext(1)
            .verifyComplete();

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testEmitSingleValueWhenFlowControlTriggered() {
        String resourceName = createResourceName("testEmitSingleValueWhenFlowControlTriggered");
        FlowRuleManager.loadRules(Collections.singletonList(
            new FlowRule(resourceName).setCount(0)
        ));
        StepVerifier.create(Mono.just(1)
            .map(e -> e * 2)
            .transform(new SentinelReactorTransformer<>(resourceName)))
            .expectError(BlockException.class)
            .verify();

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
        assertEquals(1, cn.blockRequest());

        FlowRuleManager.loadRules(new ArrayList<>());
    }

    @Test
    public void testEmitExceptionWhenFlowControlTriggered() {
        String resourceName = createResourceName("testEmitExceptionWhenFlowControlTriggered");
        FlowRuleManager.loadRules(Collections.singletonList(
            new FlowRule(resourceName).setCount(0)
        ));
        StepVerifier.create(Mono.error(new IllegalStateException("some"))
            .transform(new SentinelReactorTransformer<>(resourceName)))
            .expectError(BlockException.class)
            .verify();

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(0, cn.passQps(), 0.01);
        assertEquals(1, cn.blockRequest());

        FlowRuleManager.loadRules(new ArrayList<>());
    }

    @Test
    public void testEmitSingleError() {
        String resourceName = createResourceName("testEmitSingleError");
        StepVerifier.create(Mono.error(new IllegalStateException())
            .transform(new SentinelReactorTransformer<>(resourceName)))
            .expectError(IllegalStateException.class)
            .verify();

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.totalException());
    }

    private String createResourceName(String resourceName) {
        return "reactor_test_mono_" + resourceName;
    }
}