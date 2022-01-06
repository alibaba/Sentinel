package com.alibaba.csp.sentinel.dashboard.rule.mem;

import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.BiConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

public class MemRulePublisher <T extends List> implements DynamicRulePublisher<T> {
    private BiConsumer<MachineInfo,T> publisher;
    @Autowired
    private AppManagement appManagement;

    public MemRulePublisher(BiConsumer<MachineInfo, T> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(String app, T rules) throws Exception {
        if (StringUtil.isBlank(app)||null==rules) {
            return;
        }
        Set<MachineInfo> set = appManagement.getDetailApp(app).getMachines();

        for (MachineInfo machine : set) {
            if (!machine.isHealthy()) {
                continue;
            }
            // TODO: parse the results
            publisher.accept(machine,rules);
        }
    }
}
