package com.alibaba.csp.sentinel.dashboard.rule;

/**
 * @author cdfive
 */
public interface RuleKeyBuilder<T> {
    String buildRuleKey(String app, String ip, Integer port);
}
