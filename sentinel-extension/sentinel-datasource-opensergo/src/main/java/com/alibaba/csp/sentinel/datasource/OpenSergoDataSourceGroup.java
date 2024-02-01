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
package com.alibaba.csp.sentinel.datasource;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.traffic.rule.TrafficRoutingRuleGroup;
import com.alibaba.csp.sentinel.traffic.rule.router.TrafficRouterRuleManager;
import com.alibaba.csp.sentinel.util.AssertUtil;

import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.opensergo.ConfigKind;
import io.opensergo.OpenSergoClient;
import io.opensergo.OpenSergoClientManager;
import io.opensergo.proto.fault_tolerance.v1.CircuitBreakerStrategy;
import io.opensergo.proto.fault_tolerance.v1.ConcurrencyLimitStrategy;
import io.opensergo.proto.fault_tolerance.v1.FaultToleranceRule;
import io.opensergo.proto.fault_tolerance.v1.RateLimitStrategy;
import io.opensergo.proto.fault_tolerance.v1.ThrottlingStrategy;
import io.opensergo.subscribe.OpenSergoConfigSubscriber;
import io.opensergo.subscribe.SubscribeKey;

/**
 * <p>Data-source group for subscribing Sentinel rules from OpenSergo control plane.</p>
 *
 * @author Eric Zhao
 */
public class OpenSergoDataSourceGroup {

    private final OpenSergoClient openSergoClient;

    private final String namespace;
    private final String app;

    /**
     * (SentinelRuleKind, SentinelProperty)
     */
    private final ConcurrentMap<String, SentinelProperty> dataSourceMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<ConfigKind, SubscribeKey> subscribeKeyMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> sentinelRuleSubscribeMap = new ConcurrentHashMap<>();

    private final OpenSergoRuleAggregator ruleAggregator;

    private final OpenSergoTrafficRouterParser trafficRouterParser;

    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * @param host      host of OpenSergo Control Plane
     * @param port      port of OpenSergo Control Plane
     * @param namespace namespace to subscribe
     * @param app       appName to subscribe
     */
    public OpenSergoDataSourceGroup(String host, int port, String namespace, String app) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        AssertUtil.notEmpty(app, "app cannot be empty");
        this.openSergoClient = OpenSergoClientManager.get().getOrCreateClient(host, port);
        this.namespace = namespace;
        this.app = app;
        this.ruleAggregator = new OpenSergoRuleAggregator(dataSourceMap);
        this.trafficRouterParser = new OpenSergoTrafficRouterParser();

