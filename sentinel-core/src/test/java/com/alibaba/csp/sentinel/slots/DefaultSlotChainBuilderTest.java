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
package com.alibaba.csp.sentinel.slots;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.authority.AuthoritySlot;
import com.alibaba.csp.sentinel.slots.block.degrade.DefaultCircuitBreakerSlot;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot;
import com.alibaba.csp.sentinel.slots.block.flow.FlowSlot;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.slots.logger.LogSlot;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlot;
import com.alibaba.csp.sentinel.slots.system.SystemSlot;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link DefaultSlotChainBuilder}.
 *
 * @author cdfive
 */
public class DefaultSlotChainBuilderTest {

    @Test
    public void testBuild() {
        DefaultSlotChainBuilder builder = new DefaultSlotChainBuilder();
        ProcessorSlotChain slotChain = builder.build();
        assertNotNull(slotChain);

        // Verify the order of slot
        AbstractLinkedProcessorSlot<?> next = slotChain.getNext();
        assertTrue(next instanceof NodeSelectorSlot);

        // Store the first NodeSelectorSlot instance
        NodeSelectorSlot nodeSelectorSlot = (NodeSelectorSlot) next;

        next = next.getNext();
        assertTrue(next instanceof ClusterBuilderSlot);

        next = next.getNext();
        assertTrue(next instanceof LogSlot);

        next = next.getNext();
        assertTrue(next instanceof StatisticSlot);

        next = next.getNext();
        assertTrue(next instanceof AuthoritySlot);

        next = next.getNext();
        assertTrue(next instanceof SystemSlot);

        next = next.getNext();
        assertTrue(next instanceof FlowSlot);

        next = next.getNext();
        assertTrue(next instanceof DefaultCircuitBreakerSlot);

        next = next.getNext();
        assertTrue(next instanceof DegradeSlot);

        next = next.getNext();
        assertNull(next);

        // Build again to verify different instances
        ProcessorSlotChain slotChain2 = builder.build();
        assertNotNull(slotChain2);
        // Verify the two ProcessorSlotChain instances are different
        assertNotSame(slotChain, slotChain2);

        next = slotChain2.getNext();
        assertTrue(next instanceof NodeSelectorSlot);
        // Store the second NodeSelectorSlot instance
        NodeSelectorSlot nodeSelectorSlot2 = (NodeSelectorSlot) next;
        // Verify the two NodeSelectorSlot instances are different
        assertNotSame(nodeSelectorSlot, nodeSelectorSlot2);
    }

    @Test
    public void testAddSingletonSlotInDifferentChains() {
        ProcessorSlotChain chain = new ProcessorSlotChain();
        TestSlot singletonSlot = new TestSlot();
        TestSlot firstNext = new TestSlot();
        TestSlot secondNext = new TestSlot();

        chain.addLast(singletonSlot);
        chain.addLast(firstNext);

        ProcessorSlotChain chain2 = new ProcessorSlotChain();
        chain2.addLast(singletonSlot);
        chain2.addLast(secondNext);

        AbstractLinkedProcessorSlot<?> next = chain.getNext().getNext();
        assertSame(firstNext, next.getNext());

        next = chain2.getNext().getNext();
        assertSame(secondNext, next.getNext());
        assertNotSame(firstNext, secondNext);
    }

    private static class TestSlot extends AbstractLinkedProcessorSlot<Object> {

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, Object t, int count, boolean prioritized,
                          Object... args) {
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        }
    }
}
