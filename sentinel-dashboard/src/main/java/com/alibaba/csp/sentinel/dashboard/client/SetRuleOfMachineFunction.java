package com.alibaba.csp.sentinel.dashboard.client;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;

import java.util.List;

@FunctionalInterface
public interface SetRuleOfMachineFunction<T extends RuleEntity> {

    boolean setRuleOfMachine(String app, String ip, int port, List<T> rules);
}
