package com.alibaba.csp.sentinel.dashboard.rule.kie;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.KieConfigClient;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigResponse;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.kie.util.KieConfigUtil;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component("flowRuleKieProvider")
public class FlowRuleKieProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {

    @Autowired
    KieServerManagement kieServerManagement;

    @Autowired
    KieConfigClient kieConfigClient;

    @Override
    public List<FlowRuleEntity> getRules(String id){
        Optional<KieServerInfo> kieServerInfo = kieServerManagement.queryKieInfo(id);
        if(!kieServerInfo.isPresent()){
            return Collections.emptyList();
        }

        Optional<KieConfigResponse> response = kieConfigClient.getConfig(kieServerInfo.get().getKieConfigUrl());

        if(!response.isPresent()){
            return Collections.emptyList();
        }

        List<FlowRule> flowRules = KieConfigUtil.parseKieConfig(FlowRule.class, kieServerInfo.get(), response.get());

        if(flowRules.isEmpty()){
            return Collections.emptyList();
        }

        return transToEntity(flowRules);
    }

    private List<FlowRuleEntity> transToEntity(List<FlowRule> flowRules){
        List<FlowRuleEntity> entities = new ArrayList<>();
        for(FlowRule rule: flowRules){
            FlowRuleEntity flowRuleEntity = FlowRuleEntity.fromFlowRule(rule);
            entities.add(flowRuleEntity);
        }
        return entities;
    }
}
