package com.alibaba.csp.sentinel.dashboard.rule.nacos.degrade;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.config.NacosConfigUtil;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:shifengqiang
 * @Date:2020/6/12 5:38 下午
 */
@Component("degradeRuleNacosProviderImpl")
public class DegradeRuleNacosProviderImpl implements DynamicRuleProvider<List<DegradeRuleEntity>> {
    @Autowired
    private ConfigService configService;

    @Autowired
    private Converter<String, List<DegradeRuleEntity>> converter;
    @Override
    public List<DegradeRuleEntity> getRules(String appName) throws Exception {
        String rules = configService.getConfig(appName + NacosConfigUtil.DEGRADE_FLOW_DATA_ID_POSTFIX,
                NacosConfigUtil.getGroupId(), 3000);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);

    }
}
