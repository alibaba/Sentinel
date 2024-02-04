package com.alibaba.csp.sentinel.dashboard.client;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;

import java.util.List;

/**
 * @author wuwen
 */
@FunctionalInterface
public interface FetchRuleOfMachineFunction<T extends RuleEntity> {

    List<T> fetchRuleOfMachine(String app, String ip, int port);
}
