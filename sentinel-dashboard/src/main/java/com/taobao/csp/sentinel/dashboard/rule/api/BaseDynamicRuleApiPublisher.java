package com.taobao.csp.sentinel.dashboard.rule.api;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.taobao.csp.sentinel.dashboard.discovery.AppManagement;
import com.taobao.csp.sentinel.dashboard.discovery.MachineInfo;
import com.taobao.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.taobao.csp.sentinel.dashboard.util.MachineUtil;

import java.util.List;
import java.util.Set;

/**
 * @author Created by YL on 2018/12/27
 */
public interface BaseDynamicRuleApiPublisher<T extends RuleEntity> extends DynamicRulePublisher<List<T>> {

    AppManagement getAppManagement();

    void setRuleOfMachine(MachineInfo machine, List<T> rules);

    void setRuleOfMachine(String app, MachineInfo machine, List<T> rules);

    @Override
    default void publish(String app, List<T> rules) throws Exception {
        if (StringUtil.isBlank(app)) {
            return;
        }
        if (rules == null) {
            return;
        }
        Set<MachineInfo> set = getAppManagement().getDetailApp(app).getMachines();

        for (MachineInfo machine : set) {
            if (!MachineUtil.isMachineHealth(machine)) {
                continue;
            }
            setRuleOfMachine(machine, rules);
            // setRuleOfMachine(app, machine, rules);
        }
    }
}
