package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Carpenter Lee
 */
public class TracerTest extends Tracer {

    @Before
    public void setUp() {
        ContextTestUtil.cleanUpContext();
        ContextTestUtil.resetContextMap();
    }

    @After
    public void tearDown() {
        ContextTestUtil.cleanUpContext();
        ContextTestUtil.resetContextMap();
    }

    @Test
    public void testTraceWhenContextSizeExceedsThreshold() {
        int i = 0;
        for (; i < Constants.MAX_CONTEXT_NAME_SIZE; i++) {
            ContextUtil.enter("test-context-" + i);
            ContextUtil.exit();
        }

        try {
            ContextUtil.enter("test-context-" + i);
            throw new RuntimeException("test");
        } catch (Exception e) {
            Tracer.trace(e);
        } finally {
            ContextUtil.exit();
        }
    }

    @Test
    public void setExceptionsToTrace() {
        Tracer.ignoreClasses = null;
        Tracer.traceClasses = null;
        Tracer.setExceptionsToTrace(TraceException.class, TraceException2.class);
        Assert.assertTrue(Tracer.shouldTrace(new TraceException2()));
        Assert.assertTrue(Tracer.shouldTrace(new TraceExceptionSub()));
        Assert.assertFalse(Tracer.shouldTrace(new Exception()));
    }

    @Test
    public void setExceptionsToIgnore() {
        Tracer.ignoreClasses = null;
        Tracer.traceClasses = null;
        Tracer.setExceptionsToIgnore(IgnoreException.class, IgnoreException2.class);
        Assert.assertFalse(Tracer.shouldTrace(new IgnoreException()));
        Assert.assertFalse(Tracer.shouldTrace(new IgnoreExceptionSub()));
        Assert.assertTrue(Tracer.shouldTrace(new Exception()));
    }

    @Test
    public void testBoth() {
        Tracer.ignoreClasses = null;
        Tracer.traceClasses = null;
        Tracer.setExceptionsToTrace(TraceException.class, TraceException2.class, BothException.class);
        Tracer.setExceptionsToIgnore(IgnoreException.class, IgnoreException2.class, BothException.class);
        Assert.assertFalse(Tracer.shouldTrace(new IgnoreException()));
        Assert.assertFalse(Tracer.shouldTrace(new BothException()));
        Assert.assertTrue(Tracer.shouldTrace(new TraceException()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        Tracer.setExceptionsToTrace(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull1() {
        Tracer.setExceptionsToTrace(TraceException.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull2() {
        Tracer.setExceptionsToIgnore(IgnoreException.class, null);
    }

    private class TraceException extends Exception {}

    private class TraceException2 extends Exception {}

    private class TraceExceptionSub extends TraceException {}

    private class IgnoreException extends Exception {}

    private class IgnoreException2 extends Exception {}

    private class IgnoreExceptionSub extends IgnoreException {}

    private class BothException extends Exception {}
}