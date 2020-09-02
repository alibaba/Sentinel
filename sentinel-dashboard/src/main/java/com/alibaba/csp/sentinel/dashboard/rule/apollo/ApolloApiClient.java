package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.RuleConfigTypeEnum;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.AbstractpersistentRuleApiClient;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/31
 * @Version 1.0
 */
public class ApolloApiClient<T> extends AbstractpersistentRuleApiClient<T> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<String, List<FlowRuleEntity>> converter;

    private String getRuleConfigId(String appName, RuleConfigTypeEnum ruleFix) {
        appName = StringUtils.isBlank(appName) ? "Sentinel" : appName;
        return String.format("%s-%s", appName, ruleFix.getValue());
    }

    @Override
    public List<T> fetch(String app, RuleConfigTypeEnum configType) throws Exception {
        String appId = "appId";
        String flowDataId = this.getRuleConfigId(app, configType);
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, "DEV", "default", "application");
        String rulesJson = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(flowDataId))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");

        if (StringUtil.isEmpty(rulesJson)) {
            return (List<T>) new ArrayList();
        }
        return JSON.parseArray(rulesJson, configType.getClazz());
    }

    @Override
    public void publish(String app, RuleConfigTypeEnum configType, List<T> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        // Increase the configuration
        String appId = "appId";
        String flowDataId = this.getRuleConfigId(app, configType);
        String rulesJson = JSON.toJSONString(rules,true);
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(rulesJson);
        openItemDTO.setComment("Program auto-join");
        openItemDTO.setDataChangeCreatedBy("some-operator");
        apolloOpenApiClient.createOrUpdateItem(appId, "DEV", "default", "application", openItemDTO);

        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
        namespaceReleaseDTO.setReleasedBy("some-operator");
        namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
        apolloOpenApiClient.publishNamespace(appId, "DEV", "default", "application", namespaceReleaseDTO);
    }

}
