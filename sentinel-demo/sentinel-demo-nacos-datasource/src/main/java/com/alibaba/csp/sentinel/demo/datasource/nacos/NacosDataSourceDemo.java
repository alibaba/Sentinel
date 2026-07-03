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
package com.alibaba.csp.sentinel.demo.datasource.nacos;

import java.util.List;
import java.util.Properties;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;

/**
 * This demo demonstrates how to use Nacos as the data source of Sentinel rules.
 * Before you start, you need to start a Nacos server in local first, and then
 * use {@link NacosConfigSender} to publish initial rule configuration to Nacos.
 *
 * @author Eric Zhao
 */
public class NacosDataSourceDemo {

    private static final String KEY = "TestResource";
    // nacos server ip
    private static final String remoteAddress = "localhost:8848";
    // nacos group
    private static final String groupId = "Sentinel_Demo";
    // nacos dataId
    private static final String dataId = "com.alibaba.csp.sentinel.demo.flow.rule";
    // if change to true, should be config NACOS_NAMESPACE_ID
    private static boolean isDemoNamespace = false;
    // fill your namespace id,if you want to use namespace. for example: 0f5c7314-4983-4022-ad5a-347de1d1057d,you can get it on nacos's console
    private static final String NACOS_NAMESPACE_ID = "${namespace}";

    public static void main(String[] args) {
        if (isDemoNamespace) {
            loadMyNamespaceRules();
        } else {
            loadRules();
        }

        // Assume we config: resource is `TestResource`, initial QPS threshold is 5.
        FlowQpsRunner runner = new FlowQpsRunner(KEY, 1, 100);
        runner.simulateTraffic();
        runner.tick();
    }

    private static void loadRules() {
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(remoteAddress, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    private static void loadMyNamespaceRules() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, remoteAddress);
        properties.put(PropertyKeyConst.NAMESPACE, NACOS_NAMESPACE_ID);

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(properties, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

}
