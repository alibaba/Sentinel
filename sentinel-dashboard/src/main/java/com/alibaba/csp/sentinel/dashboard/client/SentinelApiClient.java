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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.command.CommandConstants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.util.AsyncUtils;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.SentinelVersion;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterClientInfoVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterServerStateVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.state.ClusterStateSimpleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.dashboard.util.VersionUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Communicate with Sentinel client.
 *
 * @author leyou
 */
@Component
public class SentinelApiClient {
    private static Logger logger = LoggerFactory.getLogger(SentinelApiClient.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName(SentinelConfig.charset());

    private static final String RESOURCE_URL_PATH = "jsonTree";
    private static final String CLUSTER_NODE_PATH = "clusterNode";
    private static final String GET_RULES_PATH = "getRules";
    private static final String SET_RULES_PATH = "setRules";
    private static final String GET_PARAM_RULE_PATH = "getParamFlowRules";
    private static final String SET_PARAM_RULE_PATH = "setParamFlowRules";

    private static final String FETCH_CLUSTER_MODE_PATH = "getClusterMode";
    private static final String MODIFY_CLUSTER_MODE_PATH = "setClusterMode";
    private static final String FETCH_CLUSTER_CLIENT_CONFIG_PATH = "cluster/client/fetchConfig";
    private static final String MODIFY_CLUSTER_CLIENT_CONFIG_PATH = "cluster/client/modifyConfig";

    private static final String FETCH_CLUSTER_SERVER_ALL_CONFIG_PATH = "cluster/server/fetchConfig";
    private static final String FETCH_CLUSTER_SERVER_BASIC_INFO_PATH = "cluster/server/info";

    private static final String MODIFY_CLUSTER_SERVER_TRANSPORT_CONFIG_PATH = "cluster/server/modifyTransportConfig";
    private static final String MODIFY_CLUSTER_SERVER_FLOW_CONFIG_PATH = "cluster/server/modifyFlowConfig";
    private static final String MODIFY_CLUSTER_SERVER_NAMESPACE_SET_PATH = "cluster/server/modifyNamespaceSet";

    private static final String FETCH_GATEWAY_API_PATH = "gateway/getApiDefinitions";
    private static final String MODIFY_GATEWAY_API_PATH = "gateway/updateApiDefinitions";

    private static final String FETCH_GATEWAY_FLOW_RULE_PATH = "gateway/getRules";
    private static final String MODIFY_GATEWAY_FLOW_RULE_PATH = "gateway/updateRules";

    private static final String FLOW_RULE_TYPE = "flow";
    private static final String DEGRADE_RULE_TYPE = "degrade";
    private static final String SYSTEM_RULE_TYPE = "system";
    private static final String AUTHORITY_TYPE = "authority";

    private CloseableHttpAsyncClient httpClient;

    private static final SentinelVersion version160 = new SentinelVersion(1, 6, 0);
    
    @Autowired
    private AppManagement appManagement;

    public SentinelApiClient() {
        IOReactorConfig ioConfig = IOReactorConfig.custom().setConnectTimeout(3000).setSoTimeout(10000)
            .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2).build();
        httpClient = HttpAsyncClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            protected boolean isRedirectable(final String method) {
                return false;
            }
        }).setMaxConnTotal(4000).setMaxConnPerRoute(1000).setDefaultIOReactorConfig(ioConfig).build();
        httpClient.start();
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
    
    private boolean isCommandNotFound(int statusCode, String body) {
        return statusCode == 400 && StringUtil.isNotEmpty(body) && body.contains(CommandConstants.MSG_UNKNOWN_COMMAND_PREFIX);
    }
    
    private StringBuilder queryString(Map<String, String> params) {
        StringBuilder queryStringBuilder = new StringBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            if (StringUtil.isEmpty(entry.getValue())) {
                continue;
            }
            String name = urlEncode(entry.getKey());
            String value = urlEncode(entry.getValue());
            if (name != null && value != null) {
                if (queryStringBuilder.length() > 0) {
                    queryStringBuilder.append('&');
                }
                queryStringBuilder.append(name).append('=').append(value);
            }
        }
        return queryStringBuilder;
    }
    
    private HttpUriRequest postRequest(String url, Map<String, String> params) {
        HttpPost httpPost = new HttpPost(url);
        if (params != null && params.size() > 0) {
            List<NameValuePair> list = new ArrayList<>(params.size());
            for (Entry<String, String> entry : params.entrySet()) {
                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(list));
            } catch (UnsupportedEncodingException e) {
                logger.warn("httpPostContent encode entity error: {}", params, e);
                return null;
            }
        }
        return httpPost;
    }
    
    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode string error: {}", str, e);
            return null;
        }
    }
    
    private String getBody(HttpResponse response) throws Exception {
        Charset charset = null;
        try {
            String contentTypeStr = response.getFirstHeader("Content-type").getValue();
            if (StringUtil.isNotEmpty(contentTypeStr)) {
                ContentType contentType = ContentType.parse(contentTypeStr);
                charset = contentType.getCharset();
            }
        } catch (Exception ignore) {
        }
        return EntityUtils.toString(response.getEntity(), charset != null ? charset : DEFAULT_CHARSET);
    }
    
    /**
     * With no param
     * 
     * @param ip
     * @param port
     * @param api
     * @return
     */
    private CompletableFuture<String> executeCommand(String ip, int port, String api, boolean useHttpPost) {
        return executeCommand(ip, port, api, null, useHttpPost);
    }
    
    /**
     * No app specified, force to GET
     * 
     * @param ip
     * @param port
     * @param api
     * @param params
     * @return
     */
    private CompletableFuture<String> executeCommand(String ip, int port, String api, Map<String, String> params, boolean useHttpPost) {
        return executeCommand(null, ip, port, api, params, useHttpPost);
    }

    /**
     * Prefer to execute request using POST
     * 
     * @param app
     * @param ip
     * @param port
     * @param api
     * @param params
     * @return
     */
    private CompletableFuture<String> executeCommand(String app, String ip, int port, String api, Map<String, String> params, boolean useHttpPost) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (StringUtil.isBlank(ip) || StringUtil.isBlank(api)) {
            future.completeExceptionally(new IllegalArgumentException("Bad URL or command name"));
            return future;
        }
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://");
        urlBuilder.append(ip).append(':').append(port).append('/').append(api);
        if (params == null) {
            params = Collections.emptyMap();
        }
        boolean supportPost = StringUtil.isNotEmpty(app) && Optional.ofNullable(appManagement.getDetailApp(app))
                .flatMap(e -> e.getMachine(ip, port))
                .flatMap(m -> VersionUtils.parseVersion(m.getVersion())
                    .map(v -> v.greaterOrEqual(version160)))
                .orElse(false);
        if (!useHttpPost || !supportPost) {
            // Using GET in older versions, append parameters after url
            if (!params.isEmpty()) {
                if (urlBuilder.indexOf("?") == -1) {
                    urlBuilder.append('?');
                } else {
                    urlBuilder.append('&');
                }
                urlBuilder.append(queryString(params));
            }
            return executeCommand(new HttpGet(urlBuilder.toString()));
        } else {
            // Using POST
            return executeCommand(postRequest(urlBuilder.toString(), params));
        }
    }
    
    private CompletableFuture<String> executeCommand(HttpUriRequest request) {
        CompletableFuture<String> future = new CompletableFuture<>();
        httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                int statusCode = response.getStatusLine().getStatusCode();
                try {
                    String value = getBody(response);
                    if (isSuccess(statusCode)) {
                        future.complete(value);
                    } else {
                        if (isCommandNotFound(statusCode, value)) {
                            future.completeExceptionally(new CommandNotFoundException(request.getURI().getPath()));
                        } else {
                            future.completeExceptionally(new CommandFailedException(value));
                        }
                    }

                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                    logger.error("HTTP request failed: {}", request.getURI().toString(), ex);
                }
            }

            @Override
            public void failed(final Exception ex) {
                future.completeExceptionally(ex);
                logger.error("HTTP request failed: {}", request.getURI().toString(), ex);
            }

            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        return future;
    }
    
    public void close() throws Exception {
        httpClient.close();
    }
    
    @Nullable
    private <T> CompletableFuture<List<T>> fetchItemsAsync(String ip, int port, String api, String type, Class<T> ruleType) {
        AssertUtil.notEmpty(ip, "Bad machine IP");
        AssertUtil.isTrue(port > 0, "Bad machine port");
        Map<String, String> params = null;
        if (StringUtil.isNotEmpty(type)) {
            params = new HashMap<>(1);
            params.put("type", type);
        }
        return executeCommand(ip, port, api, params, false)
                .thenApply(json -> JSON.parseArray(json, ruleType));
    }
    
    @Nullable
    private <T> List<T> fetchItems(String ip, int port, String api, String type, Class<T> ruleType) {
        try {
            AssertUtil.notEmpty(ip, "Bad machine IP");
            AssertUtil.isTrue(port > 0, "Bad machine port");
            Map<String, String> params = null;
            if (StringUtil.isNotEmpty(type)) {
                params = new HashMap<>(1);
                params.put("type", type);
            }
            return fetchItemsAsync(ip, port, api, type, ruleType).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error when fetching items from api: {} -> {}", api, type, e);
            return null;
        } catch (Exception e) {
            logger.error("Error when fetching items: {} -> {}", api, type, e);
            return null;
        }
    }
    
    private <T extends Rule> List<T> fetchRules(String ip, int port, String type, Class<T> ruleType) {
        return fetchItems(ip, port, GET_RULES_PATH, type, ruleType);
    }
    
    private boolean setRules(String app, String ip, int port, String type, List<? extends RuleEntity> entities) {
        if (entities == null) {
            return true;
        }
        try {
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(ip, "Bad machine IP");
            AssertUtil.isTrue(port > 0, "Bad machine port");
            String data = JSON.toJSONString(
                    entities.stream().map(r -> r.toRule()).collect(Collectors.toList()));
            Map<String, String> params = new HashMap<>(2);
            params.put("type", type);
            params.put("data", data);
            String result = executeCommand(app, ip, port, SET_RULES_PATH, params, true).get();
            logger.info("setRules: {}", result);
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("setRules api failed: {}", type, e);
            return false;
        } catch (Exception e) {
            logger.warn("setRules failed", e);
            return false;
        }
    }

    public List<NodeVo> fetchResourceOfMachine(String ip, int port, String type) {
        return fetchItems(ip, port, RESOURCE_URL_PATH, type, NodeVo.class);
    }

    /**
     * Fetch cluster node.
     *
     * @param ip          ip to fetch
     * @param port        port of the ip
     * @param includeZero whether zero value should in the result list.
     * @return
     */
    public List<NodeVo> fetchClusterNodeOfMachine(String ip, int port, boolean includeZero) {
        String type = "noZero";
        if (includeZero) {
            type = "zero";
        }
        return fetchItems(ip, port, CLUSTER_NODE_PATH, type, NodeVo.class);
    }

    public List<FlowRuleEntity> fetchFlowRuleOfMachine(String app, String ip, int port) {
        List<FlowRule> rules = fetchRules(ip, port, FLOW_RULE_TYPE, FlowRule.class);
        if (rules != null) {
            return rules.stream().map(rule -> FlowRuleEntity.fromFlowRule(app, ip, port, rule))
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<DegradeRuleEntity> fetchDegradeRuleOfMachine(String app, String ip, int port) {
        List<DegradeRule> rules = fetchRules(ip, port, DEGRADE_RULE_TYPE, DegradeRule.class);
        if (rules != null) {
            return rules.stream().map(rule -> DegradeRuleEntity.fromDegradeRule(app, ip, port, rule))
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<SystemRuleEntity> fetchSystemRuleOfMachine(String app, String ip, int port) {
        List<SystemRule> rules = fetchRules(ip, port, SYSTEM_RULE_TYPE, SystemRule.class);
        if (rules != null) {
            return rules.stream().map(rule -> SystemRuleEntity.fromSystemRule(app, ip, port, rule))
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * Fetch all parameter flow rules from provided machine.
     *
     * @param app  application name
     * @param ip   machine client IP
     * @param port machine client port
     * @return all retrieved parameter flow rules
     * @since 0.2.1
     */
    public CompletableFuture<List<ParamFlowRuleEntity>> fetchParamFlowRulesOfMachine(String app, String ip, int port) {
        try {
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(ip, "Bad machine IP");
            AssertUtil.isTrue(port > 0, "Bad machine port");
            return fetchItemsAsync(ip, port, GET_PARAM_RULE_PATH, null, ParamFlowRule.class)
                .thenApply(rules -> rules.stream()
                    .map(e -> ParamFlowRuleEntity.fromAuthorityRule(app, ip, port, e))
                    .collect(Collectors.toList())
                );
        } catch (Exception e) {
            logger.error("Error when fetching parameter flow rules", e);
            return AsyncUtils.newFailedFuture(e);
        }
    }

    /**
     * Fetch all authority rules from provided machine.
     *
     * @param app  application name
     * @param ip   machine client IP
     * @param port machine client port
     * @return all retrieved authority rules
     * @since 0.2.1
     */
    public List<AuthorityRuleEntity> fetchAuthorityRulesOfMachine(String app, String ip, int port) {
        AssertUtil.notEmpty(app, "Bad app name");
        AssertUtil.notEmpty(ip, "Bad machine IP");
        AssertUtil.isTrue(port > 0, "Bad machine port");
        Map<String, String> params = new HashMap<>(1);
        params.put("type", AUTHORITY_TYPE);
        List<AuthorityRule> rules = fetchRules(ip, port, AUTHORITY_TYPE, AuthorityRule.class);
        return Optional.ofNullable(rules).map(r -> r.stream()
                    .map(e -> AuthorityRuleEntity.fromAuthorityRule(app, ip, port, e))
                    .collect(Collectors.toList())
                ).orElse(null);
    }

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
    public boolean setFlowRuleOfMachine(String app, String ip, int port, List<FlowRuleEntity> rules) {
        return setRules(app, ip, port, FLOW_RULE_TYPE, rules);
    }

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
    public boolean setDegradeRuleOfMachine(String app, String ip, int port, List<DegradeRuleEntity> rules) {
        return setRules(app, ip, port, DEGRADE_RULE_TYPE, rules);
    }

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
    public boolean setSystemRuleOfMachine(String app, String ip, int port, List<SystemRuleEntity> rules) {
        return setRules(app, ip, port, SYSTEM_RULE_TYPE, rules);
    }

    public boolean setAuthorityRuleOfMachine(String app, String ip, int port, List<AuthorityRuleEntity> rules) {
        return setRules(app, ip, port, AUTHORITY_TYPE, rules);
    }

    public CompletableFuture<Void> setParamFlowRuleOfMachine(String app, String ip, int port, List<ParamFlowRuleEntity> rules) {
        if (rules == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            String data = JSON.toJSONString(
                rules.stream().map(ParamFlowRuleEntity::getRule).collect(Collectors.toList())
            );
            Map<String, String> params = new HashMap<>(1);
            params.put("data", data);
            return executeCommand(app, ip, port, SET_PARAM_RULE_PATH, params, true)
                .thenCompose(e -> {
                    if (CommandConstants.MSG_SUCCESS.equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Push parameter flow rules to client failed: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when setting parameter flow rule", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    // Cluster related

    public CompletableFuture<ClusterStateSimpleEntity> fetchClusterMode(String ip, int port) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            return executeCommand(ip, port, FETCH_CLUSTER_MODE_PATH, false)
                .thenApply(r -> JSON.parseObject(r, ClusterStateSimpleEntity.class));
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster mode", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterMode(String ip, int port, int mode) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("mode", String.valueOf(mode));
            return executeCommand(ip, port, MODIFY_CLUSTER_MODE_PATH, params, false)
                .thenCompose(e -> {
                    if (CommandConstants.MSG_SUCCESS.equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster mode: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster mode", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<ClusterClientInfoVO> fetchClusterClientInfoAndConfig(String ip, int port) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            return executeCommand(ip, port, FETCH_CLUSTER_CLIENT_CONFIG_PATH, false)
                .thenApply(r -> JSON.parseObject(r, ClusterClientInfoVO.class));
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster client config", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterClientConfig(String app, String ip, int port, ClusterClientConfig config) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("data", JSON.toJSONString(config));
            return executeCommand(app, ip, port, MODIFY_CLUSTER_CLIENT_CONFIG_PATH, params, true)
                .thenCompose(e -> {
                    if (CommandConstants.MSG_SUCCESS.equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster client config: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster client config", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerFlowConfig(String app, String ip, int port, ServerFlowConfig config) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("data", JSON.toJSONString(config));
            return executeCommand(app, ip, port, MODIFY_CLUSTER_SERVER_FLOW_CONFIG_PATH, params, true)
                .thenCompose(e -> {
                    if (CommandConstants.MSG_SUCCESS.equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server flow config: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server flow config", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerTransportConfig(String app, String ip, int port, ServerTransportConfig config) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(2);
            params.put("port", config.getPort().toString());
            params.put("idleSeconds", config.getIdleSeconds().toString());
            return executeCommand(app, ip, port, MODIFY_CLUSTER_SERVER_TRANSPORT_CONFIG_PATH, params, false)
                .thenCompose(e -> {
                    if (CommandConstants.MSG_SUCCESS.equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server transport config: " + e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server transport config", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerNamespaceSet(String app, String ip, int port, Set<String> set) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("data", JSON.toJSONString(set));
            return executeCommand(app, ip, port, MODIFY_CLUSTER_SERVER_NAMESPACE_SET_PATH, params, true)
                .thenCompose(e -> {
                    if (CommandConstants.MSG_SUCCESS.equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server NamespaceSet", e);
                        return AsyncUtils.newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server NamespaceSet", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<ClusterServerStateVO> fetchClusterServerBasicInfo(String ip, int port) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            return executeCommand(ip, port, FETCH_CLUSTER_SERVER_BASIC_INFO_PATH, false)
                .thenApply(r -> JSON.parseObject(r, ClusterServerStateVO.class));
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster sever all config and basic info", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public CompletableFuture<List<ApiDefinitionEntity>> fetchApis(String app, String ip, int port) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }

        try {
            return executeCommand(ip, port, FETCH_GATEWAY_API_PATH, false)
                    .thenApply(r -> {
                        List<ApiDefinitionEntity> entities = JSON.parseArray(r, ApiDefinitionEntity.class);
                        if (entities != null) {
                            for (ApiDefinitionEntity entity : entities) {
                                entity.setApp(app);
                                entity.setIp(ip);
                                entity.setPort(port);
                            }
                        }
                        return entities;
                    });
        } catch (Exception ex) {
            logger.warn("Error when fetching gateway apis", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public boolean modifyApis(String app, String ip, int port, List<ApiDefinitionEntity> apis) {
        if (apis == null) {
            return true;
        }

        try {
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(ip, "Bad machine IP");
            AssertUtil.isTrue(port > 0, "Bad machine port");
            String data = JSON.toJSONString(
                    apis.stream().map(r -> r.toApiDefinition()).collect(Collectors.toList()));
            Map<String, String> params = new HashMap<>(2);
            params.put("data", data);
            String result = executeCommand(app, ip, port, MODIFY_GATEWAY_API_PATH, params, true).get();
            logger.info("Modify gateway apis: {}", result);
            return true;
        } catch (Exception e) {
            logger.warn("Error when modifying gateway apis", e);
            return false;
        }
    }

    public CompletableFuture<List<GatewayFlowRuleEntity>> fetchGatewayFlowRules(String app, String ip, int port) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return AsyncUtils.newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }

        try {
            return executeCommand(ip, port, FETCH_GATEWAY_FLOW_RULE_PATH, false)
                    .thenApply(r -> {
                        List<GatewayFlowRule> gatewayFlowRules = JSON.parseArray(r, GatewayFlowRule.class);
                        List<GatewayFlowRuleEntity> entities = gatewayFlowRules.stream().map(rule -> GatewayFlowRuleEntity.fromGatewayFlowRule(app, ip, port, rule)).collect(Collectors.toList());
                        return entities;
                    });
        } catch (Exception ex) {
            logger.warn("Error when fetching gateway flow rules", ex);
            return AsyncUtils.newFailedFuture(ex);
        }
    }

    public boolean modifyGatewayFlowRules(String app, String ip, int port, List<GatewayFlowRuleEntity> rules) {
        if (rules == null) {
            return true;
        }

        try {
            AssertUtil.notEmpty(app, "Bad app name");
            AssertUtil.notEmpty(ip, "Bad machine IP");
            AssertUtil.isTrue(port > 0, "Bad machine port");
            String data = JSON.toJSONString(
                    rules.stream().map(r -> r.toGatewayFlowRule()).collect(Collectors.toList()));
            Map<String, String> params = new HashMap<>(2);
            params.put("data", data);
            String result = executeCommand(app, ip, port, MODIFY_GATEWAY_FLOW_RULE_PATH, params, true).get();
            logger.info("Modify gateway flow rules: {}", result);
            return true;
        } catch (Exception e) {
            logger.warn("Error when modifying gateway apis", e);
            return false;
        }
    }
}
