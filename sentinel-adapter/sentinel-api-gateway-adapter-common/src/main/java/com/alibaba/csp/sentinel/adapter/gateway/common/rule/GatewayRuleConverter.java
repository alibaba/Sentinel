/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
final class GatewayRuleConverter {

    static FlowRule toFlowRule(/*@Valid*/ GatewayFlowRule rule) {
        return new FlowRule(rule.getResource())
            .setControlBehavior(rule.getControlBehavior())
            .setCount(rule.getCount())
            .setGrade(rule.getGrade())
            .setMaxQueueingTimeMs(rule.getMaxQueueingTimeoutMs());
    }

    static ParamFlowItem generateNonMatchPassParamItem() {
        return new ParamFlowItem().setClassType(String.class.getName())
            .setCount(1000_0000)
            .setObject(SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM);
    }

    static ParamFlowItem generateNonMatchBlockParamItem() {
        return new ParamFlowItem().setClassType(String.class.getName())
            .setCount(0)
            .setObject(SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM);
    }

    static ParamFlowRule applyNonParamToParamRule(/*@Valid*/ GatewayFlowRule gatewayRule, int idx) {
        return new ParamFlowRule(gatewayRule.getResource())
            .setCount(gatewayRule.getCount())
            .setGrade(gatewayRule.getGrade())
            .setDurationInSec(gatewayRule.getIntervalSec())
            .setBurstCount(gatewayRule.getBurst())
            .setControlBehavior(gatewayRule.getControlBehavior())
            .setMaxQueueingTimeMs(gatewayRule.getMaxQueueingTimeoutMs())
            .setParamIdx(idx);
    }

    /**
     * Convert a gateway rule to parameter flow rule, then apply the generated
     * parameter index to {@link GatewayParamFlowItem} of the rule.
     *
     * @param gatewayRule a valid gateway rule that should contain valid parameter items
     * @param idx generated parameter index (callers should guarantee it's unique and incremental)
     * @return converted parameter flow rule
     */
    static ParamFlowRule applyToParamRule(/*@Valid*/ GatewayFlowRule gatewayRule, int idx) {
        ParamFlowRule paramRule = new ParamFlowRule(gatewayRule.getResource())
            .setCount(gatewayRule.getCount())
            .setGrade(gatewayRule.getGrade())
            .setDurationInSec(gatewayRule.getIntervalSec())
            .setBurstCount(gatewayRule.getBurst())
            .setControlBehavior(gatewayRule.getControlBehavior())
            .setMaxQueueingTimeMs(gatewayRule.getMaxQueueingTimeoutMs())
            .setParamIdx(idx);
        GatewayParamFlowItem gatewayItem = gatewayRule.getParamItem();
        // Apply the current idx to gateway rule item.
        gatewayItem.setIndex(idx);
        // Apply for pattern-based parameters.
        String valuePattern = gatewayItem.getPattern();
        if (valuePattern != null) {
            paramRule.getParamFlowItemList().add(generateNonMatchPassParamItem());
        }
        return paramRule;
    }

    private GatewayRuleConverter() {}
}
