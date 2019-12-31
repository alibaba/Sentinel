package com.alibaba.csp.sentinel.slotchain;

/**
 * The extender for processor slot chain.
 */
public interface SlotChainExtender {
    /**
     * extend the existed processor slot chain.
     * @return
     */
    ProcessorSlotChain extend(ProcessorSlotChain slotChain);
}
