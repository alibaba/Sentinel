package com.taobao.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.taobao.csp.sentinel.dashboard.rule.DynamicRuleProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by YL on 2018/12/27
 */
public interface BaseDynamicRuleNacosProvider<T extends RuleEntity> extends DynamicRuleProvider<List<T>> {

    ConfigService getConfigService();

    Converter<String, List<T>> getDecoder();

    String getPostfix();

    @Override
    default List<T> getRules(String appName) throws Exception {
        AssertUtil.assertNotBlank(this.getPostfix(), "postfix cannot be blank");

        String dataId = appName + this.getPostfix();

        String rules = this.getConfigService().getConfig(
                dataId,
                NacosConfigUtil.GROUP_ID,
                3000
        );

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }

        return this.getDecoder().convert(rules);
    }
}
