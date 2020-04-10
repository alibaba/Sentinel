package com.alibaba.csp.sentinel.dashboard.repository;

/**
 * @author cdfive
 */
public interface RuleKeyBuilder<T> {
    String buildRuleKey(String app, String ip, Integer port);
}
