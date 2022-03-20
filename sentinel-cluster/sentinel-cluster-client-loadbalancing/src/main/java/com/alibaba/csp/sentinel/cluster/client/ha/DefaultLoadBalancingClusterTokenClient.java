/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.client.ha;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.cluster.client.ClientConstants;
import com.alibaba.csp.sentinel.cluster.client.ClusterTokenClient;
import com.alibaba.csp.sentinel.cluster.client.DefaultClusterTokenClient;
import com.alibaba.csp.sentinel.cluster.client.NettyTransportClient;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.client.config.ServerChangeObserver;
import com.alibaba.csp.sentinel.cluster.client.ha.discovery.SentinelTokenServerDiscovery;
import com.alibaba.csp.sentinel.cluster.client.ha.loadbalance.ConsistentHashTokenClientLoadBalancer;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.*;
import java.util.function.Function;

/**
 * @author icodening
 * @date 2022.03.06
 */
@Spi(order = Spi.ORDER_HIGHEST + 10000)
public class DefaultLoadBalancingClusterTokenClient implements ClusterTokenClient, ServerChangeObserver, TokenServerListChangeObserver {

    private static final String DISCOVERY = "discovery";

    private static final String LOADBALANCER = "loadbalancer";

    private volatile List<ClusterTokenClient> clusterTokenClients;

    private volatile TokenClientLoadBalancer loadBalancer;

    private volatile String tokenServerName;

    private TokenServerDescriptor tokenServerDescriptor;

    private DynamicTokenServerList dynamicTokenServerList;

    public DefaultLoadBalancingClusterTokenClient() {
        ClusterClientConfigManager.addServerChangeObserver(this);
    }

    @Override
    public void onRemoteServerChange(ClusterClientAssignConfig assignConfig) {
        try {
            if (this.dynamicTokenServerList != null) {
                dynamicTokenServerList.stop();
            }
            String url = assignConfig.getServerHost();
            String appName = parseAppName(url);
            Map<String, String> queryParams = parseQueryParams(url);

            String discovery = queryParams.getOrDefault(DISCOVERY, SentinelTokenServerDiscovery.NAME);
            String loadbalancer = queryParams.getOrDefault(LOADBALANCER, ConsistentHashTokenClientLoadBalancer.NAME);
            TokenServerDiscovery tokenServerDiscovery = SpiLoader.of(TokenServerDiscovery.class).loadInstance(discovery);

            FilterableTokenServerDiscovery filterableTokenServerDiscovery = new FilterableTokenServerDiscovery(tokenServerDiscovery);
            DynamicTokenServerList dynamicTokenServerList = new DynamicTokenServerList(appName, filterableTokenServerDiscovery);
            dynamicTokenServerList.registerTokenServerListChangeObserver(this);
            List<TokenServerDescriptor> tokenServers = dynamicTokenServerList.getTokenServers();
            onTokenServerListChange(tokenServers);
            this.dynamicTokenServerList = dynamicTokenServerList;

            this.tokenServerName = appName;
            this.loadBalancer = SpiLoader.of(TokenClientLoadBalancer.class).loadInstance(loadbalancer);
            this.tokenServerDescriptor = new TokenServerDescriptor(appName, -1);
        } catch (Exception e) {
            RecordLog.warn("[DefaultHAClusterTokenClient] Failed to onRemoteServerChange", e);
        }
    }

    @Override
    public void onTokenServerListChange(List<TokenServerDescriptor> tokenServerDescriptors) {
        try {
            List<ClusterTokenClient> clusterTokenClients = new ArrayList<>(tokenServerDescriptors.size());
            for (TokenServerDescriptor tokenServer : tokenServerDescriptors) {
                NettyTransportClient nettyTransportClient = new NettyTransportClient(tokenServer.getHost(), tokenServer.getPort());
                DefaultClusterTokenClient defaultClusterTokenClient = new DefaultClusterTokenClient(nettyTransportClient, tokenServer);
                clusterTokenClients.add(defaultClusterTokenClient);
            }
            stop();
            this.clusterTokenClients = clusterTokenClients;
            start();
        } catch (Exception e) {
            RecordLog.warn("[DefaultHAClusterTokenClient] Failed to onTokenServerListChange", e);
        }

    }

