package com.taobao.csp.sentinel.dashboard.rule.api;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.taobao.csp.sentinel.dashboard.discovery.AppManagement;
import com.taobao.csp.sentinel.dashboard.discovery.MachineInfo;
import com.taobao.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.taobao.csp.sentinel.dashboard.util.MachineUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Created by YL on 2018/12/27
 */
public interface BaseDynamicRuleApiProvider<T extends RuleEntity> extends DynamicRuleProvider<List<T>> {

    AppManagement getAppManagement();

    List<T> fetchRuleOfMachine(List<MachineInfo> list);

    @Override
    default List<T> getRules(String appName) throws Exception {
        if (StringUtil.isBlank(appName)) {
            return Collections.emptyList();
        }

        List<MachineInfo> list = getAppManagement()
                .getDetailApp(appName)
                .getMachines()
                .parallelStream()
                .filter(MachineUtil::isMachineHealth)
                .sorted((e1, e2) -> {
                    if (e1.getTimestamp().before(e2.getTimestamp())) {
                        return 1;
                    } else if (e1.getTimestamp().after(e2.getTimestamp())) {
                        return -1;
                    } else {
                        return 0;
                    }
                })
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        return fetchRuleOfMachine(list);
    }
}
