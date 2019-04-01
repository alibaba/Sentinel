package com.alibaba.csp.sentinel.dashboard.fetch.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloClientManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.fetch.Fetcher;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Apollo fetch rules adapter
 *
 * @author longqiang
 */
public abstract class ApolloFetchAdapter<T extends RuleEntity> implements Fetcher<T> {

    private static final Logger logger = LoggerFactory.getLogger(ApolloFetchAdapter.class);

    AppManagement appManagement;

    public ApolloFetchAdapter(AppManagement appManagement) {
        this.appManagement = appManagement;
    }

    @Override
    public List<T> fetch(String app, String ip, int port) {
        Optional<MachineInfo> machineInfoOptional = appManagement.getDetailApp(app).getMachine(ip, port);
        ApolloMachineInfo apolloMachineInfo = (ApolloMachineInfo) machineInfoOptional.get();
        AssertUtil.notNull(apolloMachineInfo, String.format("There is no equivalent machineInfo for app: %s, ip: %s, port: %s", app, ip, port));
        ApolloOpenApiClient apolloClient = ApolloClientManagement.getOrCreateClient(apolloMachineInfo);
        AssertUtil.notNull(apolloClient, String.format("There is no equivalent client for apollo portal url: %s", apolloMachineInfo.getPortalUrl()));
        OpenItemDTO item = getItem(apolloClient, apolloMachineInfo);
        if (Objects.isNull(item)) {
            logger.warn("There is no corresponding configuration in Apollo,app:{},ip:{},port:{},apollo portal url:{} ,rules key:{}"
                        , app, ip, port, apolloMachineInfo.getPortalUrl(), getKey(apolloMachineInfo));
            return Lists.newArrayList();
        }
        String value = item.getValue();
        return convert(value);
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
     * @param value
     * @return java.util.List<T>
     */
    protected abstract List<T> convert(String value);

}
