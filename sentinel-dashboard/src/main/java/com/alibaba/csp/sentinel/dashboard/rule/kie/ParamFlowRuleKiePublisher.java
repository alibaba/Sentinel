package com.alibaba.csp.sentinel.dashboard.rule.kie;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.KieConfigClient;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigLabel;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigResponse;
import com.alibaba.csp.sentinel.dashboard.config.KieConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component("paramFlowRuleKiePublisher")
public class ParamFlowRuleKiePublisher implements RuleKiePublisher<List<ParamFlowRuleEntity>> {

    @Autowired
    KieServerManagement kieServerManagement;

    @Autowired
    KieConfigClient kieConfigClient;

    @Autowired
    KieConfig kieConfig;

    @Override
    public void update(String serverId, List<ParamFlowRuleEntity> entities) {
        if(StringUtils.isEmpty(serverId) || Objects.isNull(entities)){
            throw new NullPointerException();
        }

        Optional<KieServerInfo> kieServerInfo = kieServerManagement.queryKieInfo(serverId);
        if(!kieServerInfo.isPresent()){
            String errorMessage = String.format("Cannot find kie server by id: %s.", serverId);
            RecordLog.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        String urlPrefix = kieConfig.getKieBaseUrl(kieServerInfo.get().getLabel().getProject());

        entities.forEach(ruleEntity -> {
            String url = urlPrefix + "/" + ruleEntity.getRuleId();
            ParamFlowRule paramFlowRule = ruleEntity.toRule();
            Optional<KieConfigResponse> response = kieConfigClient.updateConfig(url, paramFlowRule);
            if(!response.isPresent()){
                RecordLog.error("Update rules failed");
                throw new RuntimeException("Update rules failed");
            }
        });
    }

    @Override
    public void add(String serverId, List<ParamFlowRuleEntity> entities) {
        if(StringUtils.isEmpty(serverId) || Objects.isNull(entities)){
            throw new NullPointerException();
        }

        Optional<KieServerInfo> kieServerInfo = kieServerManagement.queryKieInfo(serverId);
        if(!kieServerInfo.isPresent()){
            String errorMessage = String.format("Cannot find kie server by id: %s.", serverId);
            RecordLog.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        KieServerLabel kieServerLabel = kieServerInfo.get().getLabel();
        KieConfigLabel kieConfigLabel = KieConfigLabel.builder()
                .app(kieServerLabel.getApp())
                .environment(kieServerLabel.getEnvironment())
                .service(kieServerLabel.getService())
                .version(kieServerLabel.getServerVersion())
                .build();

        String url = kieConfig.getKieBaseUrl(kieServerInfo.get().getLabel().getProject());
        entities.forEach(ruleEntity -> {
            kieConfigLabel.setResource(ruleEntity.getResource());
            ParamFlowRule paramFlowRule = ruleEntity.toRule();
            Optional<KieConfigResponse> response = kieConfigClient.addConfig(url, "ParamFlowRule", paramFlowRule, kieConfigLabel);
            if(!response.isPresent()){
                RecordLog.error("Add rules failed");
                throw new RuntimeException("Add rules failed");
            }
        });
    }

    @Override
    public void delete(String serverId, String ruleId) {
        if(StringUtils.isEmpty(serverId) || StringUtils.isEmpty(ruleId)){
            throw new NullPointerException();
        }

        Optional<KieServerInfo> kieServerInfo = kieServerManagement.queryKieInfo(serverId);
        if(!kieServerInfo.isPresent()){
            String errorMessage = String.format("Cannot find kie server by id: %s.", serverId);
            RecordLog.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        String url = kieConfig.getKieBaseUrl(kieServerInfo.get().getLabel().getProject());
        kieConfigClient.deleteConfig(url, ruleId);
    }
}
