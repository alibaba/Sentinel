package com.alibaba.csp.sentinel.dashboard.transpot.publish.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.management.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.transpot.publish.PublishAdapter;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Publishing adapter based on Apollo
 *
 * @author longqiang
 */
public abstract class ApolloPublishAdapter<T extends RuleEntity> extends PublishAdapter<T, ApolloOpenApiClient, ApolloMachineInfo> {

    private static final Logger logger = LoggerFactory.getLogger(ApolloPublishAdapter.class);

    @Override
    protected boolean publish(ApolloOpenApiClient apolloClient, ApolloMachineInfo apolloMachineInfo) {
        try {
            createOrUpdateItem(apolloClient, apolloMachineInfo, findRules(apolloMachineInfo));
            publishNamespace(apolloClient, apolloMachineInfo);
            return true;
        } catch (Exception e) {
            logger.error("[Apollo] publish rules to apollo failed, rules key:{}, reason:{}", getKey(apolloMachineInfo), e);
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

}
