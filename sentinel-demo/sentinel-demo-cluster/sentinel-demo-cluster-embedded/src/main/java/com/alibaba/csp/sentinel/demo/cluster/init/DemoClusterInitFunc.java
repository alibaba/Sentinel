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

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.converter.JsonArrayConverter;
import com.alibaba.csp.sentinel.datasource.converter.JsonObjectConverter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.demo.cluster.DemoConstants;
import com.alibaba.csp.sentinel.demo.cluster.entity.ClusterGroupEntity;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Eric Zhao
 */
public class DemoClusterInitFunc implements InitFunc {

    private static final String APP_NAME = AppNameUtil.getAppName();

    private final String remoteAddress = "localhost:8848";
    private final String groupId = "SENTINEL_GROUP";

    private final String flowDataId = APP_NAME + DemoConstants.FLOW_POSTFIX;
    private final String paramDataId = APP_NAME + DemoConstants.PARAM_FLOW_POSTFIX;
    private final String configDataId = APP_NAME + "-cluster-client-config";
    private final String clusterMapDataId = APP_NAME + DemoConstants.CLUSTER_MAP_POSTFIX;

    @Override
    public void init() throws Exception {
        // Register client dynamic rule data source.
        initDynamicRuleProperty();

        // Register token client related data source.
        // Token client common config:
        initClientConfigProperty();
        // Token client assign config (e.g. target token server) retrieved from assign map:
        initClientServerAssignProperty();

        // Register token server related data source.
        // Register dynamic rule data source supplier for token server:
        registerClusterRuleSupplier();
        // Token server transport config extracted from assign map:
        initServerTransportConfigProperty();

        // Init cluster state property for extracting mode from cluster map data source.
        initStateProperty();
    }

    private void initDynamicRuleProperty() {
        NacosDataSource<List<FlowRule>> ruleSource = new NacosDataSource<>(remoteAddress, groupId, flowDataId, new JsonArrayConverter<>(FlowRule.class));
        FlowRuleManager.register2Property(ruleSource.getReader().getProperty());

        NacosDataSource<List<ParamFlowRule>> paramRuleSource = new NacosDataSource<>(remoteAddress, groupId, paramDataId, new JsonArrayConverter<>(ParamFlowRule.class));
        ParamFlowRuleManager.register2Property(paramRuleSource.getReader().getProperty());
    }

    private void initClientConfigProperty() {
        NacosDataSource<ClusterClientConfig> clientConfigDs = new NacosDataSource<>(remoteAddress, groupId, configDataId, new JsonObjectConverter<>(ClusterClientConfig.class));
        ClusterClientConfigManager.registerClientConfigProperty(clientConfigDs.getReader().getProperty());
    }

    private void initServerTransportConfigProperty() {
        NacosDataSource<ServerTransportConfig> serverTransportDs = new NacosDataSource<>(remoteAddress, groupId, clusterMapDataId,
                new JsonObjectConverter<String, ServerTransportConfig>(ServerTransportConfig.class) {
                    @Override
                    public ServerTransportConfig toSentinel(String source) {
                        List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
                        return Optional.ofNullable(groupList)
                                .flatMap(DemoClusterInitFunc.this::extractServerTransportConfig)
                                .orElse(null);
                    }
                });
        ClusterServerConfigManager.registerServerTransportProperty(serverTransportDs.getReader().getProperty());
    }

    private void registerClusterRuleSupplier() {
        // Register cluster flow rule property supplier which creates data source by namespace.
        // Flow rule dataId format: ${namespace}-flow-rules
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            NacosDataSource<List<FlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId, namespace + DemoConstants.FLOW_POSTFIX, new JsonArrayConverter<>(FlowRule.class));
            return ds.getReader().getProperty();
        });
        // Register cluster parameter flow rule property supplier which creates data source by namespace.
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            NacosDataSource<List<ParamFlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId, namespace + DemoConstants.PARAM_FLOW_POSTFIX, new JsonArrayConverter<>(ParamFlowRule.class));
            return ds.getReader().getProperty();
        });
    }

    private void initClientServerAssignProperty() {
        // Cluster map format:
        // [{"clientSet":["112.12.88.66@8729","112.12.88.67@8727"],"ip":"112.12.88.68","machineId":"112.12.88.68@8728","port":11111}]
        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)
        NacosDataSource<ClusterClientAssignConfig> clientAssignDs = new NacosDataSource<>(remoteAddress, groupId, clusterMapDataId,
            new JsonObjectConverter<String, ClusterClientAssignConfig>(ClusterClientAssignConfig.class) {
                @Override
                public ClusterClientAssignConfig toSentinel(String source) {
                    List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
                    return Optional.ofNullable(groupList)
                            .flatMap(DemoClusterInitFunc.this::extractClientAssignment)
                            .orElse(null);
                }
            });
        ClusterClientConfigManager.registerServerAssignProperty(clientAssignDs.getReader().getProperty());
    }

    private void initStateProperty() {
        // Cluster map format:
        // [{"clientSet":["112.12.88.66@8729","112.12.88.67@8727"],"ip":"112.12.88.68","machineId":"112.12.88.68@8728","port":11111}]
        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)
        NacosDataSource<Integer> clusterModeDs = new NacosDataSource<>(remoteAddress, groupId,
            clusterMapDataId, new JsonObjectConverter<String, Integer>(Integer.class) {
                @Override
                public Integer toSentinel(String source) {
                    List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
                    return Optional.ofNullable(groupList)
                            .map(DemoClusterInitFunc.this::extractMode)
                            .orElse(ClusterStateManager.CLUSTER_NOT_STARTED);
            }
        });
        ClusterStateManager.registerProperty(clusterModeDs.getReader().getProperty());
    }

    private int extractMode(List<ClusterGroupEntity> groupList) {
        // If any server group machineId matches current, then it's token server.
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return ClusterStateManager.CLUSTER_SERVER;
        }
        // If current machine belongs to any of the token server group, then it's token client.
        // Otherwise it's unassigned, should be set to NOT_STARTED.
        boolean canBeClient = groupList.stream()
            .flatMap(e -> e.getClientSet().stream())
            .filter(Objects::nonNull)
            .anyMatch(e -> e.equals(getCurrentMachineId()));
        return canBeClient ? ClusterStateManager.CLUSTER_CLIENT : ClusterStateManager.CLUSTER_NOT_STARTED;
    }

    private Optional<ServerTransportConfig> extractServerTransportConfig(List<ClusterGroupEntity> groupList) {
        return groupList.stream()
            .filter(this::machineEqual)
            .findAny()
            .map(e -> new ServerTransportConfig().setPort(e.getPort()).setIdleSeconds(600));
    }

    private Optional<ClusterClientAssignConfig> extractClientAssignment(List<ClusterGroupEntity> groupList) {
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return Optional.empty();
        }
        // Build client assign config from the client set of target server group.
        for (ClusterGroupEntity group : groupList) {
            if (group.getClientSet().contains(getCurrentMachineId())) {
                String ip = group.getIp();
                Integer port = group.getPort();
                return Optional.of(new ClusterClientAssignConfig(ip, port));
            }
        }
        return Optional.empty();
    }

    private boolean machineEqual(/*@Valid*/ ClusterGroupEntity group) {
        return getCurrentMachineId().equals(group.getMachineId());
    }

    private String getCurrentMachineId() {
        // Note: this may not work well for container-based env.
        return HostNameUtil.getIp() + SEPARATOR + TransportConfig.getRuntimePort();
    }

    private static final String SEPARATOR = "@";
}