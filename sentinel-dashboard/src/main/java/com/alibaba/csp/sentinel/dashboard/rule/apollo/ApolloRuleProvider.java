package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.rule.AbstractRuleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(ApolloConfig.class)
@Component("apolloRuleProvider")
public class ApolloRuleProvider<T> extends AbstractRuleProvider<T> {

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        return null;
    }
}
