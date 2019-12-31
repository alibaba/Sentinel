package com.alibaba.csp.sentinel.cluster.redis;

import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.SlotChainExtender;

public class RedisClusterFlowSlotExtender implements SlotChainExtender {

    @Override
    public ProcessorSlotChain extend(ProcessorSlotChain slotChain) {
        slotChain.addLast(new RedisClusterFlowSlot());
        return slotChain;
    }
}
