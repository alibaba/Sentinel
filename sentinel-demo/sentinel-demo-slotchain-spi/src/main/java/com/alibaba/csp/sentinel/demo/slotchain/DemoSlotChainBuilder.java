/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.slotchain;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.*;
import com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.List;

/**
 * A demo {@link SlotChainBuilder} for build custom slot chain.
 * Two ways to build slot chain are demonstrated.
 *
 * Pay attention to that `ProcessorSlotChain` is not a SPI, but the `SlotChainBuilder`.
 *
 * Most of the time, we don't need to customize `SlotChainBuilder`,
 * maybe customize `ProcessorSlot` is enough, refer to `sentinel-demo-slot-spi` module.
 *
 * Note that the sentinel's default slots and the order of them are very important, be careful when customizing,
 * refer to the constants for slot order definitions in {@link Constants}.
 * You may also refer to {@link DefaultSlotChainBuilder}.
 *
 * @author cdfive
 */
@Spi
public class DemoSlotChainBuilder implements SlotChainBuilder {

    @Override
    public ProcessorSlotChain build() {
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();

        List<ProcessorSlot> sortedSlotList = SpiLoader.of(ProcessorSlot.class).loadInstanceListSorted();
        // Filter out `DegradeSlot`
        // Test for `DemoDegradeRuleApplication`, the demo will not be blocked by `DegradeException`
        sortedSlotList.removeIf(o -> DegradeSlot.class.equals(o.getClass()));
        for (ProcessorSlot slot : sortedSlotList) {
            if (!(slot instanceof AbstractLinkedProcessorSlot)) {
                RecordLog.warn("The ProcessorSlot(" + slot.getClass().getCanonicalName() + ") is not an instance of AbstractLinkedProcessorSlot, can't be added into ProcessorSlotChain");
                continue;
            }

            chain.addLast((AbstractLinkedProcessorSlot<?>) slot);
        }

        return chain;
    }

    /**
     * Another way to build the slot chain, add slot one by one with `SpiLoader#loadInstance`.
     * Note that the sentinel's default slots and the order of them are very important, be careful when customizing,
     * refer to the constants for slot order definitions in {@link com.alibaba.csp.sentinel.Constants}.
     */
    /*
    @Override
    public ProcessorSlotChain build() {
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();

        // Create a `SpiLoader` instance
        SpiLoader<ProcessorSlot> spiLoader = SpiLoader.of(ProcessorSlot.class);

        // Add `NodeSelectorSlot`, load by class
        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance(NodeSelectorSlot.class));

        // Add `ClusterBuilderSlot`, load by aliasname(default is classname)
        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance("com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot"));

        // Add `StatisticSlot`
        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance(StatisticSlot.class));

        // Add `FlowSlot`
        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance(FlowSlot.class));

        // Add `DegradeSlot`
        // Test for `DemoDegradeRuleApplication`
        // If we don't add `DegradeSlot`, the demo will not be blocked by `DegradeException`
        // If it's added, we can see the expected DegradeException
//        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance(DegradeSlot.class));
        return chain;
    }
    */
}
