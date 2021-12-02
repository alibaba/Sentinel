/**
 * coder4j.cn
 * Copyright (C) 2013-2019 All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo.flow;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.ApolloCommonService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author matthew
 * @date 2021-12-1 17:54
 * @description
 */
@Component("flowRuleApolloPublisher")
public class FlowRuleApolloPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {

    @Autowired
    private ApolloCommonService apolloCommonService;
    @Value("${flow.key.suffix:flow}")
    private String flowDataIdSuffix;

    @Override
    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
        apolloCommonService.publishRules(app, flowDataIdSuffix, JSON.toJSONString(rules));
    }
}