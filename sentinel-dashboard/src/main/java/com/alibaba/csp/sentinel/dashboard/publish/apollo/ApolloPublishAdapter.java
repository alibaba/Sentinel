package com.alibaba.csp.sentinel.dashboard.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.publish.Publisher;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Publishing adapter based on Apollo
 *
 * @author longqiang
 */
public abstract class ApolloPublishAdapter<T extends RuleEntity> implements Publisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(ApolloPublishAdapter.class);

    AppManagement appManagement;

    InMemoryRuleRepositoryAdapter<T> repository;

    protected ApolloPublishAdapter(AppManagement appManagement, InMemoryRuleRepositoryAdapter<T> repository) {
        this.appManagement = appManagement;
        this.repository = repository;
    }

    @Override
    public boolean publish(String app, String ip, int port) {
        return Optional.ofNullable(appManagement.getDetailApp(app))
                        .flatMap(appInfo -> appInfo.getMachine(ip, port))
                        .map(machineInfo -> new Tuple2<>((ApolloMachineInfo) machineInfo, ApolloManagement.getOrCreateClient((ApolloMachineInfo) machineInfo)))
                        .filter(pair -> Objects.nonNull(pair.r2))
                        .map(pair -> publish(pair.r2, pair.r1))
                        .orElse(false);
    }

    private boolean publish(ApolloOpenApiClient apolloClient, ApolloMachineInfo apolloMachineInfo) {
        try {
            createOrUpdateItem(apolloClient, apolloMachineInfo, findRules(apolloMachineInfo));
            publishNamespace(apolloClient, apolloMachineInfo);
            return true;
        } catch (Exception e) {
            logger.error("publish rules to apollo failed, rules key:{}, reason:{}", getKey(apolloMachineInfo), e);
            return false;
        }
    }

    private void createOrUpdateItem(ApolloOpenApiClient apolloClient, ApolloMachineInfo apolloMachineInfo, List<T> rules) {
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(getKey(apolloMachineInfo));
        openItemDTO.setValue(processRules(rules));
        openItemDTO.setDataChangeCreatedBy(apolloMachineInfo.getOperator());
        apolloClient.createOrUpdateItem(apolloMachineInfo.getAppId(), apolloMachineInfo.getEnv(), apolloMachineInfo.getClusterName(),
                                        apolloMachineInfo.getNamespace(), openItemDTO);
    }

    private void publishNamespace(ApolloOpenApiClient apolloClient, ApolloMachineInfo apolloMachineInfo) {
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleaseTitle(String.format("sentinel-%s-release", DateFormat.getInstance().format(new Date())));
        namespaceReleaseDTO.setReleasedBy(apolloMachineInfo.getOperator());
        apolloClient.publishNamespace(apolloMachineInfo.getAppId(), apolloMachineInfo.getEnv(), apolloMachineInfo.getClusterName(),
                                      apolloMachineInfo.getNamespace(), namespaceReleaseDTO);
    }

    private List<T> findRules(MachineInfo machineInfo) {
        return repository.findAllByMachine(machineInfo);
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
