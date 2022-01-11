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

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.converter.JsonArrayConverter;
import com.alibaba.csp.sentinel.datasource.converter.JsonObjectConverter;
import com.alibaba.csp.sentinel.datasource.converter.JsonSetConverter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.demo.cluster.DemoConstants;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

import java.util.List;
import java.util.Set;

/**
 * @author Eric Zhao
 */
public class DemoClusterServerInitFunc implements InitFunc {

    private final String remoteAddress = "localhost:8848";
    private final String groupId = "SENTINEL_GROUP";
    private final String namespaceSetDataId = "cluster-server-namespace-set";
    private final String serverTransportDataId = "cluster-server-transport-config";

    @Override
    public void init() throws Exception {
        // Register cluster flow rule property supplier which creates data source by namespace.
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            NacosDataSource<List<FlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
                    namespace + DemoConstants.FLOW_POSTFIX, new JsonArrayConverter<>(FlowRule.class));
            return ds.getReader().getProperty();
        });
        // Register cluster parameter flow rule property supplier.
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            NacosDataSource<List<ParamFlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
                    namespace + DemoConstants.PARAM_FLOW_POSTFIX, new JsonArrayConverter<>(ParamFlowRule.class));
            return ds.getReader().getProperty();
        });

        // Server namespace set (scope) data source.
        NacosDataSource<Set<String>> namespaceDs = new NacosDataSource<>(remoteAddress, groupId, namespaceSetDataId, new JsonSetConverter<>(String.class));
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getReader().getProperty());
        // Server transport configuration data source.
        NacosDataSource<ServerTransportConfig> transportConfigDs = new NacosDataSource<>(remoteAddress,
            groupId, serverTransportDataId, new JsonObjectConverter<>(ServerTransportConfig.class));
        ClusterServerConfigManager.registerServerTransportProperty(transportConfigDs.getReader().getProperty());
    }
}
