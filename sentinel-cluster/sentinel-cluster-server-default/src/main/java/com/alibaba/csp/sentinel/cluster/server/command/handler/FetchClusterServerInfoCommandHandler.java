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
package com.alibaba.csp.sentinel.cluster.server.command.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.csp.sentinel.cluster.flow.statistic.limit.GlobalRequestLimiter;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionGroup;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionManager;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.serialization.common.JsonTransformerLoader;
import com.alibaba.csp.sentinel.util.AppNameUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@CommandMapping(name = "cluster/server/info", desc = "get cluster server info")
public class FetchClusterServerInfoCommandHandler implements CommandHandler<String> {
    @SuppressWarnings("unused")
    private static class NamespaceInfo {
        private String namespace;
        private double currentQps;
        private double maxAllowedQps;
        public NamespaceInfo() {
        }
        public NamespaceInfo(String ns, double qps, double maxAllowed) {
            this.namespace = ns;
            this.currentQps = qps;
            this.maxAllowedQps = maxAllowed;
        }
        public String getNamespace() {
            return namespace;
        }
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
        public double getCurrentQps() {
            return currentQps;
        }
        public void setCurrentQps(double currentQps) {
            this.currentQps = currentQps;
        }
        public double getMaxAllowedQps() {
            return maxAllowedQps;
        }
        public void setMaxAllowedQps(double maxAllowedQps) {
            this.maxAllowedQps = maxAllowedQps;
        }
    }

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        Map<String, Object> info = new HashMap<>();
        List<ConnectionGroup> connectionGroups = new ArrayList<>();
        Set<String> namespaceSet = ClusterServerConfigManager.getNamespaceSet();
        for (String namespace : namespaceSet) {
            ConnectionGroup group = ConnectionManager.getOrCreateConnectionGroup(namespace);
            if (group != null) {
                connectionGroups.add(group);
            }
        }

        ServerTransportConfig transportConfig = new ServerTransportConfig()
            .setPort(ClusterServerConfigManager.getPort())
            .setIdleSeconds(ClusterServerConfigManager.getIdleSeconds());
        ServerFlowConfig flowConfig = new ServerFlowConfig()
            .setExceedCount(ClusterServerConfigManager.getExceedCount())
            .setMaxOccupyRatio(ClusterServerConfigManager.getMaxOccupyRatio())
            .setIntervalMs(ClusterServerConfigManager.getIntervalMs())
            .setSampleCount(ClusterServerConfigManager.getSampleCount())
            .setMaxAllowedQps(ClusterServerConfigManager.getMaxAllowedQps());

        List<NamespaceInfo> requestLimitData = buildRequestLimitData(namespaceSet);

        info.put("port", ClusterServerConfigManager.getPort());
        info.put("connection", connectionGroups);
        info.put("requestLimitData", requestLimitData);
        info.put("transport", transportConfig);
        info.put("flow", flowConfig);
        info.put("namespaceSet", namespaceSet);
        info.put("embedded", ClusterServerConfigManager.isEmbedded());

        // Since 1.5.0 the appName is carried so that the caller can identify the appName of the token server.
        info.put("appName", AppNameUtil.getAppName());

        return CommandResponse.ofSuccess(JsonTransformerLoader.serializer().serialize(info));
    }

    private List<NamespaceInfo> buildRequestLimitData(Set<String> namespaceSet) {
        List<NamespaceInfo> array = new ArrayList<>();
        for (String namespace : namespaceSet) {
            array.add(new NamespaceInfo(
                namespace, 
                GlobalRequestLimiter.getCurrentQps(namespace), 
                GlobalRequestLimiter.getMaxAllowedQps(namespace)
            ));
        }
        return array;
    }
}

