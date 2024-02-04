/**
 * coder4j.cn
 * Copyright (C) 2013-2019 All Rights Reserved.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo.authority;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.ApolloCommonService;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
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

@Component("authorityRuleApolloProvider")
public class AuthorityRuleApolloProvider implements DynamicRuleProvider<List<AuthorityRuleEntity>> {

    @Autowired
    private ApolloCommonService apolloCommonService;
    @Value("${authority.key.suffix:authority}")
    private String authorityDataIdSuffix;

    @Override
    public List<AuthorityRuleEntity> getRules(String appName) throws Exception {
        List<AuthorityRule> flow = apolloCommonService.getRules(appName, authorityDataIdSuffix, AuthorityRule.class);
        return flow.stream().map(AuthorityRuleEntity::new).collect(Collectors.toList());
    }
}