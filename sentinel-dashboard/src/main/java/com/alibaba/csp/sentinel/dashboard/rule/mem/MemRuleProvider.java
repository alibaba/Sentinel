package com.alibaba.csp.sentinel.dashboard.rule.mem;

import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MemRuleProvider <T extends List> implements DynamicRuleProvider<T> {

    private Function<MachineInfo,T> ruleFetch;
    @Autowired
    private AppManagement appManagement;

    public MemRuleProvider(Function<MachineInfo,T> fetch) {
        this.ruleFetch=fetch;
    }

    @Override
    public T getRules(String appName) throws Exception {
        if (StringUtil.isBlank(appName)) {
            return (T) new ArrayList<T>();
        }
        List<MachineInfo> list = appManagement.getDetailApp(appName).getMachines()
                .stream()
                .filter(MachineInfo::isHealthy)
                .sorted((e1, e2) -> Long.compare(e2.getLastHeartbeat(), e1.getLastHeartbeat())).collect(Collectors.toList());
        if (list.isEmpty()) {
            return (T) new ArrayList<T>();
        } else {
            MachineInfo machine = list.get(0);
            return ruleFetch.apply(machine);
        }
    }
}
