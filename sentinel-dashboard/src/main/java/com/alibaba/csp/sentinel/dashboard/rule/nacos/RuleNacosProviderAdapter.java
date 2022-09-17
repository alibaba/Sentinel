/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Component
public class RuleNacosProviderAdapter<T> implements DynamicRuleProvider<List<T>> {

    private static final Logger logger = LoggerFactory.getLogger(RuleNacosProviderAdapter.class);

    @Autowired
    private NacosConfig nacosConfig;
    @Autowired
    private ConfigService configService;

    private Converter<String, List<T>> converter = s -> (List<T>) JSON.parseArray(s, new Type[]{new TypeReference<List<T>>() {}.getType()});

    private RuleType ruleType;

    @Override
    public List<T> getRules(String appName) throws Exception {
        String groupId = nacosConfig.getGroupId();
        String dataId = appName + "-" + ruleType.getName();
        logger.info("getRules from dataId:{} groupId:{}", dataId, groupId);
        String rules = configService.getConfig(dataId, groupId, 3000);
        logger.info("rules = {}", rules);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }


    protected void setConverter(Converter<String, List<T>> converter) {
        this.converter = converter;
    }

    protected void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }
}
