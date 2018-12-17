/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.csp.sentinel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

/**
 * @author Carpenter Lee
 */
public class TracerTest extends Tracer {

    @Test
    public void testTrace() throws BlockException {
        String url = "/exception";
        ContextUtil.enter(url);
        Entry entry = SphU.entry(url);
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertEquals(0, cn.exceptionQps(), 0.1);
        Tracer.trace(new RuntimeException());
        assertEquals(1, cn.exceptionQps(), 0.1);
        Tracer.trace(new RuntimeException(), 2);
        assertEquals(3, cn.exceptionQps(), 0.1);
        entry.exit();
        ContextUtil.exit();
    }
    
    @Test
    public void testTraceWithDiffContext() throws BlockException {
        String url = "/exceptionDiff";
        Context ctx1 = ContextUtil.enter(url);
        Entry entry1 = SphU.entry(url);
        ContextUtil.exitByAsync();
        Context ctx2 = ContextUtil.enter(url);
        Entry entry2 = SphU.entry(url);
        ContextUtil.exitByAsync();
        assertNotEquals(ctx1, ctx2);
        Tracer.traceContext(new RuntimeException(), 1, ctx1);
        Tracer.traceContext(new RuntimeException(), 2, ctx2);
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertEquals(2, cn.curThreadNum());
        assertEquals(3, cn.exceptionQps(), 0.1);
        entry1.exit();
        entry2.exit();
    }

    @Test
    public void setExceptionsToTrace() {
        Tracer.ignoreClasses = null;
        Tracer.traceClasses = null;
        Tracer.setExceptionsToTrace(TraceException.class, TraceException2.class);
        Assert.assertTrue(Tracer.shouldTrace(new TraceException2()));
        Assert.assertTrue(Tracer.shouldTrace(new TraceExceptionSub()));
        Assert.assertFalse(Tracer.shouldTrace(new Exception()));
        Tracer.resetExceptionsToTrace();
    }

    @Test
    public void setExceptionsToIgnore() {
        Tracer.ignoreClasses = null;
        Tracer.traceClasses = null;
        Tracer.setExceptionsToIgnore(IgnoreException.class, IgnoreException2.class);
        Assert.assertFalse(Tracer.shouldTrace(new IgnoreException()));
        Assert.assertFalse(Tracer.shouldTrace(new IgnoreExceptionSub()));
        Assert.assertTrue(Tracer.shouldTrace(new Exception()));
        Tracer.resetExceptionsToIgnore();
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
        Tracer.resetExceptionsToTrace();
        Tracer.resetExceptionsToIgnore();
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

    private class TraceException extends Exception { private static final long serialVersionUID = 1L; }

    private class TraceException2 extends Exception { private static final long serialVersionUID = 1L; }

    private class TraceExceptionSub extends TraceException { private static final long serialVersionUID = 1L; }

    private class IgnoreException extends Exception { private static final long serialVersionUID = 1L; }

    private class IgnoreException2 extends Exception { private static final long serialVersionUID = 1L; }

    private class IgnoreExceptionSub extends IgnoreException { private static final long serialVersionUID = 1L; }

    private class BothException extends Exception { private static final long serialVersionUID = 1L; }
}

