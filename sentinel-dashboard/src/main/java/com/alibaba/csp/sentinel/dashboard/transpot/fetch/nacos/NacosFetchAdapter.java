package com.alibaba.csp.sentinel.dashboard.transpot.fetch.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.NacosMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.NacosManagement;
import com.alibaba.csp.sentinel.dashboard.transpot.adapter.DataSourceAdapter;
import com.alibaba.csp.sentinel.dashboard.transpot.fetch.Fetcher;
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
 * Nacos fetch rules adapter
 *
 * @author longqiang
 */
public abstract class NacosFetchAdapter<T extends RuleEntity> implements Fetcher<T> {

    private static final Logger logger = LoggerFactory.getLogger(NacosFetchAdapter.class);

    @Autowired
    AppManagement appManagement;

    @Autowired
    DataSourceAdapter<T> dataSourceAdapter;

    @Override
    public List<T> fetch(String app, String ip, int port) {
        return Optional.ofNullable(appManagement.getDetailApp(app))
                        .flatMap(appInfo -> appInfo.getMachine(ip, port))
                        .map(machineInfo -> new Tuple2<>((NacosMachineInfo) machineInfo, NacosManagement.getOrCreateClient((NacosMachineInfo) machineInfo)))
                        .filter(pair -> Objects.nonNull(pair.r2))
                        .map(pair -> getConfig(pair.r2, pair.r1))
                        .map(item -> dataSourceAdapter.convert(app, ip, port, item))
                        .orElse(null);
    }

    private String getConfig(ConfigService configService, NacosMachineInfo nacosMachineInfo) {
        try {
            return configService.getConfig(dataSourceAdapter.getKey(nacosMachineInfo), nacosMachineInfo.getGroup(), nacosMachineInfo.getTimeoutMs());
        } catch (NacosException e) {
            logger.error("[Nacos] fetch rules from nacos failed, rules key:{}, reason:{}", dataSourceAdapter.getKey(nacosMachineInfo), e);
        }
        return null;
    }

}
