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

import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotContext;
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
        ProcessorSlotContext<?> nodeSelectorContext = assertContext(next, NodeSelectorSlot.class);

        // Store the first NodeSelectorSlot instance
        NodeSelectorSlot nodeSelectorSlot = (NodeSelectorSlot) nodeSelectorContext.getDelegate();

        next = next.getNext();
        assertContext(next, ClusterBuilderSlot.class);

        next = next.getNext();
        ProcessorSlotContext<?> logContext = assertContext(next, LogSlot.class);
        LogSlot logSlot = (LogSlot) logContext.getDelegate();

        next = next.getNext();
        assertContext(next, StatisticSlot.class);

        next = next.getNext();
        assertContext(next, AuthoritySlot.class);

        next = next.getNext();
        assertContext(next, SystemSlot.class);

        next = next.getNext();
        assertContext(next, FlowSlot.class);

        next = next.getNext();
        assertContext(next, DefaultCircuitBreakerSlot.class);

        next = next.getNext();
        assertContext(next, DegradeSlot.class);

        next = next.getNext();
        assertNull(next);

        // Build again to verify different instances
        ProcessorSlotChain slotChain2 = builder.build();
        assertNotNull(slotChain2);
        // Verify the two ProcessorSlotChain instances are different
        assertNotSame(slotChain, slotChain2);

        next = slotChain2.getNext();
        ProcessorSlotContext<?> nodeSelectorContext2 = assertContext(next, NodeSelectorSlot.class);
        assertNotSame(nodeSelectorContext, nodeSelectorContext2);
        // Store the second NodeSelectorSlot instance
        NodeSelectorSlot nodeSelectorSlot2 = (NodeSelectorSlot) nodeSelectorContext2.getDelegate();
        // Verify the two NodeSelectorSlot instances are different
        assertNotSame(nodeSelectorSlot, nodeSelectorSlot2);

        next = next.getNext().getNext();
        ProcessorSlotContext<?> logContext2 = assertContext(next, LogSlot.class);
        assertNotSame(logContext, logContext2);
        assertSame(logSlot, logContext2.getDelegate());
    }

    private ProcessorSlotContext<?> assertContext(AbstractLinkedProcessorSlot<?> slot,
                                                   Class<?> delegateClass) {
        assertTrue(slot instanceof ProcessorSlotContext);
        ProcessorSlotContext<?> context = (ProcessorSlotContext<?>) slot;
        assertTrue(delegateClass.isInstance(context.getDelegate()));
        return context;
    }
}
