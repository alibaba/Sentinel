package com.alibaba.csp.sentinel.dashboard.rule.adapter;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleStore;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author FengJianxin
 */
public class DefaultFlowRuleDynamicRuleStore extends DynamicRuleStore<FlowRuleEntity> {

    @Resource(name = "flowRuleDefaultProvider")
    private DynamicRuleProvider<List<FlowRuleEntity>> ruleProvider;
    @Resource(name = "flowRuleDefaultPublisher")
    private DynamicRulePublisher<List<FlowRuleEntity>> rulePublisher;

    @Override
    public List<FlowRuleEntity> getRules(final String appName) throws Exception {
        return ruleProvider.getRules(appName);
    }

    @Override
    public void publish(final String app, final List<FlowRuleEntity> rules) throws Exception {
        rulePublisher.publish(app, rules);
    }
}
