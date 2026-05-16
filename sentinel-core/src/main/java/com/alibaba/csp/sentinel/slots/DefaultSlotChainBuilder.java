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

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.DefaultProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.SlotChainBuilder;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.List;

/**
 * Builder for a default {@link ProcessorSlotChain}.
 *
 * @author qinan.qn
 * @author leyou
 */
@Spi(isDefault = true)
public class DefaultSlotChainBuilder implements SlotChainBuilder {

    @Override
    public ProcessorSlotChain build() {
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();

        List<ProcessorSlot> sortedSlotList = SpiLoader.of(ProcessorSlot.class).loadInstanceListSorted();
        for (ProcessorSlot slot : sortedSlotList) {
            if (!(slot instanceof AbstractLinkedProcessorSlot)) {
                RecordLog.warn("The ProcessorSlot(" + slot.getClass().getCanonicalName() + ") is not an instance of AbstractLinkedProcessorSlot, can't be added into ProcessorSlotChain");
                continue;
            }

            // Always create a new instance of the slot to ensure each chain has its own
            // copy with independent next pointers. This is necessary because singleton slots
            // (isSingleton=true in @Spi annotation) are shared across all build() calls.
            // If a singleton slot is directly used in multiple chains, modifying its 'next'
            // pointer when building one chain will affect all other chains that use it.
            // See: https://github.com/alibaba/Sentinel/issues/3007
            AbstractLinkedProcessorSlot<?> newSlot = newSlot(slot);
            chain.addLast(newSlot);
        }

        return chain;
    }

    /**
     * Create a new instance of the given slot.
     * <p>
     * This method creates a fresh instance of the slot's concrete class via reflection,
     * ensuring that each chain gets its own independent copy of every slot. This prevents
     * the issue where singleton slots shared across multiple chains would have their
     * {@code next} pointer overwritten by the last chain built.
     * </p>
     *
     * @param slot the slot to create a new instance of
     * @return a new instance of the slot, or the original slot if creation fails
     */
    @SuppressWarnings("unchecked")
    private static AbstractLinkedProcessorSlot<?> newSlot(ProcessorSlot slot) {
        try {
            return slot.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            RecordLog.warn("Failed to create new instance of ProcessorSlot("
                + slot.getClass().getCanonicalName() + "), using original instance", e);
            return (AbstractLinkedProcessorSlot<?>) slot;
        }
    }
}
