package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.RuleConfigTypeEnum;
import com.alibaba.csp.sentinel.dashboard.datasource.ds.nacos.NacosProperties;
import com.alibaba.csp.sentinel.dashboard.rule.AbstractpersistentRuleApiClient;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
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
public class NacosApiClient<T> extends AbstractpersistentRuleApiClient<T> {

    @Autowired
    private NacosProperties nacosProperties;
    @Autowired
    private ConfigService configService;

    private String getRuleConfigId(String appName, RuleConfigTypeEnum ruleFix) {
        appName = StringUtils.isBlank(appName) ? "Sentinel" : appName;
        return String.format("%s-%s", appName, ruleFix.getValue());
    }

    @Override
    public List<T> fetch(String app, RuleConfigTypeEnum configType) throws Exception {
        String ruleName = this.getRuleConfigId(app, configType);
        String rulesJson = configService.getConfig(ruleName, nacosProperties.getGroupId(), 3000);
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
        String ruleName = this.getRuleConfigId(app, configType);
        String groupId = nacosProperties.getGroupId();
        String rulesJson = JSON.toJSONString(rules,true);
        configService.publishConfig(ruleName, groupId, rulesJson);
    }

}
