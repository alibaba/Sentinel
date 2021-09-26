
package com.alibaba.csp.sentinel.dashboard.repository.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;

/**
 *  类注释
 */

public interface DynamicRuleRepository<T> extends RuleRepository<T, Long>
{
    List<T> queryRules(String app, String ip, Integer port) throws Throwable;
    
    boolean publishRules(String app, String ip, Integer port);
}
