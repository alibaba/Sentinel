package com.alibaba.csp.sentinel.dashboard.transpot.publish.inmemory;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.transpot.publish.Publisher;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;

import java.util.List;

/**
 * Publishing adapter based on memory
 *
 * @author longqiang
 */
public abstract class InMemoryPublishAdapter<T extends RuleEntity> implements Publisher<T> {

    protected SentinelApiClient sentinelApiClient;

    private InMemoryRuleRepositoryAdapter<T> repository;

    protected InMemoryPublishAdapter(InMemoryRuleRepositoryAdapter<T> repository, SentinelApiClient sentinelApiClient) {
        this.repository = repository;
        this.sentinelApiClient = sentinelApiClient;
    }

    @Override
    public boolean publish(String app, String ip, int port) {
        List<T> rules = findRules(MachineInfo.of(app, ip, port));
        return publish(app, ip, port, rules);
    }

    /**
     * find rules for publish
     *
     * @param machineInfo project machineInfo
     * @return java.util.List<T>
     */
    private List<T> findRules(MachineInfo machineInfo) {
        return repository.findAllByMachine(machineInfo);
    }

    /**
     * publish rules action
     *
     * @param app project name
     * @param ip project deployment IP
     * @param port project deployment port
     * @param rules rules to be pushed to third-party data sources
     * @return boolean
     */
    protected abstract boolean publish(String app, String ip, int port, List<T> rules);

}
