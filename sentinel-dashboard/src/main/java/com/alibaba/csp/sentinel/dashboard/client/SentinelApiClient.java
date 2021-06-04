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
package com.alibaba.csp.sentinel.dashboard.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterClientInfoVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterServerStateVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterStateSimpleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;

import org.springframework.stereotype.Component;

/**
 * Communicate with Sentinel client.
 *
 * @author leyou
 * @author wxq
 */
@Component
public interface SentinelApiClient extends Closeable {

    void close() throws IOException;

    List<NodeVo> fetchResourceOfMachine(String ip, int port, String type);

    /**
     * Fetch cluster node.
     *
     * @param ip          ip to fetch
     * @param port        port of the ip
     * @param includeZero whether zero value should in the result list.
     * @return
     */
    List<NodeVo> fetchClusterNodeOfMachine(String ip, int port, boolean includeZero);

    List<FlowRuleEntity> fetchFlowRuleOfMachine(String app, String ip, int port);

    List<DegradeRuleEntity> fetchDegradeRuleOfMachine(String app, String ip, int port);

    List<SystemRuleEntity> fetchSystemRuleOfMachine(String app, String ip, int port);

    /**
     * Fetch all parameter flow rules from provided machine.
     *
     * @param app  application name
     * @param ip   machine client IP
     * @param port machine client port
     * @return all retrieved parameter flow rules
     * @since 0.2.1
     */
    CompletableFuture<List<ParamFlowRuleEntity>> fetchParamFlowRulesOfMachine(String app, String ip, int port);

    /**
     * Fetch all authority rules from provided machine.
     *
     * @param app  application name
     * @param ip   machine client IP
     * @param port machine client port
     * @return all retrieved authority rules
     * @since 0.2.1
     */
    List<AuthorityRuleEntity> fetchAuthorityRulesOfMachine(String app, String ip, int port);

    /**
     * set rules of the machine. rules == null will return immediately;
     * rules.isEmpty() means setting the rules to empty.
     *
     * @param app
     * @param ip
     * @param port
     * @param rules
     * @return whether successfully set the rules.
     */
    boolean setFlowRuleOfMachine(String app, String ip, int port, List<FlowRuleEntity> rules);

    CompletableFuture<Void> setFlowRuleOfMachineAsync(String app, String ip, int port, List<FlowRuleEntity> rules);

    /**
     * set rules of the machine. rules == null will return immediately;
     * rules.isEmpty() means setting the rules to empty.
     *
     * @param app
     * @param ip
     * @param port
     * @param rules
     * @return whether successfully set the rules.
     */
    boolean setDegradeRuleOfMachine(String app, String ip, int port, List<DegradeRuleEntity> rules);

    /**
     * set rules of the machine. rules == null will return immediately;
     * rules.isEmpty() means setting the rules to empty.
     *
     * @param app
     * @param ip
     * @param port
     * @param rules
     * @return whether successfully set the rules.
     */
    boolean setSystemRuleOfMachine(String app, String ip, int port, List<SystemRuleEntity> rules);

    boolean setAuthorityRuleOfMachine(String app, String ip, int port, List<AuthorityRuleEntity> rules);

    CompletableFuture<Void> setParamFlowRuleOfMachine(String app, String ip, int port, List<ParamFlowRuleEntity> rules);

    // Cluster related

    CompletableFuture<ClusterStateSimpleEntity> fetchClusterMode(String ip, int port);

    CompletableFuture<Void> modifyClusterMode(String ip, int port, int mode);

    CompletableFuture<ClusterClientInfoVO> fetchClusterClientInfoAndConfig(String ip, int port);

    CompletableFuture<Void> modifyClusterClientConfig(String app, String ip, int port, ClusterClientConfig config);

    CompletableFuture<Void> modifyClusterServerFlowConfig(String app, String ip, int port, ServerFlowConfig config);

    CompletableFuture<Void> modifyClusterServerTransportConfig(String app, String ip, int port, ServerTransportConfig config);

    CompletableFuture<Void> modifyClusterServerNamespaceSet(String app, String ip, int port, Set<String> set);

    CompletableFuture<ClusterServerStateVO> fetchClusterServerBasicInfo(String ip, int port);

    CompletableFuture<List<ApiDefinitionEntity>> fetchApis(String app, String ip, int port);

    boolean modifyApis(String app, String ip, int port, List<ApiDefinitionEntity> apis);

    CompletableFuture<List<GatewayFlowRuleEntity>> fetchGatewayFlowRules(String app, String ip, int port);

    boolean modifyGatewayFlowRules(String app, String ip, int port, List<GatewayFlowRuleEntity> rules);
}
