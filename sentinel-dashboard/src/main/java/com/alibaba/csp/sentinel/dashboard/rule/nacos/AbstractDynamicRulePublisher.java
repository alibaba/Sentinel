package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AbstractDynamicRulePublisher<T extends List> implements DynamicRulePublisher<T> {
    private String rulepath;
    private Converter<T, String> converter;

    @Autowired
    private ConfigService configService;

    @Override
    public void publish(String appId, T rules) throws Exception {
        AssertUtil.notEmpty(appId, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        configService.publishConfig(appId,rulepath,converter.convert(rules));
    }

    public AbstractDynamicRulePublisher() {
    }

    public AbstractDynamicRulePublisher(String rulepath,Converter<T, String> converter) {
        this.rulepath = rulepath;
        this.converter = converter;
    }
}
