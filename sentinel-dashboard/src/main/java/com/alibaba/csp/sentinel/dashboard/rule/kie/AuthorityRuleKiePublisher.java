package com.alibaba.csp.sentinel.dashboard.rule.kie;


import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.KieConfigClient;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigLabel;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigResponse;
import com.alibaba.csp.sentinel.dashboard.config.KieConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerLabel;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component("authorityRuleKiePublisher")
public class AuthorityRuleKiePublisher implements RuleKiePublisher<List<AuthorityRuleEntity>> {
    @Autowired
    KieServerManagement kieServerManagement;

    @Autowired
    KieConfigClient kieConfigClient;

    @Autowired
    KieConfig kieConfig;

    private Optional<KieServerInfo>  getServerInfo(String serverId){
        if(StringUtils.isEmpty(serverId)){
            throw new NullPointerException();
        }
        Optional<KieServerInfo> kieServerInfo = kieServerManagement.queryKieInfo(serverId);
        if(!kieServerInfo.isPresent()){
            String errorMessage = String.format("Cannot find kie server by id: %s.", serverId);
            RecordLog.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        return kieServerInfo;
    }

    @Override
    public void update(String serverId, List<AuthorityRuleEntity> entities) throws Exception {
        if(Objects.isNull(entities)){
            throw new NullPointerException();
        }
        Optional<KieServerInfo> kieServerInfo = getServerInfo(serverId);
        String urlPrefix = kieConfig.getKieBaseUrl(kieServerInfo.get().getLabel().getProject());
        entities.forEach(ruleEntity -> {
            String url = urlPrefix + "/" + ruleEntity.getRuleId();
            AuthorityRule authorityRule = ruleEntity.toRule();
            Optional<KieConfigResponse> response = kieConfigClient.updateConfig(url, authorityRule);
            if(!response.isPresent()){
                RecordLog.error("Update rules failed");
                throw new RuntimeException("Update rules failed");
            }
        });
    }

    @Override
    public void add(String serverId, List<AuthorityRuleEntity> entities) {
        if(Objects.isNull(entities)){
            throw new NullPointerException();
        }
        Optional<KieServerInfo> kieServerInfo = getServerInfo(serverId);
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
            AuthorityRule authorityRule = ruleEntity.toRule();
            Optional<KieConfigResponse> response = kieConfigClient.addConfig(url, "AuthorityRule", authorityRule, kieConfigLabel);
            if(!response.isPresent()){
                RecordLog.error("Add rules failed");
                throw new RuntimeException("Add rules failed");
            }
        });
    }

    @Override
    public void delete(String serverId, String ruleId) {
        if(StringUtils.isEmpty(ruleId)){
            throw new NullPointerException();
        }
        Optional<KieServerInfo> kieServerInfo = getServerInfo(serverId);
        String url = kieConfig.getKieBaseUrl(kieServerInfo.get().getLabel().getProject());
        kieConfigClient.deleteConfig(url, ruleId);
    }
}