    private String parseAppName(String url) {
        String appName;
        int index = url.indexOf("://");
        int idx = url.indexOf("?");
        if (idx == -1) {
            if (index == -1) {
                appName = url;
            } else {
                appName = url.substring(index + "://".length());
            }
        } else {
            if (index == -1) {
                appName = url.substring(0, idx);
            } else {
                appName = url.substring(index + "://".length(), idx);
            }
        }
        return appName;
    }

    private Map<String, String> parseQueryParams(String url) {
        int idx = url.indexOf("?");
        if (idx == -1) {
            return Collections.emptyMap();
        }
        String queries = url.substring(idx + 1);
        Map<String, String> queryParams = new HashMap<>();
        String[] params = queries.split("&");
        for (String param : params) {
            int eqIdx = param.indexOf("=");
            if (eqIdx == -1
                    || eqIdx + 1 >= param.length()) {
                continue;
            }
            String key = param.substring(0, eqIdx);
            queryParams.put(key, param.substring(eqIdx + 1));
        }
        return queryParams;
    }

    @Override
    public TokenServerDescriptor currentServer() {
        return tokenServerDescriptor;
    }

    @Override
    public void start() throws Exception {
        if (clusterTokenClients != null) {
            for (ClusterTokenClient clusterTokenClient : clusterTokenClients) {
                clusterTokenClient.start();
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (clusterTokenClients != null) {
            for (ClusterTokenClient clusterTokenClient : clusterTokenClients) {
                clusterTokenClient.stop();
            }
        }
    }

    @Override
    public int getState() {
        if (clusterTokenClients == null || clusterTokenClients.isEmpty()) {
            return ClientConstants.CLIENT_STATUS_OFF;
        }
        return ClientConstants.CLIENT_STATUS_STARTED;
    }

    @Override
    public TokenResult requestToken(Long ruleId, int acquireCount, boolean prioritized) {
        LoadBalanceContext loadBalanceContext = new DefaultLoadBalanceContext(tokenServerName);
        loadBalanceContext.setAttribute("ruleId", ruleId);
        loadBalanceContext.setAttribute("acquireCount", acquireCount);
        loadBalanceContext.setAttribute("prioritized", prioritized);

        return requestWithLoadBalanceInternal(loadBalanceContext, (client) -> client.requestToken(ruleId, acquireCount, prioritized));
    }

    @Override
    public TokenResult requestParamToken(Long ruleId, int acquireCount, Collection<Object> params) {
        LoadBalanceContext loadBalanceContext = new DefaultLoadBalanceContext(tokenServerName);
        loadBalanceContext.setAttribute("ruleId", ruleId);
        loadBalanceContext.setAttribute("acquireCount", acquireCount);
        loadBalanceContext.setAttribute("params", params);

        return requestWithLoadBalanceInternal(loadBalanceContext, (client) -> client.requestParamToken(ruleId, acquireCount, params));
    }

    @Override
    public TokenResult requestConcurrentToken(String clientAddress, Long ruleId, int acquireCount) {
        //ignore
        return null;
    }

    @Override
    public void releaseConcurrentToken(Long tokenId) {
        //ignore
    }

    private TokenResult requestWithLoadBalanceInternal(LoadBalanceContext loadBalanceContext, Function<ClusterTokenClient, TokenResult> resultFunction) {
        ClusterTokenClient client = selectClusterTokenClient(loadBalanceContext);
        if (client == null) {
            return new TokenResult(TokenResultStatus.FAIL);
        }
        return resultFunction.apply(client);
    }

    private ClusterTokenClient selectClusterTokenClient(LoadBalanceContext loadBalanceContext) {
        if (loadBalancer == null || clusterTokenClients == null) {
            return null;
        }
        return loadBalancer.select(clusterTokenClients, loadBalanceContext);
    }
}
