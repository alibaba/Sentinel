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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;

/**
 * Metrics for frequent ("hot spot") parameters.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParameterMetric {

    private static final int THREAD_COUNT_MAX_CAPACITY = 4000;
    private static final int BASE_PARAM_MAX_CAPACITY = 4000;
    private static final int TOTAL_MAX_CAPACITY = 20_0000;

    private final Object lock = new Object();

    /**
     * Format: (rule, (value, timeRecorder))
     *
     * @since 1.6.0
     */
    private final Map<ParamFlowRule, CacheMap<Object, AtomicLong>> ruleTimeCounters = new HashMap<>();
    /**
     * Format: (rule, (value, tokenCounter))
     *
     * @since 1.6.0
     */
    private final Map<ParamFlowRule, CacheMap<Object, AtomicLong>> ruleTokenCounter = new HashMap<>();
    private final Map<Integer, CacheMap<Object, AtomicInteger>> threadCountMap = new HashMap<>();

    /**
     * Get the token counter for given parameter rule.
     *
     * @param rule valid parameter rule
     * @return the associated token counter
     * @since 1.6.0
     */
    public CacheMap<Object, AtomicLong> getRuleTokenCounter(ParamFlowRule rule) {
        return ruleTokenCounter.get(rule);
    }

    /**
     * Get the time record counter for given parameter rule.
     *
     * @param rule valid parameter rule
     * @return the associated time counter
     * @since 1.6.0
     */
    public CacheMap<Object, AtomicLong> getRuleTimeCounter(ParamFlowRule rule) {
        return ruleTimeCounters.get(rule);
    }

    public void clear() {
        synchronized (lock) {
            threadCountMap.clear();
            ruleTimeCounters.clear();
            ruleTokenCounter.clear();
        }
    }

    public void initialize(ParamFlowRule rule) {
        if (!ruleTimeCounters.containsKey(rule)) {
            synchronized (lock) {
                if (ruleTimeCounters.get(rule) == null) {
                    long size = Math.min(BASE_PARAM_MAX_CAPACITY * rule.getDurationInSec(), TOTAL_MAX_CAPACITY);
                    ruleTimeCounters.put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(size));
                }
            }
        }

        if (!ruleTokenCounter.containsKey(rule)) {
            synchronized (lock) {
                if (ruleTokenCounter.get(rule) == null) {
                    long size = Math.min(BASE_PARAM_MAX_CAPACITY * rule.getDurationInSec(), TOTAL_MAX_CAPACITY);
                    ruleTokenCounter.put(rule, new ConcurrentLinkedHashMapWrapper<Object, AtomicLong>(size));
                }
            }
        }

        if (!threadCountMap.containsKey(rule.getParamIdx())) {
            synchronized (lock) {
                if (threadCountMap.get(rule.getParamIdx()) == null) {
                    threadCountMap.put(rule.getParamIdx(),
                        new ConcurrentLinkedHashMapWrapper<Object, AtomicInteger>(THREAD_COUNT_MAX_CAPACITY));
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void decreaseThreadCount(Object... args) {
        if (args == null) {
            return;
        }

        try {
            for (int index = 0; index < args.length; index++) {
                CacheMap<Object, AtomicInteger> threadCount = threadCountMap.get(index);
                if (threadCount == null) {
                    continue;
                }

                Object arg = args[index];
                if (arg == null) {
                    continue;
                }
                if (Collection.class.isAssignableFrom(arg.getClass())) {

                    for (Object value : ((Collection)arg)) {
                        AtomicInteger oldValue = threadCount.putIfAbsent(value, new AtomicInteger());
                        if (oldValue != null) {
                            int currentValue = oldValue.decrementAndGet();
                            if (currentValue <= 0) {
                                threadCount.remove(value);
                            }
                        }

                    }
                } else if (arg.getClass().isArray()) {
                    int length = Array.getLength(arg);
                    for (int i = 0; i < length; i++) {
                        Object value = Array.get(arg, i);
                        AtomicInteger oldValue = threadCount.putIfAbsent(value, new AtomicInteger());
                        if (oldValue != null) {
                            int currentValue = oldValue.decrementAndGet();
                            if (currentValue <= 0) {
                                threadCount.remove(value);
                            }
                        }

                    }
                } else {
                    AtomicInteger oldValue = threadCount.putIfAbsent(arg, new AtomicInteger());
                    if (oldValue != null) {
                        int currentValue = oldValue.decrementAndGet();
                        if (currentValue <= 0) {
                            threadCount.remove(arg);
                        }
                    }

                }

            }
        } catch (Throwable e) {
            RecordLog.warn("[ParameterMetric] Param exception", e);
        }
    }

    @SuppressWarnings("rawtypes")
    public void addThreadCount(Object... args) {
        if (args == null) {
            return;
        }

        try {
            for (int index = 0; index < args.length; index++) {
                CacheMap<Object, AtomicInteger> threadCount = threadCountMap.get(index);
                if (threadCount == null) {
                    continue;
                }

                Object arg = args[index];

                if (arg == null) {
                    continue;
                }

                if (Collection.class.isAssignableFrom(arg.getClass())) {
                    for (Object value : ((Collection)arg)) {
                        AtomicInteger oldValue = threadCount.putIfAbsent(value, new AtomicInteger());
                        if (oldValue != null) {
                            oldValue.incrementAndGet();
                        } else {
                            threadCount.put(value, new AtomicInteger(1));
                        }

                    }
                } else if (arg.getClass().isArray()) {
                    int length = Array.getLength(arg);
                    for (int i = 0; i < length; i++) {
                        Object value = Array.get(arg, i);
                        AtomicInteger oldValue = threadCount.putIfAbsent(value, new AtomicInteger());
                        if (oldValue != null) {
                            oldValue.incrementAndGet();
                        } else {
                            threadCount.put(value, new AtomicInteger(1));
                        }

                    }
                } else {
                    AtomicInteger oldValue = threadCount.putIfAbsent(arg, new AtomicInteger());
                    if (oldValue != null) {
                        oldValue.incrementAndGet();
                    } else {
                        threadCount.put(arg, new AtomicInteger(1));
                    }

                }

            }

        } catch (Throwable e) {
            RecordLog.warn("[ParameterMetric] Param exception", e);
        }
    }

    public long getThreadCount(int index, Object value) {
        CacheMap<Object, AtomicInteger> cacheMap = threadCountMap.get(index);
        if (cacheMap == null) {
            return 0;
        }

        AtomicInteger count = cacheMap.get(value);
        return count == null ? 0L : count.get();
    }

    /**
     * Get the token counter map. Package-private for test.
     *
     * @return the token counter map
     */
    Map<ParamFlowRule, CacheMap<Object, AtomicLong>> getRuleTokenCounterMap() {
        return ruleTokenCounter;
    }

    Map<Integer, CacheMap<Object, AtomicInteger>> getThreadCountMap() {
        return threadCountMap;
    }

    Map<ParamFlowRule, CacheMap<Object, AtomicLong>> getRuleTimeCounterMap() {
        return ruleTimeCounters;
    }
}
