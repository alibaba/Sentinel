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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricTimerListener;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil.isValidRule;
import static com.alibaba.csp.sentinel.util.AtomicUtil.atomicUpdate;

/**
 * <p>
 * One resources can have multiple rules. And these rules take effects in the following order:
 * <ol>
 * <li>requests from specified caller</li>
 * <li>no specified caller</li>
 * </ol>
 * </p>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @author Weihua
 */
public class FlowRuleManager {

    private static final FlowPropertyListener LISTENER = new FlowPropertyListener();
    /**
     * the corePool size of SCHEDULER must be set at 1, so the two task ({@link #startMetricTimerListener()} can run orderly by the SCHEDULER
     **/
    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1, new NamedThreadFactory("sentinel-metrics-record-task", true));
    private static volatile Map<String, List<FlowRule>> flowRules = new HashMap<>();
    private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty<List<FlowRule>>();
    private static volatile AtomicLong postLock = new AtomicLong(System.currentTimeMillis());
    private static Comparator<FlowRule> comparator = (o1, o2) -> {
        if (o1 == o2)
            return 0;
        if (o1 == null || o2 == null)
            return -1;
        return o1.getResource().equals(o2.getResource()) && (Optional.ofNullable(o1.getLimitApp()).orElse(RuleConstant.LIMIT_APP_DEFAULT).equals(Optional.ofNullable(o2.getLimitApp()).orElse(RuleConstant.LIMIT_APP_DEFAULT))) ? 0 : -1;


    };

    static {
        currentProperty.addListener(LISTENER);
        startMetricTimerListener();
    }

    /**
     * <p> Start the MetricTimerListener
     * <ol>
     *     <li>If the flushInterval more than 0,
     * the timer will run with the flushInterval as the rate </li>.
     *      <li>If the flushInterval less than 0(include) or value is not valid,
     * then means the timer will not be started </li>
     * <ol></p>
     */
    private static void startMetricTimerListener() {
        long flushInterval = SentinelConfig.metricLogFlushIntervalSec();
        if (flushInterval <= 0) {
            RecordLog.info("[FlowRuleManager] The MetricTimerListener isn't started. If you want to start it, " + "please change the value(current: {}) of config({}) more than 0 to start it.", flushInterval, SentinelConfig.METRIC_FLUSH_INTERVAL);
            return;
        }
        SCHEDULER.scheduleAtFixedRate(new MetricTimerListener(), 0, flushInterval, TimeUnit.SECONDS);
    }

    /**
     * Listen to the {@link SentinelProperty} for {@link FlowRule}s. The property is the source of {@link FlowRule}s.
     * Flow rules can also be set by {@link #loadRules(List)} directly.
     *
     * @param property the property to listen.
     */
    public static void register2Property(SentinelProperty<List<FlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[FlowRuleManager] Registering new property to flow rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * Get a copy of the rules.
     *
     * @return a new copy of the rules.
     */
    public static List<FlowRule> getRules() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        for (Map.Entry<String, List<FlowRule>> entry : flowRules.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    /**
     * Load {@link FlowRule}s, former rules will be replaced.
     *
     * @param rules new rules to load.
     */
    public static void loadRules(List<FlowRule> rules) {
        synchronized (FlowRuleManager.class){
            postLock.updateAndGet(fn -> {
                currentProperty.updateValue(rules);
                return System.currentTimeMillis();
            });
        }
    }

    static Map<String, List<FlowRule>> getFlowRuleMap() {
        return flowRules;
    }

    public static boolean hasConfig(String resource) {
        return flowRules.containsKey(resource);
    }

    public static boolean isOtherOrigin(String origin, String resourceName) {
        if (StringUtil.isEmpty(origin)) {
            return false;
        }

        List<FlowRule> rules = flowRules.get(resourceName);

        if (rules != null) {
            for (FlowRule rule : rules) {
                if (origin.equals(rule.getLimitApp())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * append {@link FlowRule}s, former rules will be reserve.
     * same resource name will be replaced
     *
     * @param degradeRules degradeRules to be append or replace
     */
    public static boolean appendAndReplaceRules(List<FlowRule> degradeRules) {

        List<FlowRule> tmp = degradeRules.stream()
                .filter(FlowRuleUtil::isValidRule)
                .collect(Collectors.toList());
        if (tmp.isEmpty()) {
            //if all input is not valid, return true
            return true;
        }
        Supplier<List<FlowRule>> supplier = () -> {
            List<FlowRule> oldRules = getRules();
            //remove all same resource rules
            oldRules.removeIf(item -> tmp.stream().anyMatch(finder -> comparator.compare(finder, item) == 0));
            //append and replace
            oldRules.addAll(tmp);
            loadRules(oldRules);
            return oldRules;
        };
        synchronized (FlowRuleManager.class){
            return atomicUpdate(postLock, supplier);
        }


    }

    /**
     * delete {@link FlowRule}s which resource name was same,
     * other rules will reserve
     * will be reserve.
     *
     * @param degradeRules degradeRules to be delete
     */
    public static boolean deleteRules(List<FlowRule> degradeRules) {
        List<FlowRule> tmp = degradeRules.stream()
                .filter(FlowRuleUtil::isValidRule)
                .collect(Collectors.toList());
        if (tmp.isEmpty()) {
            //if all input is not valid, return true
            return true;
        }
        Supplier<List<FlowRule>> supplier = () -> {
            List<FlowRule> oldRules = getRules();
            //remove all rule which resource and limit app is same
            oldRules.removeIf(item -> tmp.stream().anyMatch(finder -> comparator.compare(finder, item) == 0));
            loadRules(oldRules);
            return oldRules;
        };
        synchronized (FlowRuleManager.class){
            return atomicUpdate(postLock, supplier);
        }
    }

    private static final class FlowPropertyListener implements PropertyListener<List<FlowRule>> {

        @Override
        public synchronized void configUpdate(List<FlowRule> value) {
            Map<String, List<FlowRule>> rules = FlowRuleUtil.buildFlowRuleMap(value);
            if (rules != null) {
                flowRules = rules;
            }
            RecordLog.info("[FlowRuleManager] Flow rules received: {}", rules);
        }

        @Override
        public synchronized void configLoad(List<FlowRule> conf) {
            Map<String, List<FlowRule>> rules = FlowRuleUtil.buildFlowRuleMap(conf);
            if (rules != null) {
                flowRules = rules;
            }
            RecordLog.info("[FlowRuleManager] Flow rules loaded: {}", rules);
        }
    }

}
