package com.alibaba.csp.sentinel.slots.block.degrade.cb;

import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for circuit breakers.
 *
 * @author Eric Zhao
 */
public class CircuitBreakerTest {

    @Test
    public void testExceptionRatioCircuitBreaker() throws Exception {
        final String resName = "testDegrade_exceptionRatio";
        final int timeout = 2;
        final double ratio = 0.15;

        ClusterNode cn = mock(ClusterNode.class);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(resName, EntryType.IN), cn);

        DegradeRule rule = new DegradeRule();
        rule.setResource(resName);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION);
        rule.setTimeWindow(timeout);
        rule.setCount(ratio);

        CircuitBreaker cb = new ExceptionRatioCircuitBreaker(rule);

        when(cn.exceptionQps()).thenReturn(2L);
        // Indicates that there are QPS more than min threshold.
        when(cn.totalQps()).thenReturn(12L);
        when(cn.successQps()).thenReturn(8L);

        // Will fail.
        assertFalse(cb.canPass());

        // Restore from the degrade timeout.
        TimeUnit.SECONDS.sleep(timeout + 1);

        when(cn.successQps()).thenReturn(20L);
        // Will pass.
        assertTrue(cb.canPass());
    }

    @Test
    public void testRtCircuitBreaker() throws Exception {
        final String resName = "testDegrade_averageRT";
        final int timeout = 2;
        final long rt = 3;

        ClusterNode cn = mock(ClusterNode.class);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(resName, EntryType.IN), cn);

        DegradeRule rule = new DegradeRule();
        rule.setResource(resName);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rule.setTimeWindow(timeout);
        rule.setCount(rt);

        CircuitBreaker cb = new RtCircuitBreaker(rule);

        when(cn.avgRt()).thenReturn(rt - 1);

        // Always can pass
        for (int i = 0; i < 10; i++) {
            assertTrue(cb.canPass());
        }

        when(cn.avgRt()).thenReturn(rt + 2);
        assertTrue(cb.canPass());
        assertTrue(cb.canPass());
        assertTrue(cb.canPass());
        // After a small RT peek, it recovered. Then it won't fail.
        when(cn.avgRt()).thenReturn(rt - 1);
        assertTrue(cb.canPass());
        assertTrue(cb.canPass());
        assertTrue(cb.canPass());

        // Exceed the rt threshold, the fifth time and later will fail.
        when(cn.avgRt()).thenReturn(rt + 2);

        for (int i = 0; i < 4; i++) {
            assertTrue(cb.canPass());
        }
        assertFalse(cb.canPass());
        assertFalse(cb.canPass());

        TimeUnit.SECONDS.sleep(timeout + 1);
        // Will recover.
        assertTrue(cb.canPass());
    }
}