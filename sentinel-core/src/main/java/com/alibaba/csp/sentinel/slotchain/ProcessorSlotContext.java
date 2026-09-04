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

import com.alibaba.csp.sentinel.context.Context;

/**
 * A chain-local node that delegates processing to a {@link ProcessorSlot} while owning an independent
 * {@code next} reference.
 *
 * @param <T> type of the entry parameter
 */
public final class ProcessorSlotContext<T> extends AbstractLinkedProcessorSlot<T> {

    private final AbstractLinkedProcessorSlot<T> delegate;

    ProcessorSlotContext(AbstractLinkedProcessorSlot<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, T param, int count, boolean prioritized,
                      Object... args) throws Throwable {
        ChainContext previous = setChainContext(delegate, getNext());
        try {
            delegate.entry(context, resourceWrapper, param, count, prioritized, args);
        } finally {
            restoreChainContext(previous);
        }
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        ChainContext previous = setChainContext(delegate, getNext());
        try {
            delegate.exit(context, resourceWrapper, count, args);
        } finally {
            restoreChainContext(previous);
        }
    }

    /**
     * Get the SPI-managed slot that performs the actual processing.
     *
     * @return delegate slot
     */
    public AbstractLinkedProcessorSlot<T> getDelegate() {
        return delegate;
    }
}