        initializeDataSourceMap();
        OpenSergoDataSourceGroupManager.addGroup(host + ":" + port, this);
    }

    private void initializeDataSourceMap() {
        dataSourceMap.put(OpenSergoSentinelConstants.KIND_FLOW_RULE, new DynamicSentinelProperty<List<FlowRule>>());
        dataSourceMap.put(OpenSergoSentinelConstants.KIND_CIRCUIT_BREAKER_RULE,
                new DynamicSentinelProperty<List<DegradeRule>>());
    }

    public void unSubscribeTrafficRouterConfig(String namespace, String appName) {
        SubscribeKey key = new SubscribeKey(namespace, appName, ConfigKind.TRAFFIC_ROUTER_STRATEGY);
        openSergoClient.unsubscribeConfig(key);
    }

    /**
     * <p>Subscribe Sentinel traffic rules from OpenSergo control plane.</p>
     */
    public void subscribeTrafficRouterConfig(String namespace, String appName) {
        SubscribeKey key = new SubscribeKey(namespace, appName, ConfigKind.TRAFFIC_ROUTER_STRATEGY);
        openSergoClient.subscribeConfig(key, new OpenSergoConfigSubscriber() {

            @Override
            public boolean onConfigUpdate(SubscribeKey subscribeKey, Object dataList) {
                try {
                    TrafficRoutingRuleGroup trafficRoutingRuleGroup = trafficRouterParser
                            .resolveLabelRouting((List<RouteConfiguration>) dataList);
                    TrafficRouterRuleManager.updateTrafficRouter(trafficRoutingRuleGroup);
                    return true;
                } catch (InvalidProtocolBufferException e) {
                    RecordLog.error("Subscribing OpenSergo config subscribeKey: {} enhance error: {}", subscribeKey, e);
                }

                return false;
            }
        });
        subscribeKeyMap.put(ConfigKind.TRAFFIC_ROUTER_STRATEGY, key);

        RecordLog.info("Subscribing OpenSergo base traffic-router rules for target <{}, {}>", namespace, app);
    }

    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            openSergoClient.start();

            prepareForBaseSubscription();
        }
    }

    public void close() throws Exception {
        if (started.compareAndSet(true, false)) {
            for (Entry<ConfigKind, SubscribeKey> e : subscribeKeyMap.entrySet()) {
                openSergoClient.unsubscribeConfig(e.getValue());
                RecordLog.info("Unsubscribing OpenSergo config for target: {}", e.getValue());
            }

            openSergoClient.close();
        }
    }

    private void prepareForBaseSubscription() {
        SubscribeKey key = new SubscribeKey(namespace, app, ConfigKind.FAULT_TOLERANCE_RULE);

        openSergoClient.subscribeConfig(key, new OpenSergoFaultToleranceRuleSubscriber(ruleAggregator));
        subscribeKeyMap.put(ConfigKind.FAULT_TOLERANCE_RULE, key);

        RecordLog.info("Subscribing OpenSergo base fault-tolerance rules for target <{}, {}>", namespace, app);
    }

    public boolean hasSubscribedFor(String sentinelRuleKind) {
        if (sentinelRuleKind == null) {
            return false;
        }
        return sentinelRuleSubscribeMap.getOrDefault(sentinelRuleKind, false);
    }

    /**
     * <p>Subscribe Sentinel flow rules from OpenSergo control plane.</p>
     *
     * @return {@link SentinelProperty} of Sentinel flow rules
     */
    public synchronized SentinelProperty<List<FlowRule>> subscribeFlowRules() {
        if (!started.get()) {
            throw new IllegalStateException("OpenSergo data-source not started");
        }

        boolean subscribed = hasSubscribedFor(OpenSergoSentinelConstants.KIND_FLOW_RULE);
        SentinelProperty<List<FlowRule>> property = dataSourceMap.get(OpenSergoSentinelConstants.KIND_FLOW_RULE);
        if (subscribed) {
            return property;
        }

        OpenSergoSentinelFlowRuleSubscriber subscriber = new OpenSergoSentinelFlowRuleSubscriber(ruleAggregator);

        SubscribeKey rlsKey = new SubscribeKey(namespace, app, ConfigKind.RATE_LIMIT_STRATEGY);
        openSergoClient.subscribeConfig(rlsKey, subscriber);
        subscribeKeyMap.put(ConfigKind.RATE_LIMIT_STRATEGY, rlsKey);
        RecordLog.info("Subscribing OpenSergo config for target: {}", rlsKey);

        SubscribeKey tsKey = new SubscribeKey(namespace, app, ConfigKind.THROTTLING_STRATEGY);
        openSergoClient.subscribeConfig(tsKey, subscriber);
        subscribeKeyMap.put(ConfigKind.THROTTLING_STRATEGY, tsKey);
        RecordLog.info("Subscribing OpenSergo config for target: {}", tsKey);

        SubscribeKey concurrencyLimitKey = new SubscribeKey(namespace, app, ConfigKind.CONCURRENCY_LIMIT_STRATEGY);
        openSergoClient.subscribeConfig(concurrencyLimitKey, subscriber);
        subscribeKeyMap.put(ConfigKind.CONCURRENCY_LIMIT_STRATEGY, concurrencyLimitKey);
        RecordLog.info("Subscribing OpenSergo config for target: {}", concurrencyLimitKey);

        sentinelRuleSubscribeMap.put(OpenSergoSentinelConstants.KIND_FLOW_RULE, true);

        return property;
    }

    /**
     * <p>Subscribe Sentinel degrade rules from OpenSergo control plane.</p>
     *
     * @return {@link SentinelProperty} of Sentinel degrade rules
     */
    public synchronized SentinelProperty<List<DegradeRule>> subscribeDegradeRules() {
        if (!started.get()) {
            throw new IllegalStateException("OpenSergo data-source not started");
        }

        boolean subscribed = hasSubscribedFor(OpenSergoSentinelConstants.KIND_CIRCUIT_BREAKER_RULE);
        SentinelProperty<List<DegradeRule>> property = dataSourceMap.get(
                OpenSergoSentinelConstants.KIND_CIRCUIT_BREAKER_RULE);
        if (subscribed) {
            return property;
        }

        SubscribeKey subscribeKey = new SubscribeKey(namespace, app, ConfigKind.CIRCUIT_BREAKER_STRATEGY);
        openSergoClient.subscribeConfig(subscribeKey,
                new OpenSergoSentinelCircuitBreakerRuleSubscriber(ruleAggregator));
        subscribeKeyMap.put(ConfigKind.CIRCUIT_BREAKER_STRATEGY, subscribeKey);
        RecordLog.info("Subscribing OpenSergo config for target: {}", subscribeKey);

        sentinelRuleSubscribeMap.put(OpenSergoSentinelConstants.KIND_CIRCUIT_BREAKER_RULE, true);

        return property;
    }

    /**
     * <p>Subscribe Sentinel flow rules from OpenSergo control plane.</p>
     */
    public synchronized void unsubscribeFlowRules() {
        boolean subscribed = hasSubscribedFor(OpenSergoSentinelConstants.KIND_FLOW_RULE);
        if (!subscribed) {
            return;
        }

        SubscribeKey rlsKey = subscribeKeyMap.remove(ConfigKind.RATE_LIMIT_STRATEGY);
        boolean rlRemoved = openSergoClient.unsubscribeConfig(rlsKey);
        SubscribeKey tsKey = subscribeKeyMap.remove(ConfigKind.THROTTLING_STRATEGY);
        boolean tsRemoved = openSergoClient.unsubscribeConfig(tsKey);
        SubscribeKey concurrencyLimitKey = subscribeKeyMap.remove(ConfigKind.CONCURRENCY_LIMIT_STRATEGY);
        boolean clRemoved = openSergoClient.unsubscribeConfig(concurrencyLimitKey);

        sentinelRuleSubscribeMap.remove(OpenSergoSentinelConstants.KIND_FLOW_RULE);

        // NOTE: unsubscribe operation does not affect existing rules in SentinelProperty.
    }

    public synchronized void unsubscribeDegradeRules() {
        boolean subscribed = hasSubscribedFor(OpenSergoSentinelConstants.KIND_CIRCUIT_BREAKER_RULE);
        if (!subscribed) {
            return;
        }

        SubscribeKey cbKey = subscribeKeyMap.remove(ConfigKind.CIRCUIT_BREAKER_STRATEGY);
        boolean cbRemoved = openSergoClient.unsubscribeConfig(cbKey);

        sentinelRuleSubscribeMap.remove(OpenSergoSentinelConstants.KIND_CIRCUIT_BREAKER_RULE);

        // NOTE: unsubscribe operation does not affect existing rules in SentinelProperty.
    }

    public String getNamespace() {
        return namespace;
    }

    public String getApp() {
        return app;
    }

    static class OpenSergoFaultToleranceRuleSubscriber implements OpenSergoConfigSubscriber {

        private final OpenSergoRuleAggregator ruleAggregator;

        public OpenSergoFaultToleranceRuleSubscriber(OpenSergoRuleAggregator ruleAggregator) {
            this.ruleAggregator = ruleAggregator;
        }

        @Override
        public boolean onConfigUpdate(SubscribeKey subscribeKey, Object data) {
            if (subscribeKey.getKind() != ConfigKind.FAULT_TOLERANCE_RULE || !(data instanceof List)) {
                // type or data mismatch
                return false;
            }
            return ruleAggregator.updateFaultToleranceRuleList((List<FaultToleranceRule>) data);
        }
    }

    /**
     * Subscriber for OpenSergo strategies related to Sentinel flow rules.
     */
    static class OpenSergoSentinelFlowRuleSubscriber implements OpenSergoConfigSubscriber {

        private final OpenSergoRuleAggregator ruleAggregator;

        public OpenSergoSentinelFlowRuleSubscriber(OpenSergoRuleAggregator ruleAggregator) {
            this.ruleAggregator = ruleAggregator;
        }

        @Override
        public boolean onConfigUpdate(SubscribeKey subscribeKey, Object data) {
            if (!(data instanceof List)) {
                // data mismatch
                return false;
            }

            switch (subscribeKey.getKind()) {
                case RATE_LIMIT_STRATEGY:
                    return ruleAggregator.updateRateLimitStrategy((List<RateLimitStrategy>) data);
                case THROTTLING_STRATEGY:
                    return ruleAggregator.updateThrottlingStrategy((List<ThrottlingStrategy>) data);
                case CONCURRENCY_LIMIT_STRATEGY:
                    return ruleAggregator.updateConcurrencyLimitStrategy((List<ConcurrencyLimitStrategy>) data);
                default:
                    // Type mismatch
                    return false;
            }
        }
    }

    static class OpenSergoSentinelCircuitBreakerRuleSubscriber implements OpenSergoConfigSubscriber {

        private final OpenSergoRuleAggregator ruleAggregator;

        public OpenSergoSentinelCircuitBreakerRuleSubscriber(OpenSergoRuleAggregator ruleAggregator) {
            this.ruleAggregator = ruleAggregator;
        }

        @Override
        public boolean onConfigUpdate(SubscribeKey subscribeKey, Object data) {
            if (subscribeKey.getKind() != ConfigKind.CIRCUIT_BREAKER_STRATEGY || !(data instanceof List)) {
                // type or data mismatch
                return false;
            }
            return ruleAggregator.updateCircuitBreakerStrategy((List<CircuitBreakerStrategy>) data);
        }
    }
}
