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
package com.alibaba.csp.sentinel.webflow.rule;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.webflow.SentinelWebFlowConstants;

/**
 * @author guanyu
 * @since 1.10.0
 */
public final class WebFlowRuleConverter {

    public static final String WEB_PARAM_RULE_KEY_PREFIX = "$WEB_PF";

    static FlowRule toFlowRule(/*@Valid*/ WebFlowRule rule) {
        return new FlowRule(rule.getResource())
                .setControlBehavior(rule.getControlBehavior())
                .setCount(rule.getCount())
                .setGrade(rule.getGrade())
                .setMaxQueueingTimeMs(rule.getMaxQueueingTimeoutMs());
    }

    static ParamFlowItem generateNonMatchPassParamItem() {
        return new ParamFlowItem().setClassType(String.class.getName())
                .setCount(10000000)
                .setObject(SentinelWebFlowConstants.WEB_FLOW_NOT_MATCH_PARAM);
    }

    static ParamFlowItem generateNonMatchBlockParamItem() {
        return new ParamFlowItem().setClassType(String.class.getName())
                .setCount(0)
                .setObject(SentinelWebFlowConstants.WEB_FLOW_NOT_MATCH_PARAM);
    }

    static ParamFlowRule applyNonParamToParamRule(/*@Valid*/ WebFlowRule webFlowRule) {
        return new ParamFlowRule(webFlowRule.getResource(), webFlowRule.getId())
                .setCount(webFlowRule.getCount())
                .setGrade(webFlowRule.getGrade())
                .setDurationInSec(webFlowRule.getIntervalMs() / 1000)
                .setBurstCount(webFlowRule.getBurst())
                .setControlBehavior(webFlowRule.getControlBehavior())
                .setMaxQueueingTimeMs(webFlowRule.getMaxQueueingTimeoutMs())
                .setParamKey(SentinelWebFlowConstants.WEB_FLOW_NON_PARAM_DEFAULT_KEY);
    }

    /**
     * Convert a web flow rule to parameter flow rule, then apply the generated
     * parameter index to {@link WebParamItem} of the rule.
     *
     * @param webFlowRule a valid gateway rule that should contain valid parameter items
     * @return converted parameter flow rule
     */
    static ParamFlowRule applyToParamRule(/*@Valid*/ WebFlowRule webFlowRule) {
        ParamFlowRule paramRule = new ParamFlowRule(webFlowRule.getResource(), webFlowRule.getId())
                .setCount(webFlowRule.getCount())
                .setGrade(webFlowRule.getGrade())
                .setDurationInSec(webFlowRule.getIntervalMs() / 1000)
                .setBurstCount(webFlowRule.getBurst())
                .setControlBehavior(webFlowRule.getControlBehavior())
                .setMaxQueueingTimeMs(webFlowRule.getMaxQueueingTimeoutMs());
        WebParamItem webParamItem = webFlowRule.getParamItem();
        // Apply the parse strategy to param key.
        String convertedParamKey = generateParamKeyForWebRule(webFlowRule);
        paramRule.setParamKey(convertedParamKey);
        // Cache the param key inside the web param item.
        webParamItem.setConvertedParamKey(convertedParamKey);

        // Apply for pattern-based parameters.
        String valuePattern = webParamItem.getPattern();
        if (valuePattern != null) {
            paramRule.getParamFlowItemList().add(generateNonMatchPassParamItem());
        }
        //cluster mode
        if (webFlowRule.isClusterMode()) {
            paramRule.setClusterConfig(webFlowRule.getClusterConfig());
        }
        return paramRule;
    }

    public static String generateParamKeyForWebRule(WebFlowRule rule) {
        if (rule == null || rule.getId() == null) {
            return null;
        }
        WebParamItem item = rule.getParamItem();
        if (item == null) {
            return WEB_PARAM_RULE_KEY_PREFIX + '|' + rule.getId();
        }
        String paramKey = SentinelWebFlowConstants.PARSE_STRATEGY_KEY_MAP.get(item.getParseStrategy());
        if (paramKey == null) {
            paramKey = SentinelWebFlowConstants.WEB_PARAM_UNKNOWN_PARSE_STRATEGY_KEY;
        }
        return WEB_PARAM_RULE_KEY_PREFIX + '|' + paramKey + '|' + rule.getId();
    }

    /**
     * Get cached converted param key inside the original {@link WebParamItem}.
     *
     * @param item valid param item of a {@link WebFlowRule}
     * @return cached key if present, otherwise null
     */
    public static String getCachedConvertedParamKey(WebParamItem item) {
        if (item == null) {
            return null;
        }
        // 只有经历过 applyToParamRule 的 WebFlowRule 才会持有对应的 key cache.
        return item.getConvertedParamKey();
    }

    private WebFlowRuleConverter() {}
}
