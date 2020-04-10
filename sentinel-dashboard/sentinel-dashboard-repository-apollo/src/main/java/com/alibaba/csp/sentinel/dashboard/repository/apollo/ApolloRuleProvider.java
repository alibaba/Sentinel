package com.alibaba.csp.sentinel.dashboard.repository.apollo;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRuleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
//@ConditionalOnBean(ApolloConfig.class)
//@Component("apolloRuleProvider")
public class ApolloRuleProvider<T extends RuleEntity> extends AbstractRuleProvider<T> {

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        return null;
    }
}
