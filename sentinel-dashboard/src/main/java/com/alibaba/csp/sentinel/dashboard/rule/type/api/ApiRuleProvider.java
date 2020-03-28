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
package com.alibaba.csp.sentinel.dashboard.rule.type.api;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.AbstractRuleProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This is default rule provider
 *
 * @author Eric Zhao
 */
//@ConditionalOnMissingBean(DynamicRuleProvider.class)
//@ConditionalOnMissingBean
//@Component("flowRuleApiProvider")
public class ApiRuleProvider<T extends RuleEntity> extends AbstractRuleProvider<T> {

    public ApiRuleProvider() {
        System.out.println("ApiRuleProvider");
    }

    @Autowired
    private SentinelApiClient sentinelApiClient;

//    @Override
//    public List<T> getRules(String app, String ip, Integer port) throws Exception {
//        return super.getRules(app, ip, port);
//    }

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        return null;
    }

//    @Override
//    public List<T> getRules(String app, String ip, Integer port) throws Exception {
////        return sentinelApiClient.fetchFlowRuleOfMachine(app, ip, port);
//        return null;
//    }
}
