package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.client.FetchRuleOfMachineFunction;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
public abstract class AbstractRuleApiProvider<T extends RuleEntity> implements DynamicRuleProvider<List<T>> {

    @Autowired
    private AppManagement appManagement;

    @Override
    public List<T> getRules(String appName) {
        if (StringUtil.isBlank(appName)) {
            return new ArrayList<>();
        }
        List<MachineInfo> list = appManagement.getDetailApp(appName).getMachines()
                .stream()
                .filter(MachineInfo::isHealthy)
                .sorted((e1, e2) -> Long.compare(e2.getLastHeartbeat(), e1.getLastHeartbeat())).collect(Collectors.toList());
        if (list.isEmpty()) {
            return new ArrayList<>();
        } else {
            MachineInfo machine = list.get(0);
            return fetchRuleFun().fetchRuleOfMachine(machine.getApp(), machine.getIp(), machine.getPort());
        }
    }

    /**
     * Fetch rules from provided machine.
     * */
    abstract FetchRuleOfMachineFunction<T> fetchRuleFun();
}
