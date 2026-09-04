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
package com.alibaba.csp.sentinel.slotchain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.context.Context;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test cases for {@link DefaultProcessorSlotChain}.
 */
public class DefaultProcessorSlotChainTest {

    @Test
    public void testSharedPredecessorDoesNotOverwritePrototypeSuccessor() throws Throwable {
        List<String> invocations = new ArrayList<>();
        RecordingSlot singletonB = new RecordingSlot("B", invocations);
        RecordingSlot prototypeA1 = new RecordingSlot("A1", invocations);
        RecordingSlot prototypeA2 = new RecordingSlot("A2", invocations);

        ProcessorSlotChain chain1 = new DefaultProcessorSlotChain();
        chain1.addLast(singletonB);
        chain1.addLast(prototypeA1);

        ProcessorSlotChain chain2 = new DefaultProcessorSlotChain();
        chain2.addLast(singletonB);
        chain2.addLast(prototypeA2);

        chain1.entry(null, null, null, 1, false);

        assertEquals(Arrays.asList("B", "A1"), invocations);

        invocations.clear();
        chain1.exit(null, null, 1);
        assertEquals(Arrays.asList("B-exit", "A1-exit"), invocations);

        ProcessorSlotContext<?> contextB1 = (ProcessorSlotContext<?>) chain1.getNext();
        ProcessorSlotContext<?> contextB2 = (ProcessorSlotContext<?>) chain2.getNext();
        assertNotSame(contextB1, contextB2);
        assertSame(singletonB, contextB1.getDelegate());
        assertSame(singletonB, contextB2.getDelegate());
    }

    @Test
    public void testScopedContextSupportsNestedChains() throws Throwable {
        List<String> invocations = new ArrayList<>();

        ProcessorSlotChain nested = new DefaultProcessorSlotChain();
        nested.addLast(new RecordingSlot("nested-B", invocations));
        nested.addLast(new RecordingSlot("nested-A", invocations));

        ProcessorSlotChain outer = new DefaultProcessorSlotChain();
        outer.addLast(new NestedSlot("outer-B", invocations, nested));
        outer.addLast(new RecordingSlot("outer-A", invocations));

        outer.entry(null, null, null, 1, false);

        assertEquals(Arrays.asList("outer-B", "nested-B", "nested-A", "outer-A"), invocations);
        assertFalse(AbstractLinkedProcessorSlot.hasActiveChainContext());
    }

    @Test
    public void testScopedContextSupportsRepeatedFireEntry() throws Throwable {
        List<String> invocations = new ArrayList<>();
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();
        chain.addLast(new DoubleFireSlot("B", invocations));
        chain.addLast(new RecordingSlot("A", invocations));

        chain.entry(null, null, null, 1, false);

        assertEquals(Arrays.asList("B", "A", "A"), invocations);
        assertFalse(AbstractLinkedProcessorSlot.hasActiveChainContext());
    }

    @Test
    public void testScopedContextIsRestoredAfterException() throws Throwable {
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();
        chain.addLast(new ThrowingSlot());

        try {
            chain.entry(null, null, null, 1, false);
            fail("Should throw the expected exception");
        } catch (IllegalStateException expected) {
            assertEquals("expected", expected.getMessage());
        }
        assertFalse(AbstractLinkedProcessorSlot.hasActiveChainContext());

        List<String> invocations = new ArrayList<>();
        RecordingSlot directB = new RecordingSlot("direct-B", invocations);
        directB.setNext(new RecordingSlot("direct-A", invocations));
        directB.entry(null, null, null, 1, false);
        assertEquals(Arrays.asList("direct-B", "direct-A"), invocations);
    }

    @Test
    public void testConcurrentChainsKeepIndependentSuccessors() throws Exception {
        int chainCount = 32;
        int invocationCount = 100;
        AtomicInteger sharedCount = new AtomicInteger();
        CountingSlot singletonB = new CountingSlot(sharedCount);
        List<AtomicInteger> prototypeCounts = new ArrayList<>(chainCount);
        List<ProcessorSlotChain> chains = new ArrayList<>(chainCount);

        for (int i = 0; i < chainCount; i++) {
            AtomicInteger prototypeCount = new AtomicInteger();
            ProcessorSlotChain chain = new DefaultProcessorSlotChain();
            chain.addLast(singletonB);
            chain.addLast(new CountingSlot(prototypeCount));
            prototypeCounts.add(prototypeCount);
            chains.add(chain);
        }

        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>(chainCount);
        try {
            for (final ProcessorSlotChain chain : chains) {
                futures.add(executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (int i = 0; i < invocationCount; i++) {
                                chain.entry(null, null, null, 1, false);
                            }
                        } catch (Throwable t) {
                            throw new AssertionError(t);
                        }
                        if (AbstractLinkedProcessorSlot.hasActiveChainContext()) {
                            throw new AssertionError("Chain context leaked from invocation");
                        }
                    }
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        assertEquals(chainCount * invocationCount, sharedCount.get());
        for (AtomicInteger prototypeCount : prototypeCounts) {
            assertEquals(invocationCount, prototypeCount.get());
        }
    }

    private static class RecordingSlot extends AbstractLinkedProcessorSlot<Object> {

        private final String name;
        private final List<String> invocations;

        private RecordingSlot(String name, List<String> invocations) {
            this.name = name;
            this.invocations = invocations;
        }

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, Object param, int count,
                          boolean prioritized, Object... args) throws Throwable {
            invocations.add(name);
            fireEntry(context, resourceWrapper, param, count, prioritized, args);
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
            invocations.add(name + "-exit");
            fireExit(context, resourceWrapper, count, args);
        }

        protected void recordEntry() {
            invocations.add(name);
        }
    }

    private static class NestedSlot extends RecordingSlot {

        private final ProcessorSlotChain nested;

        private NestedSlot(String name, List<String> invocations, ProcessorSlotChain nested) {
            super(name, invocations);
            this.nested = nested;
        }

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, Object param, int count,
                          boolean prioritized, Object... args) throws Throwable {
            recordEntry();
            nested.entry(context, resourceWrapper, param, count, prioritized, args);
            fireEntry(context, resourceWrapper, param, count, prioritized, args);
        }
    }

    private static class DoubleFireSlot extends RecordingSlot {

        private DoubleFireSlot(String name, List<String> invocations) {
            super(name, invocations);
        }

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, Object param, int count,
                          boolean prioritized, Object... args) throws Throwable {
            recordEntry();
            fireEntry(context, resourceWrapper, param, count, prioritized, args);
            fireEntry(context, resourceWrapper, param, count, prioritized, args);
        }
    }

    private static class ThrowingSlot extends AbstractLinkedProcessorSlot<Object> {

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, Object param, int count,
                          boolean prioritized, Object... args) {
            throw new IllegalStateException("expected");
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
            // No-op.
        }
    }

    private static class CountingSlot extends AbstractLinkedProcessorSlot<Object> {

        private final AtomicInteger counter;

        private CountingSlot(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, Object param, int count,
                          boolean prioritized, Object... args) throws Throwable {
            counter.incrementAndGet();
            fireEntry(context, resourceWrapper, param, count, prioritized, args);
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
            fireExit(context, resourceWrapper, count, args);
        }
    }
}
