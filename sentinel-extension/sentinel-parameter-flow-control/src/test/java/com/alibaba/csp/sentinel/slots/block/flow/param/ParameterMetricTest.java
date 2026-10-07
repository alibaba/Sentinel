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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;

/**
 * Test cases for {@link ParameterMetric}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParameterMetricTest {

    @Test
    public void testInitAndClearParameterMetric() {
        // Create a parameter metric for resource "abc".
        ParameterMetric metric = new ParameterMetric();

        ParamFlowRule rule = new ParamFlowRule("abc")
            .setParamIdx(1);
        metric.initialize(rule);
        CacheMap<Object, AtomicInteger> threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertNotNull(threadCountMap);
        CacheMap<Object, AtomicLong> timeRecordMap = metric.getRuleTimeCounter(rule);
        assertNotNull(timeRecordMap);
        metric.initialize(rule);
        assertSame(threadCountMap, metric.getThreadCountMap().get(rule.getParamIdx()));
        assertSame(timeRecordMap, metric.getRuleTimeCounter(rule));

        ParamFlowRule rule2 = new ParamFlowRule("abc")
            .setParamIdx(1);
        metric.initialize(rule2);
        CacheMap<Object, AtomicLong> timeRecordMap2 = metric.getRuleTimeCounter(rule2);
        assertSame(timeRecordMap, timeRecordMap2);

        rule2.setParamIdx(2);
        metric.initialize(rule2);
        assertNotSame(timeRecordMap2, metric.getRuleTimeCounter(rule2));

        ParamFlowRule rule3 = new ParamFlowRule("abc")
            .setParamIdx(1)
            .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        metric.initialize(rule3);
        assertNotSame(timeRecordMap, metric.getRuleTimeCounter(rule3));

        metric.clear();
        assertEquals(0, metric.getThreadCountMap().size());
        assertEquals(0, metric.getRuleTimeCounterMap().size());
        assertEquals(0, metric.getRuleTokenCounterMap().size());
    }

    @Test
    public void testAddAndDecreaseThreadCountCommon() {
        testAddAndDecreaseThreadCount(PARAM_TYPE_NORMAL);
        testAddAndDecreaseThreadCount(PARAM_TYPE_ARRAY);
        testAddAndDecreaseThreadCount(PARAM_TYPE_COLLECTION);
    }

    private void testAddAndDecreaseThreadCount(int paramType) {

        ParamFlowRule rule = new ParamFlowRule();
        rule.setParamIdx(0);

        int n = 3;
        long[] v = new long[] {19L, 3L, 8L};
        ParameterMetric metric = new ParameterMetric();
        metric.initialize(rule);
        assertTrue(metric.getThreadCountMap().containsKey(rule.getParamIdx()));

        switch (paramType) {
            case PARAM_TYPE_ARRAY:
                metric.addThreadCount((Object)v);
                break;
            case PARAM_TYPE_COLLECTION:
                metric.addThreadCount(Arrays.asList(v[0], v[1], v[2]));
                break;
            case PARAM_TYPE_NORMAL:
            default:
                metric.addThreadCount(v[0]);
                metric.addThreadCount(v[1]);
                metric.addThreadCount(v[2]);
                break;
        }

        assertEquals(1, metric.getThreadCountMap().size());
        CacheMap<Object, AtomicInteger> threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertEquals(v.length, threadCountMap.size());
        for (long vs : v) {
            assertEquals(1, threadCountMap.get(vs).get());
        }

        for (int i = 1; i < n; i++) {
            switch (paramType) {
                case PARAM_TYPE_ARRAY:
                    metric.addThreadCount((Object)v);
                    break;
                case PARAM_TYPE_COLLECTION:
                    metric.addThreadCount(Arrays.asList(v[0], v[1], v[2]));
                    break;
                case PARAM_TYPE_NORMAL:
                default:
                    metric.addThreadCount(v[0]);
                    metric.addThreadCount(v[1]);
                    metric.addThreadCount(v[2]);
                    break;
            }
        }
        assertEquals(1, metric.getThreadCountMap().size());
        threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertEquals(v.length, threadCountMap.size());
        for (long vs : v) {
            assertEquals(n, threadCountMap.get(vs).get());
        }

        for (int i = 1; i < n; i++) {
            switch (paramType) {
                case PARAM_TYPE_ARRAY:
                    metric.decreaseThreadCount((Object)v);
                    break;
                case PARAM_TYPE_COLLECTION:
                    metric.decreaseThreadCount(Arrays.asList(v[0], v[1], v[2]));
                    break;
                case PARAM_TYPE_NORMAL:
                default:
                    metric.decreaseThreadCount(v[0]);
                    metric.decreaseThreadCount(v[1]);
                    metric.decreaseThreadCount(v[2]);
                    break;
            }
        }
        assertEquals(1, metric.getThreadCountMap().size());
        threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertEquals(v.length, threadCountMap.size());
        for (long vs : v) {
            assertEquals(1, threadCountMap.get(vs).get());
        }

        switch (paramType) {
            case PARAM_TYPE_ARRAY:
                metric.decreaseThreadCount((Object)v);
                break;
            case PARAM_TYPE_COLLECTION:
                metric.decreaseThreadCount(Arrays.asList(v[0], v[1], v[2]));
                break;
            case PARAM_TYPE_NORMAL:
            default:
                metric.decreaseThreadCount(v[0]);
                metric.decreaseThreadCount(v[1]);
                metric.decreaseThreadCount(v[2]);
                break;
        }
        assertEquals(1, metric.getThreadCountMap().size());
        threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertEquals(0, threadCountMap.size());
    }

    @Test
    public void testAddThreadCountInterleavedInsertionScalar() {
        assertInterleavedAddThreadCount(PARAM_TYPE_NORMAL);
    }

    @Test
    public void testAddThreadCountInterleavedInsertionArray() {
        assertInterleavedAddThreadCount(PARAM_TYPE_ARRAY);
    }

    @Test
    public void testAddThreadCountInterleavedInsertionCollection() {
        assertInterleavedAddThreadCount(PARAM_TYPE_COLLECTION);
    }

    /**
     * Ensure {@code addThreadCount} does not lose increments when another caller adds the same
     * parameter value while the first insertion is still in progress.
     *
     * <p>The interleaving is reproduced deterministically, without threads, sleeping or retry: the
     * cache map of the metric is decorated so that a second {@code addThreadCount} runs after the
     * delegate insertion has been performed but before {@code putIfAbsent} returns {@code null} to
     * the first caller.</p>
     *
     * @param paramType one of {@link #PARAM_TYPE_NORMAL}, {@link #PARAM_TYPE_ARRAY} or
     *                  {@link #PARAM_TYPE_COLLECTION}, to exercise every argument branch
     */
    private void assertInterleavedAddThreadCount(final int paramType) {
        final Object value = THREAD_COUNT_VALUE;
        final ParamFlowRule rule = new ParamFlowRule("interleavedAddThreadCount").setParamIdx(0);
        final ParameterMetric metric = new ParameterMetric();
        metric.initialize(rule);

        CacheMap<Object, AtomicInteger> realCacheMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertNotNull(realCacheMap);
        // Decorate the actual cache map, so that size/eviction semantics stay those of the metric.
        metric.getThreadCountMap().put(rule.getParamIdx(), new InsertionHookCacheMap(realCacheMap, value,
            new Runnable() {
                @Override
                public void run() {
                    // Another caller adds the same value while the first insertion is in progress.
                    metric.addThreadCount(value);
                }
            }));

        switch (paramType) {
            case PARAM_TYPE_ARRAY:
                metric.addThreadCount((Object)new Object[] {value});
                break;
            case PARAM_TYPE_COLLECTION:
                metric.addThreadCount(Collections.singletonList(value));
                break;
            case PARAM_TYPE_NORMAL:
            default:
                metric.addThreadCount(value);
                break;
        }

        CacheMap<Object, AtomicInteger> threadCountMap = metric.getThreadCountMap().get(rule.getParamIdx());
        assertEquals("Interleaved addThreadCount calls must not lose increments", 2,
            metric.getThreadCount(rule.getParamIdx(), value));
        // A single entry is kept for the value, with no duplicate or leaked counter instance.
        assertEquals(1, threadCountMap.size());

        // The eviction contract is preserved: two decreaseThreadCount calls evict the entry.
        metric.decreaseThreadCount(value);
        metric.decreaseThreadCount(value);
        assertEquals(0, metric.getThreadCount(rule.getParamIdx(), value));
        assertEquals(0, threadCountMap.size());
    }

    private static final int PARAM_TYPE_NORMAL = 0;
    private static final int PARAM_TYPE_ARRAY = 1;
    private static final int PARAM_TYPE_COLLECTION = 2;

    /** Parameter value used by the interleaving regression tests. */
    private static final Object THREAD_COUNT_VALUE = 100L;

    /**
     * A {@link CacheMap} decorator which runs {@code afterInsertion} once, after the delegate has
     * inserted {@code triggerKey} but before {@code putIfAbsent} returns {@code null}. All other
     * operations are delegated to the real cache map.
     */
    private static final class InsertionHookCacheMap implements CacheMap<Object, AtomicInteger> {

        private final CacheMap<Object, AtomicInteger> delegate;
        private final Object triggerKey;
        private final Runnable afterInsertion;
        private boolean hookArmed = true;

        InsertionHookCacheMap(CacheMap<Object, AtomicInteger> delegate, Object triggerKey,
                              Runnable afterInsertion) {
            this.delegate = delegate;
            this.triggerKey = triggerKey;
            this.afterInsertion = afterInsertion;
        }

        @Override
        public AtomicInteger putIfAbsent(Object key, AtomicInteger value) {
            AtomicInteger previous = delegate.putIfAbsent(key, value);
            if (previous == null && hookArmed && triggerKey.equals(key)) {
                // Disarm before running the hook so that its re-entrant call cannot loop.
                hookArmed = false;
                afterInsertion.run();
            }
            return previous;
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public AtomicInteger get(Object key) {
            return delegate.get(key);
        }

        @Override
        public AtomicInteger remove(Object key) {
            return delegate.remove(key);
        }

        @Override
        public AtomicInteger put(Object key, AtomicInteger value) {
            return delegate.put(key, value);
        }

        @Override
        public long size() {
            return delegate.size();
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public Set<Object> keySet(boolean ascending) {
            return delegate.keySet(ascending);
        }
    }
}
