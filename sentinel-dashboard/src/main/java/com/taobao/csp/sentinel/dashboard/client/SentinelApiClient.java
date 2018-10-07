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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.fastjson.JSON;

import com.taobao.csp.sentinel.dashboard.datasource.entity.DegradeRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.FlowRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.SystemRuleEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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
    private static final Charset defaultCharset = Charset.forName(SentinelConfig.charset());

    private CloseableHttpAsyncClient httpClient;

    private final String resourceUrlPath = "jsonTree";
    private final String clusterNodePath = "clusterNode";
    private final String getRulesPath = "getRules";
    private final String setRulesPath = "setRules";
    private final String flowRuleType = "flow";
    private final String degradeRuleType = "degrade";
    private final String systemRuleType = "system";

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
        String url = "http://" + ip + ":" + port + "/" + resourceUrlPath + "?type=" + type;
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
        String url = "http://" + ip + ":" + port + "/" + clusterNodePath + "?type=" + type;
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
        String url = "http://" + ip + ":" + port + "/" + getRulesPath + "?type=" + flowRuleType;
        String body = httpGetContent(url);
        logger.info("FlowRule Body:{}", body);
        List<FlowRule> rules = parseFlowRule(body);
        if (rules != null) {
            return rules.stream().map(rule -> FlowRuleEntity.fromFlowRule(app, ip, port, rule))
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<DegradeRuleEntity> fetchDegradeRuleOfMachine(String app, String ip, int port) {
        String url = "http://" + ip + ":" + port + "/" + getRulesPath + "?type=" + degradeRuleType;
        String body = httpGetContent(url);
        logger.info("Degrade Body:{}", body);
        List<DegradeRule> rules = parseDegradeRule(body);
        if (rules != null) {
            return rules.stream().map(rule -> DegradeRuleEntity.fromDegradeRule(app, ip, port, rule))
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<SystemRuleEntity> fetchSystemRuleOfMachine(String app, String ip, int port) {
        String url = "http://" + ip + ":" + port + "/" + getRulesPath + "?type=" + systemRuleType;
        String body = httpGetContent(url);
        logger.info("SystemRule Body:{}", body);
        List<SystemRule> rules = parseSystemRule(body);
        if (rules != null) {
            return rules.stream().map(rule -> SystemRuleEntity.fromSystemRule(app, ip, port, rule))
                .collect(Collectors.toList());
        } else {
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
            data = URLEncoder.encode(data, defaultCharset.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode rule error", e);
            return false;
        }
        String url = "http://" + ip + ":" + port + "/" + setRulesPath + "?type=" + flowRuleType + "&data=" + data;
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
            data = URLEncoder.encode(data, defaultCharset.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode rule error", e);
            return false;
        }
        String url = "http://" + ip + ":" + port + "/" + setRulesPath + "?type=" + degradeRuleType + "&data=" + data;
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
            data = URLEncoder.encode(data, defaultCharset.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode rule error", e);
            return false;
        }
        String url = "http://" + ip + ":" + port + "/" + setRulesPath + "?type=" + systemRuleType + "&data=" + data;
        String result = httpGetContent(url);
        logger.info("setSystemRule: " + result);
        return true;
    }

    private List<FlowRule> parseFlowRule(String body) {
        try {
            return JSON.parseArray(body, FlowRule.class);
        } catch (Exception e) {
            logger.info("parser FlowRule error: ", e);
            return null;
        }
    }

    private List<DegradeRule> parseDegradeRule(String body) {
        try {
            return JSON.parseArray(body, DegradeRule.class);
        } catch (Exception e) {
            logger.info("parser DegradeRule error: ", e);
            return null;
        }
    }

    private List<SystemRule> parseSystemRule(String body) {
        try {
            return JSON.parseArray(body, SystemRule.class);
        } catch (Exception e) {
            logger.info("parser SystemRule error: ", e);
            return null;
        }
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
        return EntityUtils.toString(response.getEntity(), charset != null ? charset : defaultCharset);
    }

    public void close() throws Exception {
        httpClient.close();
    }
}
