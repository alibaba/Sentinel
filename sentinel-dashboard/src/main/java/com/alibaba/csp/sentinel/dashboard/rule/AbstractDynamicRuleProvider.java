package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AbstractDynamicRuleProvider<T extends List> implements DynamicRuleProvider<T> {
    Function<String,String> configService;
    Converter<String,T> converter;

    public AbstractDynamicRuleProvider(Function<String, String> configService, Converter<String, T> converter) {
        this.configService = configService;
        this.converter = converter;
    }

    @Override
    public T getRules(String appName) throws Exception {
        String rules = configService.apply(appName);
        if (StringUtil.isEmpty(rules)) {
            return (T) new ArrayList<T>();
        }
        return converter.convert(rules);
    }
}
