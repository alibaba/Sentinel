/**
 * coder4j.cn
 * Copyright (C) 2013-2019 All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo.param;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AbstractRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.ApolloCommonService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author matthew
 * @date 2021-12-1 17:54
 * @description
 */
@Component("paramFlowRuleApolloPublisher")
public class ParamFlowRuleApolloPublisher implements DynamicRulePublisher<List<ParamFlowRuleEntity>> {

    @Autowired
    private ApolloCommonService apolloCommonService;
    @Value("${param.key.suffix:param_flow}")
    private String paramDataIdSuffix;

    @Override
    public void publish(String app, List<ParamFlowRuleEntity> rules) throws Exception {
        apolloCommonService.publishRules(app, paramDataIdSuffix, JSON.toJSONString(rules.stream().map(AbstractRuleEntity::getRule).collect(Collectors.toList())));
    }
}