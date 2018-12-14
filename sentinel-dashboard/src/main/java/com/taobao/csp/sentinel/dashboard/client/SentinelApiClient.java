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
package com.taobao.csp.sentinel.dashboard.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;

import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.taobao.csp.sentinel.dashboard.domain.cluster.ClusterServerStateVO;
import com.taobao.csp.sentinel.dashboard.domain.cluster.ClusterStateSimpleEntity;
import com.taobao.csp.sentinel.dashboard.domain.cluster.config.ClusterClientConfig;
import com.taobao.csp.sentinel.dashboard.domain.cluster.config.ServerFlowConfig;
import com.taobao.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;
import com.taobao.csp.sentinel.dashboard.util.RuleUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String FLOW_RULE_TYPE = "flow";
    private static final String DEGRADE_RULE_TYPE = "degrade";
    private static final String SYSTEM_RULE_TYPE = "system";
    private static final String AUTHORITY_TYPE = "authority";

    private CloseableHttpAsyncClient httpClient;

    private final boolean enableHttps = false;

    public SentinelApiClient() {
        IOReactorConfig ioConfig = IOReactorConfig.custom().setConnectTimeout(3000).setSoTimeout(3000)
            .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2).build();
        httpClient = HttpAsyncClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            protected boolean isRedirectable(final String method) {
                return false;
            }
        }).setMaxConnTotal(4000).setMaxConnPerRoute(1000).setDefaultIOReactorConfig(ioConfig).build();
        httpClient.start();
    }

    public List<NodeVo> fetchResourceOfMachine(String ip, int port, String type) {
        String url = "http://" + ip + ":" + port + "/" + RESOURCE_URL_PATH + "?type=" + type;
        String body = httpGetContent(url);
        if (body == null) {
            return null;
        }
        try {
            return JSON.parseArray(body, NodeVo.class);
        } catch (Exception e) {
            logger.info("parse ResourceOfMachine error", e);
            return null;
        }
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
        String url = "http://" + ip + ":" + port + "/" + CLUSTER_NODE_PATH + "?type=" + type;
        String body = httpGetContent(url);
        if (body == null) {
            return null;
        }
        try {
            return JSON.parseArray(body, NodeVo.class);
        } catch (Exception e) {
            logger.info("parse ClusterNodeOfMachine error", e);
            return null;
        }
    }

    public List<FlowRuleEntity> fetchFlowRuleOfMachine(String app, String ip, int port) {
        String url = "http://" + ip + ":" + port + "/" + GET_RULES_PATH + "?type=" + FLOW_RULE_TYPE;
        String body = httpGetContent(url);
        logger.info("FlowRule Body:{}", body);
        List<FlowRule> rules = RuleUtils.parseFlowRule(body);
        if (rules != null) {
            return rules.stream().map(rule -> FlowRuleEntity.fromFlowRule(app, ip, port, rule))
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<DegradeRuleEntity> fetchDegradeRuleOfMachine(String app, String ip, int port) {
        String url = "http://" + ip + ":" + port + "/" + GET_RULES_PATH + "?type=" + DEGRADE_RULE_TYPE;
        String body = httpGetContent(url);
        logger.info("Degrade Body:{}", body);
        List<DegradeRule> rules = RuleUtils.parseDegradeRule(body);
        if (rules != null) {
            return rules.stream().map(rule -> DegradeRuleEntity.fromDegradeRule(app, ip, port, rule))
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<SystemRuleEntity> fetchSystemRuleOfMachine(String app, String ip, int port) {
        String url = "http://" + ip + ":" + port + "/" + GET_RULES_PATH + "?type=" + SYSTEM_RULE_TYPE;
        String body = httpGetContent(url);
        logger.info("SystemRule Body:{}", body);
        List<SystemRule> rules = RuleUtils.parseSystemRule(body);
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
            URIBuilder uriBuilder = new URIBuilder();
            String commandName = GET_PARAM_RULE_PATH;
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(commandName);
            return executeCommand(commandName, uriBuilder.build())
                .thenApply(RuleUtils::parseParamFlowRule)
                .thenApply(rules -> rules.stream()
                    .map(e -> ParamFlowRuleEntity.fromAuthorityRule(app, ip, port, e))
                    .collect(Collectors.toList())
                );
        } catch (Exception e) {
            logger.error("Error when fetching parameter flow rules", e);
            return newFailedFuture(e);
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
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http").setHost(ip).setPort(port)
            .setPath(GET_RULES_PATH)
            .setParameter("type", AUTHORITY_TYPE);
        try {
            String body = httpGetContent(uriBuilder.build().toString());
            return Optional.ofNullable(body)
                .map(RuleUtils::parseAuthorityRule)
                .map(rules -> rules.stream()
                    .map(e -> AuthorityRuleEntity.fromAuthorityRule(app, ip, port, e))
                    .collect(Collectors.toList())
                )
                .orElse(null);
        } catch (URISyntaxException e) {
            logger.error("Error when fetching authority rules", e);
            return null;
        }
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
        if (rules == null) {
            return true;
        }
        if (ip == null) {
            throw new IllegalArgumentException("ip is null");
        }
        String data = JSON.toJSONString(rules.stream().map(FlowRuleEntity::toFlowRule).collect(Collectors.toList()));
        try {
            data = URLEncoder.encode(data, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode rule error", e);
            return false;
        }
        String url = "http://" + ip + ":" + port + "/" + SET_RULES_PATH + "?type=" + FLOW_RULE_TYPE + "&data=" + data;
        String result = httpGetContent(url);
        logger.info("setFlowRule: " + result);
        return true;
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
        if (rules == null) {
            return true;
        }
        if (ip == null) {
            throw new IllegalArgumentException("ip is null");
        }
        String data = JSON.toJSONString(
            rules.stream().map(DegradeRuleEntity::toDegradeRule).collect(Collectors.toList()));
        try {
            data = URLEncoder.encode(data, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode rule error", e);
            return false;
        }
        String url = "http://" + ip + ":" + port + "/" + SET_RULES_PATH + "?type=" + DEGRADE_RULE_TYPE + "&data="
            + data;
        String result = httpGetContent(url);
        logger.info("setDegradeRule: " + result);
        return true;
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
        if (rules == null) {
            return true;
        }
        if (ip == null) {
            throw new IllegalArgumentException("ip is null");
        }
        String data = JSON.toJSONString(
            rules.stream().map(SystemRuleEntity::toSystemRule).collect(Collectors.toList()));
        try {
            data = URLEncoder.encode(data, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode rule error", e);
            return false;
        }
        String url = "http://" + ip + ":" + port + "/" + SET_RULES_PATH + "?type=" + SYSTEM_RULE_TYPE + "&data=" + data;
        String result = httpGetContent(url);
        logger.info("setSystemRule: " + result);
        return true;
    }

    public boolean setAuthorityRuleOfMachine(String app, String ip, int port, List<AuthorityRuleEntity> rules) {
        if (rules == null) {
            return true;
        }
        if (StringUtil.isBlank(ip) || port <= 0) {
            throw new IllegalArgumentException("Invalid IP or port");
        }
        String data = JSON.toJSONString(
            rules.stream().map(AuthorityRuleEntity::getRule).collect(Collectors.toList()));
        try {
            data = URLEncoder.encode(data, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("Encode rule error", e);
            return false;
        }
        String url = "http://" + ip + ":" + port + "/" + SET_RULES_PATH + "?type=" + AUTHORITY_TYPE + "&data=" + data;
        String result = httpGetContent(url);
        logger.info("Push authority rules: " + result);
        return true;
    }

    public CompletableFuture<Void> setParamFlowRuleOfMachine(String app, String ip, int port, List<ParamFlowRuleEntity> rules) {
        if (rules == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            String data = JSON.toJSONString(
                rules.stream().map(ParamFlowRuleEntity::getRule).collect(Collectors.toList())
            );
            data = URLEncoder.encode(data, DEFAULT_CHARSET.name());
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(SET_PARAM_RULE_PATH)
                .setParameter("data", data);
            return executeCommand(SET_PARAM_RULE_PATH, uriBuilder.build())
                .thenCompose(e -> {
                    if ("success".equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Push parameter flow rules to client failed: " + e);
                        return newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when setting parameter flow rule", ex);
            return newFailedFuture(ex);
        }
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private CompletableFuture<String> executeCommand(String command, URI uri) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (StringUtil.isBlank(command) || uri == null) {
            future.completeExceptionally(new IllegalArgumentException("Bad URL or command name"));
            return future;
        }
        final HttpGet httpGet = new HttpGet(uri);
        httpClient.execute(httpGet, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                int statusCode = response.getStatusLine().getStatusCode();
                try {
                    String value = getBody(response);
                    if (isSuccess(statusCode)) {
                        future.complete(value);
                    } else {
                        if (statusCode == 400) {
                            future.completeExceptionally(new CommandNotFoundException(command));
                        } else {
                            future.completeExceptionally(new IllegalStateException(value));
                        }
                    }

                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                    logger.error("HTTP request failed: " + uri.toString(), ex);
                }
            }

            @Override
            public void failed(final Exception ex) {
                future.completeExceptionally(ex);
                logger.error("HTTP request failed: " + uri.toString(), ex);
            }

            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        return future;
    }

    private String httpGetContent(String url) {
        final HttpGet httpGet = new HttpGet(url);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> reference = new AtomicReference<>();
        httpClient.execute(httpGet, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                try {
                    reference.set(getBody(response));
                } catch (Exception e) {
                    logger.info("httpGetContent " + url + " error:", e);
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void failed(final Exception ex) {
                latch.countDown();
                logger.info("httpGetContent " + url + " failed:", ex);
            }

            @Override
            public void cancelled() {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.info("wait http client error:", e);
        }
        return reference.get();
    }

    private String getBody(HttpResponse response) throws Exception {
        Charset charset = null;
        try {
            String contentTypeStr = response.getFirstHeader("Content-type").getValue();
            ContentType contentType = ContentType.parse(contentTypeStr);
            charset = contentType.getCharset();
        } catch (Exception ignore) {
        }
        return EntityUtils.toString(response.getEntity(), charset != null ? charset : DEFAULT_CHARSET);
    }

    public void close() throws Exception {
        httpClient.close();
    }

    private <R> CompletableFuture<R> newFailedFuture(Throwable ex) {
        CompletableFuture<R> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

    // Cluster related

    public CompletableFuture<ClusterStateSimpleEntity> fetchClusterMode(String app, String ip, int port) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(FETCH_CLUSTER_MODE_PATH);
            return executeCommand(FETCH_CLUSTER_MODE_PATH, uriBuilder.build())
                .thenApply(r -> JSON.parseObject(r, ClusterStateSimpleEntity.class));
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster mode", ex);
            return newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterMode(String app, String ip, int port, int mode) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(MODIFY_CLUSTER_MODE_PATH)
                .setParameter("mode", String.valueOf(mode));
            return executeCommand(MODIFY_CLUSTER_MODE_PATH, uriBuilder.build())
                .thenCompose(e -> {
                    if ("success".equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster mode: " + e);
                        return newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster mode", ex);
            return newFailedFuture(ex);
        }
    }

    public CompletableFuture<ClusterClientConfig> fetchClusterClientConfig(String app, String ip, int port) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(FETCH_CLUSTER_CLIENT_CONFIG_PATH);
            return executeCommand(FETCH_CLUSTER_CLIENT_CONFIG_PATH, uriBuilder.build())
                .thenApply(r -> JSON.parseObject(r, ClusterClientConfig.class));
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster client config", ex);
            return newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterClientConfig(String app, String ip, int port, ClusterClientConfig config) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(MODIFY_CLUSTER_CLIENT_CONFIG_PATH)
                .setParameter("data", JSON.toJSONString(config));
            return executeCommand(MODIFY_CLUSTER_MODE_PATH, uriBuilder.build())
                .thenCompose(e -> {
                    if ("success".equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster client config: " + e);
                        return newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster client config", ex);
            return newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerFlowConfig(String app, String ip, int port, ServerFlowConfig config) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(MODIFY_CLUSTER_SERVER_FLOW_CONFIG_PATH)
                .setParameter("data", JSON.toJSONString(config));
            return executeCommand(MODIFY_CLUSTER_SERVER_FLOW_CONFIG_PATH, uriBuilder.build())
                .thenCompose(e -> {
                    if ("success".equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server flow config: " + e);
                        return newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server flow config", ex);
            return newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerTransportConfig(String app, String ip, int port, ServerTransportConfig config) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(MODIFY_CLUSTER_SERVER_TRANSPORT_CONFIG_PATH)
                .setParameter("port", config.getPort().toString())
                .setParameter("idleSeconds", config.getIdleSeconds().toString());
            return executeCommand(MODIFY_CLUSTER_SERVER_TRANSPORT_CONFIG_PATH, uriBuilder.build())
                .thenCompose(e -> {
                    if ("success".equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server transport config: " + e);
                        return newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server transport config", ex);
            return newFailedFuture(ex);
        }
    }

    public CompletableFuture<Void> modifyClusterServerNamespaceSet(String app, String ip, int port, Set<String> set) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(MODIFY_CLUSTER_SERVER_NAMESPACE_SET_PATH)
                .setParameter("data", JSON.toJSONString(set));
            return executeCommand(MODIFY_CLUSTER_SERVER_NAMESPACE_SET_PATH, uriBuilder.build())
                .thenCompose(e -> {
                    if ("success".equals(e)) {
                        return CompletableFuture.completedFuture(null);
                    } else {
                        logger.warn("Error when modifying cluster server NamespaceSet: " + e);
                        return newFailedFuture(new RuntimeException(e));
                    }
                });
        } catch (Exception ex) {
            logger.warn("Error when modifying cluster server NamespaceSet", ex);
            return newFailedFuture(ex);
        }
    }

    public CompletableFuture<ClusterServerStateVO> fetchClusterServerBasicInfo(String app, String ip, int port) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return newFailedFuture(new IllegalArgumentException("Invalid parameter"));
        }
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http").setHost(ip).setPort(port)
                .setPath(FETCH_CLUSTER_SERVER_BASIC_INFO_PATH);
            return executeCommand(FETCH_CLUSTER_SERVER_BASIC_INFO_PATH, uriBuilder.build())
                .thenApply(r -> JSON.parseObject(r, ClusterServerStateVO.class));
        } catch (Exception ex) {
            logger.warn("Error when fetching cluster sever all config and basic info", ex);
            return newFailedFuture(ex);
        }
    }
}
