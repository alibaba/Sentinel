package com.alibaba.csp.sentinel.dashboard.rule.kie;


import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.KieConfigClient;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigItem;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigResponse;
import com.alibaba.csp.sentinel.dashboard.config.KieConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.rule.kie.util.KieConfigUtil;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("authorityRuleKieProvider")
public class AuthorityRuleKieProvider implements RuleKieProvider<List<AuthorityRuleEntity>>{

    @Autowired
    KieServerManagement kieServerManagement;

    @Autowired
    KieConfigClient kieConfigClient;

    @Autowired
    KieConfig kieConfig;

    private List<AuthorityRuleEntity> parseResponseToEntity(KieServerInfo kieServerInfo, KieConfigResponse config){
        if (Objects.isNull(config) || Objects.isNull(kieServerInfo)){
            return Collections.emptyList();
        }
        List<AuthorityRuleEntity> entityList = new ArrayList<>();
        for(KieConfigItem item : config.getData()){
            if(!KieConfigUtil.isTargetItem("AuthorityRule", kieServerInfo, item)){
                continue;
            }
            AuthorityRule authorityRule = KieConfigUtil.parseKieConfig(AuthorityRule.class, item);
            AuthorityRuleEntity entity = new AuthorityRuleEntity(authorityRule);
            entity.setRuleId(item.getId());
            entityList.add(entity);
        }

        return entityList;
    }

    @Override
    public List<AuthorityRuleEntity> getRules(String id) throws Exception {
        Optional<KieServerInfo> kieServerInfo = kieServerManagement.queryKieInfo(id);
        if(!kieServerInfo.isPresent()){
            return Collections.emptyList();
        }
        String url = kieConfig.getKieBaseUrl(kieServerInfo.get().getLabel().getProject());
        Optional<KieConfigResponse> response = kieConfigClient.getConfig(url);
        return response.map(configResponse -> parseResponseToEntity(kieServerInfo.get(), configResponse))
                .orElse(Collections.emptyList());
    }
}
