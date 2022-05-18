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
package com.alibaba.csp.sentinel.slots.block.degrade.param;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.spi.Spi;
import java.util.List;

/**
 * A processor slot that is responsible for flow control by frequent ("hot spot") parameters.
 * This slot is between ParamFlowSlot and FlowSlot.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
@Spi(order = -2900)
public class ParamDegradeSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        if (!ParamDegradeRuleManager.hasRules(resourceWrapper.getName())) {
            fireEntry(context, resourceWrapper, node, count, prioritized, args);
            return;
        }

        performChecking(context, resourceWrapper, args);
        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        if (args == null || args.length == 0) {
            return;
        }

        if (!ParamDegradeRuleManager.hasRules(resourceWrapper.getName())) {
            return;
        }

        Entry curEntry = context.getCurEntry();
        if (curEntry.getBlockError() != null) {
            fireExit(context, resourceWrapper, count, args);
            return;
        }
        List<CircuitBreaker> circuitBreakers = ParamDegradeRuleManager.getCircuitBreakers(resourceWrapper.getName(), args);
        if (circuitBreakers == null || circuitBreakers.isEmpty()) {
            fireExit(context, resourceWrapper, count, args);
            return;
        }

        if (curEntry.getBlockError() == null) {
            // passed request
            for (CircuitBreaker circuitBreaker : circuitBreakers) {
                circuitBreaker.onRequestComplete(context);
            }
        }

        fireExit(context, resourceWrapper, count, args);
    }

    void performChecking(Context context, ResourceWrapper resourceWrapper, Object... args) throws BlockException {
        if (args == null || args.length == 0) {
            return;
        }

        if (!ParamDegradeRuleManager.hasRules(resourceWrapper.getName())) {
            return;
        }

        List<CircuitBreaker> circuitBreakers = ParamDegradeRuleManager.getCircuitBreakers(resourceWrapper.getName(), args);
        if (circuitBreakers == null || circuitBreakers.isEmpty()) {
            return;
        }
        for (CircuitBreaker cb : circuitBreakers) {
            if (!cb.tryPass(context)) {
                throw new DegradeException(cb.getRule().getLimitApp(), cb.getRule());
            }
        }
    }
}
