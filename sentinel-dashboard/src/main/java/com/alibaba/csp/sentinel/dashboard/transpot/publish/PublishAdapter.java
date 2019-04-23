package com.alibaba.csp.sentinel.dashboard.transpot.publish;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.datasource.management.DataSourceMachineInfo;
import com.alibaba.csp.sentinel.dashboard.datasource.management.DataSourceManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.transpot.adapter.DataSourceAdapter;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Publishing adapter for data source
 *
 * @author longqiang
 */
public abstract class PublishAdapter<T extends RuleEntity, C, M extends DataSourceMachineInfo> implements Publisher<T>{

    @Autowired
    AppManagement appManagement;

    @Autowired
    InMemoryRuleRepositoryAdapter<T> repository;

    @Autowired
    DataSourceAdapter<T> dataSourceAdapter;

    @Autowired
    DataSourceManagement<C> dataSourceManagement;

    @Override
    public boolean publish(String app, String ip, int port) {
        return Optional.ofNullable(appManagement.getDetailApp(app))
                        .flatMap(appInfo -> appInfo.getMachine(ip, port))
                        .map(machineInfo -> new Tuple2<>((M) machineInfo, dataSourceManagement.getOrCreateClient((M) machineInfo)))
                        .filter(pair -> Objects.nonNull(pair.r2))
                        .map(pair -> publish(pair.r2, pair.r1))
                        .orElse(false);
    }

    protected String getKey(M machineInfo) {
        return dataSourceAdapter.getKey(machineInfo);
    }

    protected String processRules(List<T> rules) {
        return dataSourceAdapter.processRules(rules);
    }

    protected List<T> findRules(MachineInfo machineInfo) {
        return repository.findAllByMachine(machineInfo);
    }

    /**
     * publish rules
     *
     * @param client data source open api client
     * @param machineInfo machine info
     * @return T client
     */
    protected abstract boolean publish(C client, M machineInfo);

}
