package com.alibaba.csp.sentinel.slots.adaptive;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.spi.Spi;

/**
 * Adaptive Flow Control and Flow Limiting
 *
 * @author ElonTusk
 * @name AdaptiveSlot
 * @date 2023/8/2 13:12
 */
@Spi(order = Constants.ORDER_ADAPTIVE_SLOT)
public class AdaptiveSlot extends AbstractLinkedProcessorSlot<DefaultNode> {
    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        AdaptiveRuleManager.adaptiveLimit(resourceWrapper, node, count, prioritized);
        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }

}
