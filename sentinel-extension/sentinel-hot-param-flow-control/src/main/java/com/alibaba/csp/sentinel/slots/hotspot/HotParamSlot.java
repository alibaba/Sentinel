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
package com.alibaba.csp.sentinel.slots.hotspot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * A processor slot that is responsible for flow control by frequent ("hot-spot") parameters.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotParamSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    private static final Map<ResourceWrapper, HotParameterMetric> metricsMap
        = new ConcurrentHashMap<ResourceWrapper, HotParameterMetric>();

    /**
     * Lock for a specific resource.
     */
    private final Object LOCK = new Object();

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, Object... args)
        throws Throwable {

        if (!HotParamRuleManager.hasRules(resourceWrapper.getName())) {
            fireEntry(context, resourceWrapper, node, count, args);
            return;
        }

        checkFlow(resourceWrapper, count, args);
        fireEntry(context, resourceWrapper, node, count, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }

    void checkFlow(ResourceWrapper resourceWrapper, int count, Object... args)
        throws BlockException {
        if (HotParamRuleManager.hasRules(resourceWrapper.getName())) {
            List<HotParamRule> rules = HotParamRuleManager.getRulesOfResource(resourceWrapper.getName());
            if (rules == null) {
                return;
            }

            for (HotParamRule rule : rules) {
                // Initialize the hot parameter metrics.
                initHotParamMetricsFor(resourceWrapper, rule);

                if (!HotParamChecker.passCheck(resourceWrapper, rule, count, args)) {

                    // Here we add the block count.
                    addBlockCount(resourceWrapper, count, args);

                    String message = "";
                    if (args.length > rule.getParamIdx()) {
                        Object value = args[rule.getParamIdx()];
                        message = String.valueOf(value);
                    }
                    throw new HotParamException(resourceWrapper.getName(), message);
                }
            }
        }
    }

    private void addBlockCount(ResourceWrapper resourceWrapper, int count, Object... args) {
        HotParameterMetric hotParameterMetric = HotParamSlot.getHotParamMetric(resourceWrapper);

        if (hotParameterMetric != null) {
            hotParameterMetric.addBlock(count, args);
        }
    }

    private void initHotParamMetricsFor(ResourceWrapper resourceWrapper, HotParamRule rule) {
        HotParameterMetric metric;
        // Assume that the resource is valid.
        if ((metric = metricsMap.get(resourceWrapper)) == null) {
            synchronized (LOCK) {
                if ((metric = metricsMap.get(resourceWrapper)) == null) {
                    metric = new HotParameterMetric();
                    metricsMap.put(resourceWrapper, metric);
                    RecordLog.info("[HotParamSlot] Creating hot parameter metric for: " + resourceWrapper.getName());
                }
            }
        }
        metric.initializeForIndex(rule.getParamIdx());
    }

    public static HotParameterMetric getHotParamMetric(ResourceWrapper resourceWrapper) {
        if (resourceWrapper == null || resourceWrapper.getName() == null) {
            return null;
        }
        return metricsMap.get(resourceWrapper);
    }
}
