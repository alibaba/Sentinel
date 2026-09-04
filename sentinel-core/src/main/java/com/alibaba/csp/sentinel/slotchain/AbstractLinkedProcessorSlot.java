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
 * @author qinan.qn
 * @author jialiang.linjl
 */
public abstract class AbstractLinkedProcessorSlot<T> implements ProcessorSlot<T> {

    private static final ThreadLocal<ChainContext> CHAIN_CONTEXT = new ThreadLocal<>();

    private AbstractLinkedProcessorSlot<?> next = null;

    @Override
    public void fireEntry(Context context, ResourceWrapper resourceWrapper, Object obj, int count, boolean prioritized, Object... args)
        throws Throwable {
        AbstractLinkedProcessorSlot<?> next = getNext();
        if (next != null) {
            next.transformEntry(context, resourceWrapper, obj, count, prioritized, args);
        }
    }

    @SuppressWarnings("unchecked")
    void transformEntry(Context context, ResourceWrapper resourceWrapper, Object o, int count, boolean prioritized, Object... args)
        throws Throwable {
        T t = (T)o;
        entry(context, resourceWrapper, t, count, prioritized, args);
    }

    @Override
    public void fireExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        AbstractLinkedProcessorSlot<?> next = getNext();
        if (next != null) {
            next.exit(context, resourceWrapper, count, args);
        }
    }

    public AbstractLinkedProcessorSlot<?> getNext() {
        ChainContext context = CHAIN_CONTEXT.get();
        if (context != null && context.source == this) {
            return context.next;
        }
        return next;
    }

    public void setNext(AbstractLinkedProcessorSlot<?> next) {
        this.next = next;
    }

    static ChainContext setChainContext(AbstractLinkedProcessorSlot<?> source,
                                        AbstractLinkedProcessorSlot<?> next) {
        ChainContext previous = CHAIN_CONTEXT.get();
        CHAIN_CONTEXT.set(new ChainContext(source, next));
        return previous;
    }

    static void restoreChainContext(ChainContext previous) {
        if (previous == null) {
            CHAIN_CONTEXT.remove();
        } else {
            CHAIN_CONTEXT.set(previous);
        }
    }

    static boolean hasActiveChainContext() {
        return CHAIN_CONTEXT.get() != null;
    }

    static final class ChainContext {

        private final AbstractLinkedProcessorSlot<?> source;
        private final AbstractLinkedProcessorSlot<?> next;

        private ChainContext(AbstractLinkedProcessorSlot<?> source, AbstractLinkedProcessorSlot<?> next) {
            this.source = source;
            this.next = next;
        }
    }

}
