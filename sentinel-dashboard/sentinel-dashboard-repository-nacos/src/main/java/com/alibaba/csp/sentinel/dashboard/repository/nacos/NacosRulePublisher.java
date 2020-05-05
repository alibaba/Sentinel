/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository.nacos;

import com.alibaba.csp.sentinel.dashboard.common.exception.DashboardException;
import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRulePublisher;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cdfive
 */
public class NacosRulePublisher<T extends RuleEntity> extends AbstractRulePublisher<T> {

    @Autowired
    private NacosProperties nacosProperties;

    @Autowired
    private ConfigService configService;

    @Override
    protected void publishRules(String app, String ip, Integer port, String rules) throws Exception {
        String ruleKey = buildRuleKey(app, ip, port);
        boolean result = configService.publishConfig(ruleKey, nacosProperties.getSentinelGroup(), rules);
        if (!result) {
            throw new DashboardException("Nacos publish config error");
        }
    }
}
