package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slots.block.flow.controller.WarmUpRateLimiterController;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author CarpenterLee
 */
public class WarmUpRateLimiterControllerTest {

    @Test
    public void testPace() throws InterruptedException {
        WarmUpRateLimiterController controller = new WarmUpRateLimiterController(10, 10, 1000, 3);

        Node node = mock(Node.class);

        when(node.passQps()).thenReturn(100d);
        when(node.previousPassQps()).thenReturn(100d);

        assertTrue(controller.canPass(node, 1));

        long start = System.currentTimeMillis();
        assertTrue(controller.canPass(node, 1));
        long cost = System.currentTimeMillis() - start;
        assertTrue(cost >= 100 && cost <= 120);
    }

    @Test
    public void testPaceCanNotPass() throws InterruptedException {
        WarmUpRateLimiterController controller = new WarmUpRateLimiterController(10, 10, 10, 3);

        Node node = mock(Node.class);

        when(node.passQps()).thenReturn(100d);
        when(node.previousPassQps()).thenReturn(100d);

        assertTrue(controller.canPass(node, 1));

        assertFalse(controller.canPass(node, 1));
    }
}