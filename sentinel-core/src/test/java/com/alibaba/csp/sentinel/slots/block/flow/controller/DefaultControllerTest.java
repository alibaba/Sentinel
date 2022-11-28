package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.LongAdder;

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
        int threshold = 1;
        TrafficShapingController controller = new DefaultController(threshold, RuleConstant.FLOW_GRADE_THREAD);
        Node node = mock(Node.class);
        assertTrue(controller.canPass(node, 1));
        assertFalse(controller.canPass(node, 1));
    }

    @Test
    public void testCanPassForThreadCountMultiThread() throws InterruptedException {
        int threshold = 8;
        TrafficShapingController controller = new DefaultController(threshold, RuleConstant.FLOW_GRADE_THREAD);
        Node node = mock(Node.class);
        
        int testThreadCount = threshold + 5;
        CountDownLatch latch = new CountDownLatch(testThreadCount);
        CyclicBarrier barrier = new CyclicBarrier(testThreadCount);
        LongAdder passed = new LongAdder();
        
        Runnable runner = () -> {
            try {
                if (controller.canPass(node, 1)) {
                    passed.increment();
                }
                barrier.await();
            } catch (Exception e) {
            } finally {                
                controller.cleanUpEffect(node, 1);
            }
            latch.countDown();
        };
        
        for (int i = 0; i < testThreadCount; i++) {
            new Thread(runner).start();
        }
        latch.await();
        assertEquals(threshold, passed.intValue());
    }
    
    @Test
    public void testCanPassForQpsMultiThread() {
    }
}