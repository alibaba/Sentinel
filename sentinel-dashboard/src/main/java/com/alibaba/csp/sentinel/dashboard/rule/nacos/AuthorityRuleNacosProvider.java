package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dinglang
 * @since 2019-12-31 16:42
 */
    @Component("authorityRuleNacosProvider")
    public class AuthorityRuleNacosProvider implements DynamicRuleProvider<List<AuthorityRuleEntity>> {

        @Autowired
        private ConfigService configService;
        @Autowired
        private Converter<String, List<AuthorityRuleEntity>> converter;

        public static final String FLOW_DATA_ID_POSTFIX = ".authorityRule";
        public static final String GROUP_ID_POSTFIX = ".group";

        @Override
        public List<AuthorityRuleEntity> getRules(String appName) throws Exception {
            String rules = configService.getConfig(appName + FLOW_DATA_ID_POSTFIX, appName + GROUP_ID_POSTFIX, 3000);
            if (StringUtil.isEmpty(rules)) {
                return new ArrayList<>();
            }
            return converter.convert(rules);
        }
}
