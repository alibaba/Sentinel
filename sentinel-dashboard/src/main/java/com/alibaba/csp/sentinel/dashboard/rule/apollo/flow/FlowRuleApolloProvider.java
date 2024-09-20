/**
 * coder4j.cn
 * Copyright (C) 2013-2019 All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo.flow;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.ApolloCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author matthew
 * @date 2021-12-1 17:54
 * @description
 */
@Component("flowRuleApolloProvider")
public class FlowRuleApolloProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {

    @Autowired
    private ApolloCommonService apolloCommonService;
    @Value("${flow.key.suffix:flow}")
    private String flowDataIdSuffix;

    @Override
    public List<FlowRuleEntity> getRules(String appName) throws Exception {
        return apolloCommonService.getRules(appName, flowDataIdSuffix, FlowRuleEntity.class);
    }
}