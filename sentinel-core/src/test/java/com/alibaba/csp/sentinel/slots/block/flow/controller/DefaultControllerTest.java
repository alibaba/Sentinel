package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Eric Zhao
 */
public class DefaultControllerTest {

    @Test
    public void testCanPassForQps() {
        double threshold = 10;
        TrafficShapingController controller = new DefaultController(threshold, RuleConstant.FLOW_GRADE_QPS);
        Node node = mock(Node.class);
        when(node.passQps()).thenReturn(threshold - 1)
            .thenReturn(threshold);

        assertTrue(controller.canPass(node, 1));
        assertFalse(controller.canPass(node, 1));
    }

    @Test
    public void testCanPassForThreadCount() {
        int threshold = 8;
        TrafficShapingController controller = new DefaultController(threshold, RuleConstant.FLOW_GRADE_THREAD);
        Node node = mock(Node.class);
        when(node.curThreadNum()).thenReturn(threshold - 1)
            .thenReturn(threshold);

        assertTrue(controller.canPass(node, 1));
        assertFalse(controller.canPass(node, 1));
    }

    @Test
    public void testCanPassForQpsMultiThread() {
    }
}