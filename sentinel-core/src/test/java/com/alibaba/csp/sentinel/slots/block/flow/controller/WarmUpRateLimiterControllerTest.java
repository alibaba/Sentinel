package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.node.Node;
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

        // Easily fail in single request testing, so we increase it to 10 requests and test the average time
        long start = System.currentTimeMillis();
        int requests = 10;
        for (int i = 0; i < requests; i++) {
            assertTrue(controller.canPass(node, 1));
        }
        float cost = (System.currentTimeMillis() - start) / 1.0f / requests;
        assertTrue(Math.abs(cost - 100) < 10);
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