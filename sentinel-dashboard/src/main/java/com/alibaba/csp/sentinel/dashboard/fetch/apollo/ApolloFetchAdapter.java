package com.alibaba.csp.sentinel.dashboard.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.fetch.Fetcher;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;

import java.util.List;
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
        Optional<MachineInfo> machineInfoOptional = appManagement.getDetailApp(app).getMachine(ip, port);
        return machineInfoOptional.map(machineInfo -> {
                    ApolloMachineInfo apolloMachineInfo = (ApolloMachineInfo) machineInfo;
                    ApolloOpenApiClient apolloClient = ApolloManagement.getOrCreateClient(apolloMachineInfo);
                    AssertUtil.notNull(apolloClient, String.format("There is no equivalent client for apollo portal url: %s", apolloMachineInfo.getPortalUrl()));
                    return getItem(apolloClient, apolloMachineInfo); })
                .map(item -> {
                    String value = item.getValue();
                    return convert(app, ip, port, value); })
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
