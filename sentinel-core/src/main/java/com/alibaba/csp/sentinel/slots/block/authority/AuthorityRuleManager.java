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
package com.alibaba.csp.sentinel.slots.block.authority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

/**
 * Manager for authority rules.
 *
 * @author youji.zj
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class AuthorityRuleManager {

    private static Map<String, List<AuthorityRule>> authorityRules
        = new ConcurrentHashMap<String, List<AuthorityRule>>();

    final static RulePropertyListener listener = new RulePropertyListener();

    private static SentinelProperty<List<AuthorityRule>> currentProperty
        = new DynamicSentinelProperty<List<AuthorityRule>>();

    static {
        currentProperty.addListener(listener);
    }

    public static void register2Property(SentinelProperty<List<AuthorityRule>> property) {
        synchronized (listener) {
            if (currentProperty != null) {
                currentProperty.removeListener(listener);
            }
            property.addListener(listener);
            currentProperty = property;
        }
    }

    /**
     * Load the authority rules to memory.
     *
     * @param rules list of authority rules
     */
    public static void loadRules(List<AuthorityRule> rules) {
        currentProperty.updateValue(rules);
    }

    public static void checkAuthority(ResourceWrapper resource, Context context, DefaultNode node, int count)
        throws BlockException {
        if (authorityRules == null) {
            return;
        }

        List<AuthorityRule> rules = authorityRules.get(resource.getName());
        if (rules == null) {
            return;
        }

        for (AuthorityRule rule : rules) {
            if (!rule.passCheck(context, node, count)) {
                throw new AuthorityException(context.getOrigin());
            }
        }
    }

    public static boolean hasConfig(String resource) {
        return authorityRules.containsKey(resource);
    }

    /**
     * Get a copy of the rules.
     *
     * @return a new copy of the rules.
     */
    public static List<AuthorityRule> getRules() {
        List<AuthorityRule> rules = new ArrayList<AuthorityRule>();
        if (authorityRules == null) {
            return rules;
        }
        for (Map.Entry<String, List<AuthorityRule>> entry : authorityRules.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    private static class RulePropertyListener implements PropertyListener<List<AuthorityRule>> {

        @Override
        public void configUpdate(List<AuthorityRule> conf) {
            Map<String, List<AuthorityRule>> rules = loadAuthorityConf(conf);

            authorityRules.clear();
            if (rules != null) {
                authorityRules.putAll(rules);
            }
            RecordLog.info("[AuthorityRuleManager] Authority rules received: " + authorityRules);
        }

        private Map<String, List<AuthorityRule>> loadAuthorityConf(List<AuthorityRule> list) {
            if (list == null) {
                return null;
            }
            Map<String, List<AuthorityRule>> newRuleMap = new ConcurrentHashMap<String, List<AuthorityRule>>();
            for (AuthorityRule rule : list) {
                if (StringUtil.isBlank(rule.getLimitApp())) {
                    rule.setLimitApp(FlowRule.LIMIT_APP_DEFAULT);
                }

                String identity = rule.getResource();
                List<AuthorityRule> ruleM = newRuleMap.get(identity);
                // putIfAbsent
                if (ruleM == null) {
                    ruleM = new ArrayList<AuthorityRule>();
                    ruleM.add(rule);
                    newRuleMap.put(identity, ruleM);
                } else {
                    // One resource should only have at most one authority rule, so just ignore redundant rules.
                    RecordLog.warn("[AuthorityRuleManager] Ignoring redundant rule: " + rule.toString());
                }
            }

            return newRuleMap;
        }

        @Override
        public void configLoad(List<AuthorityRule> value) {
            Map<String, List<AuthorityRule>> rules = loadAuthorityConf(value);

            authorityRules.clear();
            if (rules != null) {
                authorityRules.putAll(rules);
            }
            RecordLog.info("[AuthorityRuleManager] Load authority rules: " + authorityRules);
        }
    }

}
