package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * @author joyce
 * @date 2026/5/14
 * @since 1.8.5
 **/
public class PrototypeSlotWrapper extends AbstractLinkedProcessorSlot<Object> {

    private final AbstractLinkedProcessorSlot delegate;

    public PrototypeSlotWrapper(AbstractLinkedProcessorSlot delegate) {
        this.delegate = delegate;
    }

    @Override
    public void fireEntry(Context context, ResourceWrapper resourceWrapper, Object obj, int count, boolean prioritized, Object... args) throws Throwable {
        this.delegate.fireEntry(context, resourceWrapper, obj, count, prioritized, args);
    }

    @Override
    public void fireExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        this.delegate.fireExit(context, resourceWrapper, count, args);
    }

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, Object param, int count, boolean prioritized, Object... args) throws Throwable {
        delegate.entry(context, resourceWrapper, param, count, prioritized, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        delegate.exit(context, resourceWrapper, count, args);
    }
}
