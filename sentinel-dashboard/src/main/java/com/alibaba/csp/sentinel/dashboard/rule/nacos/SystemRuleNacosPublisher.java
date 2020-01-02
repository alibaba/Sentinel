package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author dinglang
 * @since 2019-12-31 15:20
 */
    @Component("systemRuleNacosPublisher")
    public class SystemRuleNacosPublisher implements DynamicRulePublisher<List<SystemRuleEntity>> {

        @Autowired
        private ConfigService configService;
        @Autowired
        private Converter<List<SystemRuleEntity>, String> converter;

        public static final String FLOW_DATA_ID_POSTFIX = ".systemRule";
        public static final String GROUP_ID_POSTFIX  = ".group";

        @Override
        public void publish(String app, List<SystemRuleEntity> rules) throws Exception {
            AssertUtil.notEmpty(app, "app name cannot be empty");
            if (rules == null) {
                return;
            }
            configService.publishConfig(app + FLOW_DATA_ID_POSTFIX, app + GROUP_ID_POSTFIX, converter.convert(rules));
        }
    }