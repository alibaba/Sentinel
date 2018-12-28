package com.taobao.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.taobao.csp.sentinel.dashboard.rule.DynamicRulePublisher;

import java.util.List;

/**
 * @author Created by YL on 2018/12/27
 */
public interface BaseDynamicRuleNacosPublisher<T extends RuleEntity> extends DynamicRulePublisher<List<T>> {

    ConfigService getConfigService();

    Converter<List<T>, String> getEncoder();

    String getPostfix();

    @Override
    default void publish(String appName, List<T> rules) throws Exception {
        AssertUtil.assertNotBlank(appName, "app name cannot be blank");
        AssertUtil.assertNotBlank(this.getPostfix(), "postfix cannot be blank");

        if (rules == null) {
            return;
        }

        String dataId = appName + this.getPostfix();

        this.getConfigService().publishConfig(
                dataId,
                NacosConfigUtil.GROUP_ID,
                this.getEncoder().convert(rules)
        );
    }
}
