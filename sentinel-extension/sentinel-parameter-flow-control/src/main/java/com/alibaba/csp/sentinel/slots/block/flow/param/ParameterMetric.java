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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.IntervalProperty;
import com.alibaba.csp.sentinel.node.SampleCountProperty;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;
import com.alibaba.csp.sentinel.slots.statistic.cache.ConcurrentLinkedHashMapWrapper;
import com.alibaba.csp.sentinel.slots.statistic.metric.HotParameterLeapArray;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * Metrics for frequent ("hot spot") parameters.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParameterMetric {

    private final int sampleCount;
    private final int intervalMs;

    public ParameterMetric() {
        this(SampleCountProperty.SAMPLE_COUNT, IntervalProperty.INTERVAL);
    }

    public ParameterMetric(int sampleCount, int intervalInMs) {
        AssertUtil.isTrue(sampleCount > 0, "sampleCount should be positive");
        AssertUtil.isTrue(intervalInMs > 0, "window interval should be positive");
        AssertUtil.isTrue(intervalInMs % sampleCount == 0, "time span needs to be evenly divided");
        this.sampleCount = sampleCount;
        this.intervalMs = intervalInMs;
    }

    private Map<Integer, HotParameterLeapArray> rollingParameters =
        new ConcurrentHashMap<Integer, HotParameterLeapArray>();
    private Map<Integer, CacheMap<Object, AtomicInteger>> threadCountMap =
        new ConcurrentHashMap<Integer, CacheMap<Object, AtomicInteger>>();

    public Map<Integer, HotParameterLeapArray> getRollingParameters() {
        return rollingParameters;
    }

    public Map<Integer, CacheMap<Object, AtomicInteger>> getThreadCountMap() {
        return threadCountMap;
    }

    public synchronized void clear() {
        rollingParameters.clear();
        threadCountMap.clear();
    }

    public void initializeForIndex(int index) {
        if (!rollingParameters.containsKey(index)) {
            synchronized (this) {
                // putIfAbsent
                if (rollingParameters.get(index) == null) {
                    rollingParameters.put(index, new HotParameterLeapArray(sampleCount, intervalMs));
                }

                if (threadCountMap.get(index) == null) {
                    threadCountMap.put(index,
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

    public void addPass(int count, Object... args) {
        add(RollingParamEvent.REQUEST_PASSED, count, args);
    }

    public void addBlock(int count, Object... args) {
        add(RollingParamEvent.REQUEST_BLOCKED, count, args);
    }

    @SuppressWarnings("rawtypes")
    private void add(RollingParamEvent event, int count, Object... args) {
        if (args == null) {
            return;
        }
        try {
            for (int index = 0; index < args.length; index++) {
                HotParameterLeapArray param = rollingParameters.get(index);
                if (param == null) {
                    continue;
                }

                Object arg = args[index];
                if (arg == null) {
                    continue;
                }
                if (Collection.class.isAssignableFrom(arg.getClass())) {
                    for (Object value : ((Collection)arg)) {
                        param.addValue(event, count, value);
                    }
                } else if (arg.getClass().isArray()) {
                    int length = Array.getLength(arg);
                    for (int i = 0; i < length; i++) {
                        Object value = Array.get(arg, i);
                        param.addValue(event, count, value);
                    }
                } else {
                    param.addValue(event, count, arg);
                }

            }
        } catch (Throwable e) {
            RecordLog.warn("[ParameterMetric] Param exception", e);
        }
    }

    public double getPassParamQps(int index, Object value) {
        try {
            HotParameterLeapArray parameter = rollingParameters.get(index);
            if (parameter == null || value == null) {
                return -1;
            }
            return parameter.getRollingAvg(RollingParamEvent.REQUEST_PASSED, value);
        } catch (Throwable e) {
            RecordLog.info(e.getMessage(), e);
        }

        return -1;
    }

    public long getBlockParamQps(int index, Object value) {
        try {
            HotParameterLeapArray parameter = rollingParameters.get(index);
            if (parameter == null || value == null) {
                return -1;
            }

            return (long)rollingParameters.get(index).getRollingAvg(RollingParamEvent.REQUEST_BLOCKED, value);
        } catch (Throwable e) {
            RecordLog.info(e.getMessage(), e);
        }

        return -1;
    }

    public Map<Object, Double> getTopPassParamCount(int index, int number) {
        try {
            HotParameterLeapArray parameter = rollingParameters.get(index);
            if (parameter == null) {
                return new HashMap<Object, Double>();
            }

            return parameter.getTopValues(RollingParamEvent.REQUEST_PASSED, number);
        } catch (Throwable e) {
            RecordLog.info(e.getMessage(), e);
        }

        return new HashMap<Object, Double>();
    }

    public long getThreadCount(int index, Object value) {
        if (threadCountMap.get(index) == null) {
            return 0;
        }

        AtomicInteger count = threadCountMap.get(index).get(value);
        return count == null ? 0L : count.get();
    }

    private static final long THREAD_COUNT_MAX_CAPACITY = 4000;
}
