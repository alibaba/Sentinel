package com.alibaba.csp.sentinel.dashboard.transpot.fetch;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.datasource.management.DataSourceMachineInfo;
import com.alibaba.csp.sentinel.dashboard.datasource.management.DataSourceManagement;
import com.alibaba.csp.sentinel.dashboard.transpot.adapter.DataSourceAdapter;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Fetching adapter for data source
 *
 * @author longqiang
 */
public abstract class FetchAdapter<T extends RuleEntity, C, M extends DataSourceMachineInfo> implements Fetcher<T> {

    @Autowired
    AppManagement appManagement;

    @Autowired
    DataSourceAdapter<T> dataSourceAdapter;

    @Autowired
    DataSourceManagement<C> dataSourceManagement;

    @Override
    public List<T> fetch(String app, String ip, int port) {
        return Optional.ofNullable(appManagement.getDetailApp(app))
                .flatMap(appInfo -> appInfo.getMachine(ip, port))
                .map(machineInfo -> new Tuple2<>((M) machineInfo, dataSourceManagement.getOrCreateClient((M) machineInfo)))
                .filter(pair -> Objects.nonNull(pair.r2))
                .map(pair -> getConfig(pair.r2, pair.r1))
                .map(item -> dataSourceAdapter.convert(app, ip, port, item))
                .orElse(null);
    }

    protected String getKey(M machineInfo) {
        return dataSourceAdapter.getKey(machineInfo);
    }

    /**
     * get rules config
     *
     * @param client data source open api client
     * @param machineInfo machine info
     * @return T client
     */
    protected abstract String getConfig(C client, M machineInfo);

}
