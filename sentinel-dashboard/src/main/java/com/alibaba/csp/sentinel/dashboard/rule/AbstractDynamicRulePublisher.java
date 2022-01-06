package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.List;
import java.util.function.BiConsumer;

public class AbstractDynamicRulePublisher<T extends List> implements DynamicRulePublisher<T> {
    private Converter<T, String> converter;
    private BiConsumer<String,String> configService;

    @Override
    public void publish(String appId, T rules) throws Exception {
        AssertUtil.notEmpty(appId, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        configService.accept(appId,converter.convert(rules));
    }

    public AbstractDynamicRulePublisher(BiConsumer<String,String> configService,Converter<T, String> converter) {
        this.configService = configService;
        this.converter = converter;
    }
}
