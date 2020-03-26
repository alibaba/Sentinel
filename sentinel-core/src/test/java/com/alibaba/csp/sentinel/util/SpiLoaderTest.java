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
package com.alibaba.csp.sentinel.util;

import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.SlotChainBuilder;
import com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder;
import com.alibaba.csp.sentinel.slots.block.authority.AuthoritySlot;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot;
import com.alibaba.csp.sentinel.slots.block.flow.FlowSlot;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.slots.logger.LogSlot;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlot;
import com.alibaba.csp.sentinel.slots.system.SystemSlot;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for {@link SpiLoader}.
 *
 * @author cdfive
 */
public class SpiLoaderTest {

    @Test
    public void testLoadFirstInstance() {
        ProcessorSlot processorSlot = SpiLoader.loadFirstInstance(ProcessorSlot.class);
        assertNotNull(processorSlot);

        SlotChainBuilder slotChainBuilder = SpiLoader.loadFirstInstance(SlotChainBuilder.class);
        assertNotNull(slotChainBuilder);
        assertTrue(slotChainBuilder instanceof DefaultSlotChainBuilder);
    }

    @Test
    public void testLoadHighestPriorityInstance() {
        ProcessorSlot processorSlot = SpiLoader.loadHighestPriorityInstance(ProcessorSlot.class);
        assertNotNull(processorSlot);

        // NodeSelectorSlot is highest order with @SpiOrder(-9000), among all slots
        assertTrue(processorSlot instanceof NodeSelectorSlot);
    }

    @Test
    public void testLoadInstanceList() {
        List<ProcessorSlot> slots = SpiLoader.loadInstanceList(ProcessorSlot.class);
        assertNotNull(slots);

        // Total 8 default slot in sentinel-core
        assertEquals(8, slots.size());

        // Store the first slot of slots
        ProcessorSlot firstSlot = slots.get(0);

        // Call loadInstanceList again
        List<ProcessorSlot> slots2 = SpiLoader.loadInstanceList(ProcessorSlot.class);
        assertNotSame(slots, slots2);

        // Store the first slot of slots
        ProcessorSlot firstSlot2 = slots2.get(0);

        // As SERVICE_LOADER_MAP in SpiLoader cached the instance, so they're same instances
        assertSame(firstSlot, firstSlot2);
    }

    @Test
    public void testLoadInstanceListSorted() {
        List<ProcessorSlot> sortedSlots = SpiLoader.loadInstanceListSorted(ProcessorSlot.class);
        assertNotNull(sortedSlots);

        // Total 8 default slot in sentinel-core
        assertEquals(8, sortedSlots.size());

        // Verify the order of slot
        int index = 0;
        assertTrue(sortedSlots.get(index++) instanceof NodeSelectorSlot);
        assertTrue(sortedSlots.get(index++) instanceof ClusterBuilderSlot);
        assertTrue(sortedSlots.get(index++) instanceof LogSlot);
        assertTrue(sortedSlots.get(index++) instanceof StatisticSlot);
        assertTrue(sortedSlots.get(index++) instanceof AuthoritySlot);
        assertTrue(sortedSlots.get(index++) instanceof SystemSlot);
        assertTrue(sortedSlots.get(index++) instanceof FlowSlot);
        assertTrue(sortedSlots.get(index++) instanceof DegradeSlot);

        // Verify each call return different instances
        List<ProcessorSlot> sortedSlots2 = SpiLoader.loadInstanceListSorted(ProcessorSlot.class);
        assertNotSame(sortedSlots, sortedSlots2);
        assertEquals(sortedSlots.size(), sortedSlots2.size());
        for (int i = 0; i < sortedSlots.size(); i++) {
            ProcessorSlot slot = sortedSlots.get(i);
            ProcessorSlot slot2 = sortedSlots2.get(i);
            assertEquals(slot.getClass(), slot2.getClass());
        }
    }

    @Test
    public void testLoadDifferentInstanceListSorted() {
        List<ProcessorSlot> sortedSlots = SpiLoader.loadInstanceListSorted(ProcessorSlot.class);
        assertNotNull(sortedSlots);

        // Total 8 default slot in sentinel-core
        assertEquals(8, sortedSlots.size());

        // Verify the order of slot
        int index = 0;
        assertTrue(sortedSlots.get(index++) instanceof NodeSelectorSlot);
        assertTrue(sortedSlots.get(index++) instanceof ClusterBuilderSlot);
        assertTrue(sortedSlots.get(index++) instanceof LogSlot);
        assertTrue(sortedSlots.get(index++) instanceof StatisticSlot);
        assertTrue(sortedSlots.get(index++) instanceof AuthoritySlot);
        assertTrue(sortedSlots.get(index++) instanceof SystemSlot);
        assertTrue(sortedSlots.get(index++) instanceof FlowSlot);
        assertTrue(sortedSlots.get(index++) instanceof DegradeSlot);

        // Verify each call return different instances
        List<ProcessorSlot> sortedSlots2 = SpiLoader.loadDifferentInstanceListSorted(ProcessorSlot.class);
        assertNotSame(sortedSlots, sortedSlots2);
        assertEquals(sortedSlots.size(), sortedSlots2.size());
        for (int i = 0; i < sortedSlots.size(); i++) {
            ProcessorSlot slot = sortedSlots.get(i);
            ProcessorSlot slot2 = sortedSlots2.get(i);
            assertEquals(slot.getClass(), slot2.getClass());

            // Verify the instances are different
            assertNotSame(slot, slot2);
            assertNotEquals(slot, slot2);
        }
    }
}
