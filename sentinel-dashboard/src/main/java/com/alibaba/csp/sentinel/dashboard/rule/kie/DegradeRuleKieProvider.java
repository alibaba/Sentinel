package com.alibaba.csp.sentinel.dashboard.rule.kie;

import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.KieConfigClient;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigItem;
import com.alibaba.csp.sentinel.dashboard.client.servicecombkie.response.KieConfigResponse;
import com.alibaba.csp.sentinel.dashboard.config.KieConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.KieServerManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.kie.common.KieServerInfo;
import com.alibaba.csp.sentinel.dashboard.rule.kie.util.KieConfigUtil;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("degradeRuleKieProvider")
public class DegradeRuleKieProvider implements RuleKieProvider<List<DegradeRuleEntity>> {

    @Autowired
    KieServerManagement kieServerManagement;

    @Autowired
    KieConfigClient kieConfigClient;

    @Autowired
    KieConfig kieConfig;

    @Override
    public List<DegradeRuleEntity> getRules(String id){
        Optional<KieServerInfo> kieServerInfo = kieServerManagement.queryKieInfo(id);
        if(!kieServerInfo.isPresent()){
            return Collections.emptyList();
        }
        String url = kieConfig.getKieBaseUrl(kieServerInfo.get().getLabel().getProject());
        Optional<KieConfigResponse> response = kieConfigClient.getConfig(url);
        return response.map(configResponse -> parseResponseToEntity(kieServerInfo.get(), configResponse))
                .orElse(Collections.emptyList());
    }

    private List<DegradeRuleEntity> parseResponseToEntity(KieServerInfo kieServerInfo, KieConfigResponse config){
        if (Objects.isNull(config) || Objects.isNull(kieServerInfo)){
            return Collections.emptyList();
        }

        List<DegradeRuleEntity> entityList = new ArrayList<>();
        for(KieConfigItem item : config.getData()){
            if(!KieConfigUtil.isTargetItem("DegradeRule", kieServerInfo, item)){
                continue;
            }

            DegradeRule degradeRule = KieConfigUtil.parseKieConfig(DegradeRule.class, item);
            DegradeRuleEntity entity = DegradeRuleEntity.fromDegradeRule(degradeRule);
            entity.setRuleId(item.getId());
            entityList.add(entity);
        }

        return entityList;
    }
}
