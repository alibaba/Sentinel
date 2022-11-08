package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.spi.Spi;

import java.util.List;

/**
 * @author wuwen
 */
@Spi(order = Constants.ORDER_DEGRADE_SLOT + 100)
public class DefaultDegradeSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        performChecking(context, resourceWrapper);

        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    private void performChecking(Context context, ResourceWrapper r) throws BlockException {

        if (DegradeRuleManager.hasConfig(r.getName())) {
            return;
        }

        List<CircuitBreaker> circuitBreakers = DefaultDegradeRuleManager.getDefaultCircuitBreakers(r.getName());

        if (circuitBreakers == null || circuitBreakers.isEmpty()) {
            return;
        }

        for (CircuitBreaker cb : circuitBreakers) {
            if (!cb.tryPass(context)) {
                throw new DegradeException(cb.getRule().getLimitApp(), cb.getRule());
            }
        }
    }

    @Override
    public void exit(Context context, ResourceWrapper r, int count, Object... args) {
        Entry curEntry = context.getCurEntry();
        if (curEntry.getBlockError() != null) {
            fireExit(context, r, count, args);
            return;
        }

        if (DegradeRuleManager.hasConfig(r.getName())) {
            fireExit(context, r, count, args);
            return;
        }

        List<CircuitBreaker> circuitBreakers = DefaultDegradeRuleManager.getDefaultCircuitBreakers(r.getName());

        if (circuitBreakers == null || circuitBreakers.isEmpty()) {
            return;
        }

        if (curEntry.getBlockError() == null) {
            // passed request
            for (CircuitBreaker circuitBreaker : circuitBreakers) {
                circuitBreaker.onRequestComplete(context);
            }
        }

        fireExit(context, r, count, args);
    }
}
