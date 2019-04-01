package com.alibaba.csp.sentinel.dashboard.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloClientManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.publish.Publisher;
import com.alibaba.csp.sentinel.dashboard.repository.rule.thirdparty.ApolloRepository;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Publishing adapter based on Apollo
 *
 * @author longqiang
 */
public abstract class ApolloPublishAdapter<T extends RuleEntity> implements Publisher<T> {

    AppManagement appManagement;

    ApolloRepository<T> repository;

    protected ApolloPublishAdapter(AppManagement appManagement, ApolloRepository<T> repository) {
        this.appManagement = appManagement;
        this.repository = repository;
    }

    @Override
    public boolean publish(String app, String ip, int port) {
        Optional<MachineInfo> machineInfoOptional = appManagement.getDetailApp(app).getMachine(ip, port);
        ApolloMachineInfo apolloMachineInfo = (ApolloMachineInfo) machineInfoOptional.get();
        AssertUtil.notNull(apolloMachineInfo, String.format("There is no equivalent machineInfo for app: %s, ip: %s, port: %s", app, ip, port));
        List<T> rules = findRules(apolloMachineInfo);
        ApolloOpenApiClient apolloClient = ApolloClientManagement.getOrCreateClient(apolloMachineInfo);
        AssertUtil.notNull(apolloClient, String.format("There is no equivalent client for apollo portal url: %s", apolloMachineInfo.getPortalUrl()));
        String appId = apolloMachineInfo.getAppId();
        String env = apolloMachineInfo.getEnv();
        String clusterName = apolloMachineInfo.getClusterName();
        String namespace = apolloMachineInfo.getNamespace();
        String operator = apolloMachineInfo.getOperator();
        createOrUpdateItem(apolloClient, apolloMachineInfo, operator, rules, appId, env, clusterName, namespace);
        publishNamespace(apolloClient, operator, appId, env, clusterName, namespace);
        return true;
    }

    private void createOrUpdateItem(ApolloOpenApiClient apolloClient, ApolloMachineInfo apolloMachineInfo, String operator, List<T> rules, String appId, String env, String clusterName, String namespaceName) {
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(getKey(apolloMachineInfo));
        openItemDTO.setValue(processRules(rules));
        openItemDTO.setDataChangeCreatedBy(operator);
        apolloClient.createOrUpdateItem(appId, env, clusterName, namespaceName, openItemDTO);
    }

    private void publishNamespace(ApolloOpenApiClient apolloClient, String operator, String appId, String env, String clusterName, String namespaceName) {
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleaseTitle(String.format("sentinel-%s-release", DateFormat.getInstance().format(new Date())));
        namespaceReleaseDTO.setReleasedBy(operator);
        apolloClient.publishNamespace(appId, env, clusterName, namespaceName, namespaceReleaseDTO);
    }

    private List<T> findRules(ApolloMachineInfo apolloMachineInfo) {
        return repository.findByPortal(apolloMachineInfo);
    }

    /**
     * Get Apollo rule key
     *
     * @param apolloMachineInfo
     * @return java.lang.String
     */
    protected abstract String getKey(ApolloMachineInfo apolloMachineInfo);

    /**
     * Process rules let them store to Apollo
     *
     * @param rules
     * @return java.lang.String
     */
    protected abstract String processRules(List<T> rules);
}
