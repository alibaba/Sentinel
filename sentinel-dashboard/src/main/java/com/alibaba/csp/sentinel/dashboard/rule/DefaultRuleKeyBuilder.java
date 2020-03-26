package com.alibaba.csp.sentinel.dashboard.rule;

import com.google.common.base.Joiner;

/**
 * @author cdfive
 */
public class DefaultRuleKeyBuilder<T> implements RuleKeyBuilder<T> {

    @Override
    public String buildRuleKey(String app, String ip, Integer port) {
        return Joiner.on("-").join(app, ip, port);
    }
}
