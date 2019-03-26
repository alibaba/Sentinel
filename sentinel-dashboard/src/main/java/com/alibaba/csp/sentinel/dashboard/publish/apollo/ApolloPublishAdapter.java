package com.alibaba.csp.sentinel.dashboard.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloClientManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.publish.Publisher;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
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

    InMemoryRuleRepositoryAdapter<T> repository;

    protected ApolloPublishAdapter(AppManagement appManagement, InMemoryRuleRepositoryAdapter<T> repository) {
        this.appManagement = appManagement;
        this.repository = repository;
    }

    @Override
    public boolean publish(String app, String ip, int port) {
        Optional<MachineInfo> machineInfoOptional = appManagement.getDetailApp(app).getMachine(ip, port);
        ApolloMachineInfo apolloMachineInfo = (ApolloMachineInfo) machineInfoOptional.get();
        List<T> rules = findRules(apolloMachineInfo);
        ApolloOpenApiClient apolloClient = ApolloClientManagement.getClient(apolloMachineInfo.getPortalUrl());
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

    private List<T> findRules(MachineInfo machineInfo) {
        return repository.findAllByMachine(machineInfo);
    }

    protected abstract String getKey(ApolloMachineInfo apolloMachineInfo);

    protected abstract String processRules(List<T> rules);
}
