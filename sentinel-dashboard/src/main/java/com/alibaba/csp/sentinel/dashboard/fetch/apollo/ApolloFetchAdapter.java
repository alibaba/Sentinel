package com.alibaba.csp.sentinel.dashboard.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.fetch.Fetcher;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Apollo fetch rules adapter
 *
 * @author longqiang
 */
public abstract class ApolloFetchAdapter<T extends RuleEntity> implements Fetcher<T> {

    AppManagement appManagement;

    public ApolloFetchAdapter(AppManagement appManagement) {
        this.appManagement = appManagement;
    }

    @Override
    public List<T> fetch(String app, String ip, int port) {
        return Optional.ofNullable(appManagement.getDetailApp(app))
                       .flatMap(appInfo -> appInfo.getMachine(ip, port))
                       .map(machineInfo -> new Tuple2<>((ApolloMachineInfo) machineInfo, ApolloManagement.getOrCreateClient((ApolloMachineInfo) machineInfo)))
                       .filter(pair -> Objects.nonNull(pair.r2))
                       .map(pair -> getItem(pair.r2, pair.r1))
                       .map(item -> convert(app, ip, port, item.getValue()))
                       .orElse(null);
    }

    private OpenItemDTO getItem(ApolloOpenApiClient apolloClient, ApolloMachineInfo apolloMachineInfo) {
        return apolloClient.getItem(apolloMachineInfo.getAppId(), apolloMachineInfo.getEnv(), apolloMachineInfo.getClusterName(),
                                    apolloMachineInfo.getNamespace(), getKey(apolloMachineInfo));
    }

    /**
     * Get the key of the rule to be published in Apollo
     *
     * @param apolloMachineInfo
     * @return apollo key
     */
    protected abstract String getKey(ApolloMachineInfo apolloMachineInfo);

    /**
     * Convert to the corresponding rules
     *
     * @param app
     * @param ip
     * @param port
     * @param value
     * @return java.util.List<T>
     */
    protected abstract List<T> convert(String app, String ip, int port, String value);

}
