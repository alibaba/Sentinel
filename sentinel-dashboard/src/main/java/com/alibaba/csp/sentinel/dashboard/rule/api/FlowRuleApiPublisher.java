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
package com.alibaba.csp.sentinel.dashboard.rule.api;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.rule.AbstractRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This is default rule publisher
 * @author Eric Zhao
 * @since 1.4.0
 */
@ConditionalOnMissingBean(DynamicRulePublisher.class)
@Component("flowRuleDefaultPublisher")
public class FlowRuleApiPublisher<T> extends AbstractRulePublisher<T> {

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @Override
    public void publish(String app, String ip, Integer port, List<T> rules) throws Exception {
//        sentinelApiClient.setFlowRuleOfMachine(app, ip, port, rules);
    }

    @Override
    protected void publishRules(String app, String ip, Integer port, String rules) throws Exception {

    }

//    @Override
//    public void publish(String app, String ip, Integer port, List<T> rules) throws Exception {
////        sentinelApiClient.setFlowRuleOfMachine(app, ip, port, rules);
//    }
}