package com.alibaba.csp.sentinel.dashboard.transpot.publish.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.NacosMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.NacosManagement;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.transpot.adapter.DataSourceAdapter;
import com.alibaba.csp.sentinel.dashboard.transpot.publish.Publisher;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Publishing adapter based on Nacos
 *
 * @author longqiang
 */
public abstract class NacosPublishAdapter<T extends RuleEntity> implements Publisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(NacosPublishAdapter.class);

    @Autowired
    AppManagement appManagement;

    @Autowired
    InMemoryRuleRepositoryAdapter<T> repository;

    @Autowired
    DataSourceAdapter<T> dataSourceAdapter;

    @Override
    public boolean publish(String app, String ip, int port) {
        return Optional.ofNullable(appManagement.getDetailApp(app))
                        .flatMap(appInfo -> appInfo.getMachine(ip, port))
                        .map(machineInfo -> new Tuple2<>((NacosMachineInfo) machineInfo, NacosManagement.getOrCreateClient((NacosMachineInfo) machineInfo)))
                        .filter(pair -> Objects.nonNull(pair.r2))
                        .map(pair -> publish(pair.r2, pair.r1))
                        .orElse(false);
    }

    private boolean publish(ConfigService configService, NacosMachineInfo nacosMachineInfo) {
        boolean result = false;
        try {
            result = configService.publishConfig(dataSourceAdapter.getKey(nacosMachineInfo), nacosMachineInfo.getGroup(),
                                                 dataSourceAdapter.processRules(findRules(nacosMachineInfo)));
        } catch (NacosException e) {
            logger.error("[Nacos] publish rules to nacos failed, rules key:{}, reason:{}", dataSourceAdapter.getKey(nacosMachineInfo), e);
        }
        return result;
    }

    private List<T> findRules(MachineInfo machineInfo) {
        return repository.findAllByMachine(machineInfo);
    }

}
