package com.alibaba.csp.sentinel.slots.block.adaptive;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Liu Yiming
 * @date 2019-07-14 22:12
 */
public class AdaptiveSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    private final AdaptiveRuleChecker checker;

    public AdaptiveSlot() {
        this(new AdaptiveRuleChecker());
    }

    AdaptiveSlot(AdaptiveRuleChecker checker) {
        AssertUtil.notNull(checker, "Adaptive checker should not be null");
        this.checker = checker;
    }

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, boolean prioritized, Object... args)
        throws Throwable {
        checkAdaptive(resourceWrapper, context, node, count);
        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    void checkAdaptive(ResourceWrapper resource, Context context, DefaultNode node, int count)
        throws BlockException {
        checker.checkAdaptive(resource, context, node, count);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }
}
