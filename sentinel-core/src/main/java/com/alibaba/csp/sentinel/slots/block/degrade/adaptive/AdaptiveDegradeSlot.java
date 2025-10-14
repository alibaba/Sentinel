package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.spi.Spi;

/**
 * A {@link ProcessorSlot} dedicates to adaptive circuit breaking.
 *
 * @author ylnxwlp
 */
@Spi(order = Constants.ORDER_ADAPTIVE_DEGRADE_SLOT)
public class AdaptiveDegradeSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        performChecking(context, resourceWrapper);

        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    void performChecking(Context context, ResourceWrapper r) throws BlockException {
        if(!AdaptiveDegradeRuleManager.getRule(r.getName()).isEnabled() || r.getEntryType() == EntryType.IN){
            return;
        }
        AdaptiveCircuitBreaker circuitBreaker = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(r.getName());
        if (circuitBreaker == null) {
            return;
        }
        if (!circuitBreaker.tryPass(context)) {
            String msg = "[AdaptiveDegradeSlot] The adaptive rule does not pass because : " + circuitBreaker.getScenario() +
                    ", current request pass probability : " + circuitBreaker.getProbability() +
                    ", current circuit breaker state: " + circuitBreaker.currentState();
            throw new AdaptiveDegradeException(msg,AdaptiveDegradeRuleManager.getRule(r.getName()));
        }
    }

    @Override
    public void exit(Context context, ResourceWrapper r, int count, Object... args) {
        Entry curEntry = context.getCurEntry();
        if (curEntry.getBlockError() != null) {
            fireExit(context, r, count, args);
            return;
        }
        CircuitBreaker circuitBreaker = AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker(r.getName());
        if (circuitBreaker == null) {
            fireExit(context, r, count, args);
            return;
        }
        if (curEntry.getBlockError() == null) {
            circuitBreaker.onRequestComplete(context);
        }
        fireExit(context, r, count, args);
    }
}
