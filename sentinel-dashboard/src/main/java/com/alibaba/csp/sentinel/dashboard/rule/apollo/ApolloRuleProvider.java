package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(ApolloConfig.class)
@Component("apolloRuleProvider")
public class ApolloRuleProvider {


}
