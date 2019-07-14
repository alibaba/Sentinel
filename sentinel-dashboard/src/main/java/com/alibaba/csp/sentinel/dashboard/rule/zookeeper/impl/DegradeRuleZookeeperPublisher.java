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
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.impl;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.AbstractRuleZookeeperPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.ZookeeperConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("degradeRuleCustomPublisher")
@ConditionalOnBean(ZookeeperConfig.class)
public class DegradeRuleZookeeperPublisher extends AbstractRuleZookeeperPublisher<DegradeRuleEntity> {
    @Autowired
    private Converter<List<DegradeRuleEntity>, String> converter;

    @Override
    protected String getType() {
        return ZookeeperConfig.RULE_TYPE_DEGRADE;
    }

    @Override
    protected Converter<List<DegradeRuleEntity>, String> getConverter() {
        return converter;
    }
}