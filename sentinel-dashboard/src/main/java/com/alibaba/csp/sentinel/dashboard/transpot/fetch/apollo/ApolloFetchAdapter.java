package com.alibaba.csp.sentinel.dashboard.transpot.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.transpot.adapter.DataSourceAdapter;
import com.alibaba.csp.sentinel.dashboard.transpot.fetch.Fetcher;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Apollo fetch rules adapter
 *
 * @author longqiang
 */
public abstract class ApolloFetchAdapter<T extends RuleEntity> implements Fetcher<T> {

    @Autowired
    AppManagement appManagement;

    @Autowired
    DataSourceAdapter<T> dataSourceAdapter;

    @Override
    public List<T> fetch(String app, String ip, int port) {
        return Optional.ofNullable(appManagement.getDetailApp(app))
                       .flatMap(appInfo -> appInfo.getMachine(ip, port))
                       .map(machineInfo -> new Tuple2<>((ApolloMachineInfo) machineInfo, ApolloManagement.getOrCreateClient((ApolloMachineInfo) machineInfo)))
                       .filter(pair -> Objects.nonNull(pair.r2))
                       .map(pair -> getItem(pair.r2, pair.r1))
                       .map(item -> dataSourceAdapter.convert(app, ip, port, item.getValue()))
                       .orElse(null);
    }

    private OpenItemDTO getItem(ApolloOpenApiClient apolloClient, ApolloMachineInfo apolloMachineInfo) {
        return apolloClient.getItem(apolloMachineInfo.getAppId(), apolloMachineInfo.getEnv(), apolloMachineInfo.getClusterName(),
                                    apolloMachineInfo.getNamespace(), dataSourceAdapter.getKey(apolloMachineInfo));
    }

}
