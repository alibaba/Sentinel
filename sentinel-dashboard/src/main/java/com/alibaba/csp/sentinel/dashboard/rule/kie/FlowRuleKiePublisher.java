package com.alibaba.csp.sentinel.dashboard.rule.kie;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("flowRuleKiePublisher")
public class FlowRuleKiePublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {
    @Override
    public void publish(String id, List<FlowRuleEntity> rules) throws Exception {

    }
}
