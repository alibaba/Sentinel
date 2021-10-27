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
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Component
public class RuleNacosPublisherAdapter<T> implements DynamicRulePublisher<List<T>> {

    private static final Logger logger = LoggerFactory.getLogger(RuleNacosPublisherAdapter.class);

    @Autowired
    private NacosConfig nacosConfig;
    @Autowired
    private ConfigService configService;

    private Converter<List<T>, String> converter = s -> JSON.toJSONString(s, SerializerFeature.PrettyFormat);

    private RuleType ruleType;

    @Override
    public void publish(String app, List<T> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            logger.warn("publish rules is null");
            return;
        }
        String groupId = nacosConfig.getGroupId();
        String dataId = app + "-" + ruleType.getName();
        logger.info("publish dataId:{} groupId:{} content:{}", dataId, groupId, converter.convert(rules));
        configService.publishConfig(dataId, groupId, converter.convert(rules));
    }

    protected void setConverter(Converter<List<T>, String> converter){
        this.converter = converter;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }
}
