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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
@Primary
public class CustomSentinelApiClient extends SentinelApiClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CustomSentinelApiClient() {
        this.logger.info("use SentinelApiClient {}", this.getClass());
    }

    @Override
    public boolean setFlowRuleOfMachine(String app, String ip, int port, List<FlowRuleEntity> rules) {
//        return super.setFlowRuleOfMachine(app, ip, port, rules);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public CompletableFuture<Void> setFlowRuleOfMachineAsync(String app, String ip, int port, List<FlowRuleEntity> rules) {
//        return super.setFlowRuleOfMachineAsync(app, ip, port, rules);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public boolean setDegradeRuleOfMachine(String app, String ip, int port, List<DegradeRuleEntity> rules) {
//        return super.setDegradeRuleOfMachine(app, ip, port, rules);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public boolean setSystemRuleOfMachine(String app, String ip, int port, List<SystemRuleEntity> rules) {
//        return super.setSystemRuleOfMachine(app, ip, port, rules);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public boolean setAuthorityRuleOfMachine(String app, String ip, int port, List<AuthorityRuleEntity> rules) {
//        return super.setAuthorityRuleOfMachine(app, ip, port, rules);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public CompletableFuture<Void> setParamFlowRuleOfMachine(String app, String ip, int port, List<ParamFlowRuleEntity> rules) {
//        return super.setParamFlowRuleOfMachine(app, ip, port, rules);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public CompletableFuture<Void> modifyClusterMode(String ip, int port, int mode) {
//        return super.modifyClusterMode(ip, port, mode);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public CompletableFuture<Void> modifyClusterClientConfig(String app, String ip, int port, ClusterClientConfig config) {
//        return super.modifyClusterClientConfig(app, ip, port, config);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public CompletableFuture<Void> modifyClusterServerFlowConfig(String app, String ip, int port, ServerFlowConfig config) {
//        return super.modifyClusterServerFlowConfig(app, ip, port, config);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public CompletableFuture<Void> modifyClusterServerTransportConfig(String app, String ip, int port, ServerTransportConfig config) {
//        return super.modifyClusterServerTransportConfig(app, ip, port, config);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public CompletableFuture<Void> modifyClusterServerNamespaceSet(String app, String ip, int port, Set<String> set) {
//        return super.modifyClusterServerNamespaceSet(app, ip, port, set);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public boolean modifyApis(String app, String ip, int port, List<ApiDefinitionEntity> apis) {
//        return super.modifyApis(app, ip, port, apis);
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public boolean modifyGatewayFlowRules(String app, String ip, int port, List<GatewayFlowRuleEntity> rules) {
//        return super.modifyGatewayFlowRules(app, ip, port, rules);
        throw new UnsupportedOperationException("unsupported operation");
    }
}
