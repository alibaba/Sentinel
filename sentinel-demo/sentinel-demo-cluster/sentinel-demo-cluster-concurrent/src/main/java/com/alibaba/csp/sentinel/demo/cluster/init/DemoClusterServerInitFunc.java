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
package com.alibaba.csp.sentinel.demo.cluster.init;

import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.Collections;
import java.util.List;

/**
 * @author Eric Zhao
 */
public class DemoClusterServerInitFunc implements InitFunc {

    private static final String remoteAddress = "localhost:8848";
    private static final String groupId = "SENTINEL_GROUP";
    public static final String APP_NAME = "appA";
    public static final String FLOW_POSTFIX = "-flow-rules";


//the rule is as follows:
//[
//    {
//        "clusterConfig":{
//                "acquireRefuseStrategy":0,
//                "clientOfflineTime":1000,
//                "fallbackToLocalWhenFail": true,
//                "flowId":222,
//                "resourceTimeout":1000,
//                "resourceTimeoutStrategy":0,
//                "sampleCount":1000,
//                "strategy":0,
//                "thresholdType":1,
//                "windowIntervalMs":1000
//    },
//            "clusterMode":true,
//            "controlBehavior":0,
//            "count":40,
//            "grade":0,
//            "limitApp":"default",
//            "maxQueueingTimeMs":1000,
//            "resource":"cluster-resource2",
//            "strategy":0,
//            "warmUpPeriodSec":10
//    },
//]


    @Override
    public void init() throws Exception {
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
                    namespace + FLOW_POSTFIX,
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                    }));
            return ds.getProperty();
        });

        ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
                APP_NAME + FLOW_POSTFIX,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(ds.getProperty());
        initClusterClient();
        initClusterServer();
    }

    private static void initClusterServer() {
        ServerTransportConfig ServerTransportConfig = new ServerTransportConfig(18730, 600);
        ClusterServerConfigManager.loadGlobalTransportConfig(ServerTransportConfig);
        ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton(APP_NAME));
    }

    private static void initClusterClient() {
        ClusterClientConfig clusterClientConfig = new ClusterClientConfig();
        clusterClientConfig.setRequestTimeout(1500);
        ClusterClientConfigManager.applyNewConfig(clusterClientConfig);
        ClusterClientAssignConfig clusterClientAssignConfig = new ClusterClientAssignConfig("127.0.0.1", 18730);
        ClusterClientConfigManager.applyNewAssignConfig(clusterClientAssignConfig);
    }
}
