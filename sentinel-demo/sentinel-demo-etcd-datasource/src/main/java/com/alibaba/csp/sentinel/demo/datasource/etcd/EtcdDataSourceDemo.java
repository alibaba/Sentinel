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
package com.alibaba.csp.sentinel.demo.datasource.etcd;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.etcd.EtcdConfig;
import com.alibaba.csp.sentinel.datasource.etcd.EtcdDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class EtcdDataSourceDemo {

    public static void main(String[] args) {

        String rule_key = "sentinel_demo_rule_key";
        String yourUserName = "root";
        String yourPassWord = "12345";
        String endPoints = "http://127.0.0.1:2379";
        SentinelConfig.setConfig(EtcdConfig.END_POINTS, endPoints);
        SentinelConfig.setConfig(EtcdConfig.USER, yourUserName);
        SentinelConfig.setConfig(EtcdConfig.PASSWORD, yourPassWord);
        SentinelConfig.setConfig(EtcdConfig.CHARSET, "utf-8");
        SentinelConfig.setConfig(EtcdConfig.AUTH_ENABLE, "true");

        ReadableDataSource<String, List<FlowRule>> flowRuleEtcdDataSource = new EtcdDataSource<>(rule_key, (rule) -> JSON.parseArray(rule, FlowRule.class));
        FlowRuleManager.register2Property(flowRuleEtcdDataSource.getProperty());
        List<FlowRule> rules = FlowRuleManager.getRules();
        System.out.println(rules);
    }

}
