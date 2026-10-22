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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.statistic.ParamFlowStatisticEntryCallback;
import com.alibaba.csp.sentinel.slots.statistic.ParamFlowStatisticExitCallback;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry;
import com.alibaba.csp.sentinel.slots.statistic.cache.CacheMap;

/**
 * Test cases for reloading rules of {@link ParamFlowRuleManager} when several rules of the same
 * resource share the thread counter of one parameter index.
 *
 * <p>The thread counter of a parameter index is shared by all the rules that use that index, so a
 * reload has to keep the counter as long as one of those rules is still in use. Otherwise the
 * count of the entries that are still in flight is lost and the resource is under-counted, which
 * allows more entries to pass than the configured threshold.</p>
 */
public class ParamFlowRuleManagerReloadTest {

    private static final String CONTEXT_NAME = "param-flow-rule-manager-reload-test";
    private static final int PARAM_IDX = 0;
    private static final long VALUE = 100L;

    private final List<OpenEntry> openEntries = new ArrayList<OpenEntry>();
    private List<ParamFlowRule> previousRules;
    private Map<String, ParameterMetric> previousMetrics;
    private ProcessorSlotEntryCallback<DefaultNode> addedEntryCallback;
    private ProcessorSlotExitCallback addedExitCallback;

    @Before
    public void setUp() {
        ContextUtil.exit();

        // Keep the process state of the rules and metrics so that it can be restored afterwards.
        previousRules = ParamFlowRuleManager.getRules();
        previousMetrics = new HashMap<String, ParameterMetric>(ParameterMetricStorage.getMetricsMap());
        // Detach the saved metrics before the rules are reset: the metrics of the previous rules
        // must not be modified by this test.
        ParameterMetricStorage.getMetricsMap().clear();
        ParamFlowRuleManager.loadRules(null);
        ParameterMetricStorage.getMetricsMap().clear();

        addedEntryCallback = installEntryCallbackIfAbsent();
        addedExitCallback = installExitCallbackIfAbsent();

        ContextUtil.enter(CONTEXT_NAME);
    }

    @After
    public void tearDown() {
        try {
            closeOpenEntries();
            ContextUtil.exit();
        } finally {
            try {
                ParamFlowRuleManager.loadRules(null);
                ParameterMetricStorage.getMetricsMap().clear();
                if (previousRules != null) {
                    ParamFlowRuleManager.loadRules(previousRules);
                }
                if (previousMetrics != null) {
                    ParameterMetricStorage.getMetricsMap().putAll(previousMetrics);
                }
                previousRules = null;
                previousMetrics = null;
            } finally {
                removeCallbacksIfAdded();
            }
        }
    }

    @Test
    public void testReloadDroppingSiblingKeepsSharedThreadCounter() throws Exception {
        final String resource = "paramFlowReloadKeepSharedCounter";
        ParamFlowRule survivor = threadRule(resource, 3);
        ParamFlowRule sibling = threadRule(resource, 10);
        checkSiblingRemovalKeepsSharedThreadCounter(resource, Arrays.asList(survivor, sibling), survivor,
            sibling);
    }

    @Test
    public void testReloadDroppingSiblingKeepsSharedThreadCounterInReversedOrder() throws Exception {
        final String resource = "paramFlowReloadKeepSharedCounterReversed";
        ParamFlowRule survivor = threadRule(resource, 3);
        ParamFlowRule sibling = threadRule(resource, 10);
        checkSiblingRemovalKeepsSharedThreadCounter(resource, Arrays.asList(sibling, survivor), survivor,
            sibling);
    }

    @Test
    public void testReloadWithTighterLimitKeepsInFlightCount() throws Exception {
        final String resource = "paramFlowReloadTighterLimit";
        ParamFlowRule oldCompanion = threadRule(resource, 10);
        ParamFlowRule replaced = threadRule(resource, 5);
        ParamFlowRule removed = threadRule(resource, 7);
        checkTighterReloadKeepsInFlightCount(resource, Arrays.asList(oldCompanion, replaced, removed),
            oldCompanion, replaced, removed, threadRule(resource, 2));
    }

    @Test
    public void testReloadWithTighterLimitKeepsInFlightCountInReversedOrder() throws Exception {
        final String resource = "paramFlowReloadTighterLimitReversed";
        ParamFlowRule oldCompanion = threadRule(resource, 10);
        ParamFlowRule replaced = threadRule(resource, 5);
        ParamFlowRule removed = threadRule(resource, 7);
        checkTighterReloadKeepsInFlightCount(resource, Arrays.asList(removed, replaced, oldCompanion),
            oldCompanion, replaced, removed, threadRule(resource, 2));
    }

    @Test
    public void testReloadWithValueEqualReplacementRuleKeepsSharedCounter() throws Exception {
        final String resource = "paramFlowReloadEqualReplacement";
        ParamFlowRule keeper = threadRule(resource, 10);
        ParamFlowRule sibling = threadRule(resource, 5);
        ParamFlowRuleManager.loadRules(Arrays.asList(keeper, sibling));

        Entry first = enter(resource, VALUE);
        Entry second = enter(resource, VALUE);

        ParameterMetric metric = metricOf(resource);
        assertNotNull(metric);
        CacheMap<Object, AtomicInteger> counter = threadCounterOf(metric, PARAM_IDX);
        assertNotNull(counter);
        CacheMap<Object, AtomicLong> keeperTimeCounter = metric.getRuleTimeCounter(keeper);
        assertNotNull(keeperTimeCounter);
        assertEquals(2L, metric.getThreadCount(PARAM_IDX, VALUE));

        // A rule object that is equal to an already loaded rule reuses the counters of that rule.
        ParamFlowRule replacement = threadRule(resource, 10);
        assertNotSame(keeper, replacement);
        assertEquals(keeper, replacement);

        ParamFlowRuleManager.loadRules(Collections.singletonList(replacement));

        ParameterMetric metricAfter = metricOf(resource);
        assertSame(metric, metricAfter);
        assertSame("The equal rule must reuse the counters of the loaded instance", keeperTimeCounter,
            metricAfter.getRuleTimeCounter(replacement));
        assertSame("The shared thread counter must be kept", counter, threadCounterOf(metricAfter, PARAM_IDX));
        assertEquals(2L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
        assertNull(metricAfter.getRuleTimeCounter(sibling));

        Entry third = enter(resource, VALUE);
        assertEquals(3L, metricAfter.getThreadCount(PARAM_IDX, VALUE));

        exit(third, VALUE);
        assertEquals(2L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
        exit(second, VALUE);
        assertEquals(1L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
        exit(first, VALUE);
        assertEquals(0L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
    }

    @Test
    public void testReloadRemovingRuleOfLastParamIdxKeepsOtherIndexCounter() throws Exception {
        final String resource = "paramFlowReloadParamIdxCleanup";
        final long firstValue = 100L;
        final long lastValue = 200L;
        ParamFlowRule firstIdxRule = threadRule(resource, 5);
        ParamFlowRule lastIdxRule = threadRule(resource, 5);
        lastIdxRule.setParamIdx(1);
        ParamFlowRuleManager.loadRules(Arrays.asList(firstIdxRule, lastIdxRule));

        Entry first = enter(resource, firstValue, lastValue);
        Entry second = enter(resource, firstValue, lastValue);

        ParameterMetric metric = metricOf(resource);
        assertNotNull(metric);
        CacheMap<Object, AtomicInteger> firstIdxCounter = threadCounterOf(metric, 0);
        assertNotNull(firstIdxCounter);
        assertNotNull(threadCounterOf(metric, 1));
        assertEquals(2L, metric.getThreadCount(0, firstValue));
        assertEquals(2L, metric.getThreadCount(1, lastValue));

        // Only the rule of the last parameter index is dropped.
        ParamFlowRuleManager.loadRules(Collections.singletonList(firstIdxRule));

        ParameterMetric metricAfter = metricOf(resource);
        assertSame(metric, metricAfter);
        assertSame("The counter of the index that stays in use must be kept", firstIdxCounter,
            threadCounterOf(metricAfter, 0));
        assertNull("The counter of the index that is no longer used must be removed",
            threadCounterOf(metricAfter, 1));
        assertEquals(2L, metricAfter.getThreadCount(0, firstValue));
        assertEquals(0L, metricAfter.getThreadCount(1, lastValue));

        exit(second, firstValue, lastValue);
        assertEquals(1L, metricAfter.getThreadCount(0, firstValue));
        assertEquals(0L, metricAfter.getThreadCount(1, lastValue));
        exit(first, firstValue, lastValue);
        assertEquals(0L, metricAfter.getThreadCount(0, firstValue));
        assertEquals(0L, metricAfter.getThreadCount(1, lastValue));
        assertSame(firstIdxCounter, threadCounterOf(metricAfter, 0));
    }

    @Test
    public void testReloadRemovingResourceClearsMetricAndKeepsExitsSafe() throws Exception {
        final String resource = "paramFlowReloadResourceRemoved";
        final String otherResource = "paramFlowReloadResourceOther";
        ParamFlowRule rule = threadRule(resource, 2);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));

        Entry first = enter(resource, VALUE);
        Entry second = enter(resource, VALUE);

        ParameterMetric metric = metricOf(resource);
        assertNotNull(metric);
        assertEquals(2L, metric.getThreadCount(PARAM_IDX, VALUE));

        ParamFlowRule otherRule = threadRule(otherResource, 3);
        ParamFlowRuleManager.loadRules(Collections.singletonList(otherRule));

        assertNull("The metric of the removed resource must be cleared", metricOf(resource));
        assertFalse(ParamFlowRuleManager.hasRules(resource));
        assertTrue(ParamFlowRuleManager.hasRules(otherResource));
        assertEquals("The detached metric must not be updated any more", 2L,
            metric.getThreadCount(PARAM_IDX, VALUE));

        // The in-flight entries of the removed resource exit without touching any metric.
        exit(second, VALUE);
        exit(first, VALUE);
        assertNull(metricOf(resource));
        assertEquals("The detached metric must not be updated any more", 2L,
            metric.getThreadCount(PARAM_IDX, VALUE));

        // Without rules the resource is not controlled and no metric is created again.
        Entry entry = enter(resource, VALUE);
        assertNull(metricOf(resource));
        exit(entry, VALUE);
        assertNull(metricOf(resource));
    }

    @Test
    public void testReloadWithoutInFlightEntriesKeepsNoPhantomCount() throws Exception {
        final String resource = "paramFlowReloadNoInFlight";
        ParamFlowRule keeper = threadRule(resource, 2);
        ParamFlowRule sibling = threadRule(resource, 9);
        ParamFlowRuleManager.loadRules(Arrays.asList(keeper, sibling));
        assertNull("Loading rules must not create the parameter metric", metricOf(resource));

        ParamFlowRuleManager.loadRules(Collections.singletonList(keeper));
        assertNull(metricOf(resource));
        assertEquals(1, ParamFlowRuleManager.getRulesOfResource(resource).size());

        Entry entry = enter(resource, VALUE);
        ParameterMetric metric = metricOf(resource);
        assertNotNull(metric);
        assertEquals(1L, metric.getThreadCount(PARAM_IDX, VALUE));

        exit(entry, VALUE);
        assertEquals(0L, metric.getThreadCount(PARAM_IDX, VALUE));
        CacheMap<Object, AtomicInteger> counter = threadCounterOf(metric, PARAM_IDX);
        assertNotNull(counter);
        assertEquals("The count of the value must be released on exit", 0, counter.size());
    }

    /**
     * Both rules of the resource share the thread counter of the parameter index. Dropping the
     * sibling must neither reset the counter nor replace the holder of the count of the entries
     * that are still in flight.
     */
    private void checkSiblingRemovalKeepsSharedThreadCounter(String resource, List<ParamFlowRule> initialRules,
                                                             ParamFlowRule survivor, ParamFlowRule sibling)
        throws Exception {
        ParamFlowRuleManager.loadRules(initialRules);

        Entry first = enter(resource, VALUE);
        Entry second = enter(resource, VALUE);

        ParameterMetric metric = metricOf(resource);
        assertNotNull(metric);
        CacheMap<Object, AtomicInteger> counter = threadCounterOf(metric, PARAM_IDX);
        assertNotNull(counter);
        AtomicInteger valueCounter = counter.get(VALUE);
        assertNotNull(valueCounter);
        assertEquals(2, valueCounter.get());
        assertEquals(2L, metric.getThreadCount(PARAM_IDX, VALUE));

        ParamFlowRuleManager.loadRules(Collections.singletonList(survivor));

        ParameterMetric metricAfter = metricOf(resource);
        assertSame("The reload must keep the metric of the surviving rule", metric, metricAfter);
        CacheMap<Object, AtomicInteger> counterAfter = threadCounterOf(metricAfter, PARAM_IDX);
        assertSame("The thread counter of the parameter index must be kept", counter, counterAfter);
        assertSame("The holder of the in-flight count must not be replaced", valueCounter,
            counterAfter.get(VALUE));
        assertEquals("The in-flight count must be retained", 2L,
            metricAfter.getThreadCount(PARAM_IDX, VALUE));
        assertNotNull(metricAfter.getRuleTimeCounter(survivor));
        assertNull("The removed sibling must release its own counters", metricAfter.getRuleTimeCounter(sibling));

        Entry third = enter(resource, VALUE);
        assertEquals("The limit of the surviving rule applies to the retained count", 3L,
            metricAfter.getThreadCount(PARAM_IDX, VALUE));

        exit(third, VALUE);
        assertEquals(2L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
        exit(second, VALUE);
        assertEquals(1L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
        exit(first, VALUE);
        assertEquals(0L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
        assertSame("Exiting must not detach the shared counter", counter,
            threadCounterOf(metricAfter, PARAM_IDX));
    }

    /**
     * Reloading the resource with the single tighter rule drops every old rule instance of the
     * parameter index by value equality: the companion rule, the replaced one and the removed one
     * are all gone, while the new rule still loads the same parameter index and stays lazily
     * initialized until its first entry. The in-flight count of the value must survive this
     * reload, so the next entry for the same value is blocked by the new limit although the new
     * rule does not hold the retained entries itself.
     */
    private void checkTighterReloadKeepsInFlightCount(String resource, List<ParamFlowRule> initialRules,
                                                      ParamFlowRule oldCompanion, ParamFlowRule replaced,
                                                      ParamFlowRule removed, ParamFlowRule tighter)
        throws Exception {
        ParamFlowRuleManager.loadRules(initialRules);

        Entry first = enter(resource, VALUE);
        Entry second = enter(resource, VALUE);

        ParameterMetric metric = metricOf(resource);
        assertNotNull(metric);
        CacheMap<Object, AtomicInteger> counter = threadCounterOf(metric, PARAM_IDX);
        assertNotNull(counter);
        AtomicInteger valueCounter = counter.get(VALUE);
        assertNotNull(valueCounter);
        assertEquals(2, valueCounter.get());
        // All the rules of the resource rely on the same counter of the parameter index.
        assertNotNull(metric.getRuleTimeCounter(oldCompanion));
        assertNotNull(metric.getRuleTimeCounter(replaced));
        assertNotNull(metric.getRuleTimeCounter(removed));

        // No old rule value survives this reload: the only rule that keeps the parameter index of
        // the resource loaded is the new one, which is not initialized before its first entry.
        ParamFlowRuleManager.loadRules(Collections.singletonList(tighter));

        assertExtraEntryBlocked(resource, VALUE);
        ParameterMetric metricAfter = metricOf(resource);
        assertSame(metric, metricAfter);
        CacheMap<Object, AtomicInteger> counterAfter = threadCounterOf(metricAfter, PARAM_IDX);
        assertSame("The reload must keep the shared thread counter of the parameter index", counter,
            counterAfter);
        assertSame(valueCounter, counterAfter.get(VALUE));
        assertEquals("The in-flight count must survive the reload", 2L,
            metricAfter.getThreadCount(PARAM_IDX, VALUE));
        assertNull("The replaced rule must release its own counters", metricAfter.getRuleTimeCounter(replaced));
        assertNull("The removed rule must release its own counters", metricAfter.getRuleTimeCounter(removed));
        assertNotNull("The checked rule is initialized by the blocked entry", metricAfter.getRuleTimeCounter(tighter));

        assertEquals("A blocked entry must not change the in-flight count", 2L,
            metricAfter.getThreadCount(PARAM_IDX, VALUE));

        exit(second, VALUE);
        assertEquals(1L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
        exit(first, VALUE);
        assertEquals(0L, metricAfter.getThreadCount(PARAM_IDX, VALUE));
    }

    private static ParamFlowRule threadRule(String resource, int count) {
        ParamFlowRule rule = new ParamFlowRule(resource);
        rule.setCount(count);
        rule.setParamIdx(PARAM_IDX);
        rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        return rule;
    }

    private static ParameterMetric metricOf(String resource) {
        return ParameterMetricStorage.getParamMetricForResource(resource);
    }

    private static CacheMap<Object, AtomicInteger> threadCounterOf(ParameterMetric metric, int paramIdx) {
        return metric.getThreadCountMap().get(paramIdx);
    }

    private Entry enter(String resource, Object... args) throws BlockException {
        Entry entry = SphU.entry(resource, EntryType.IN, 1, args);
        openEntries.add(new OpenEntry(entry, args));
        return entry;
    }

    private void exit(Entry entry, Object... args) throws Exception {
        forgetOpenEntry(entry);
        entry.exit(1, args);
    }

    private void forgetOpenEntry(Entry entry) {
        for (int i = 0; i < openEntries.size(); i++) {
            if (openEntries.get(i).entry == entry) {
                openEntries.remove(i);
                return;
            }
        }
    }

    private void closeOpenEntries() {
        for (int i = openEntries.size() - 1; i >= 0; i--) {
            OpenEntry openEntry = openEntries.get(i);
            try {
                openEntry.entry.exit(1, openEntry.args);
            } catch (Throwable ignored) {
                // Ignore: the cleanup of a failed test must not hide the assertion error.
            }
        }
        openEntries.clear();
    }

    private static void assertExtraEntryBlocked(String resource, Object value) throws Exception {
        try {
            Entry entry = SphU.entry(resource, EntryType.IN, 1, value);
            entry.exit(1, value);
            fail("The extra entry should be blocked by the parameter flow rule");
        } catch (BlockException ex) {
            assertTrue("Expect a ParamFlowException but was " + ex.getClass().getName(),
                ex instanceof ParamFlowException);
            assertEquals(resource, ((ParamFlowException) ex).getResourceName());
        }
    }

    private static ProcessorSlotEntryCallback<DefaultNode> installEntryCallbackIfAbsent() {
        for (ProcessorSlotEntryCallback<DefaultNode> registered : StatisticSlotCallbackRegistry
            .getEntryCallbacks()) {
            if (registered instanceof ParamFlowStatisticEntryCallback) {
                return null;
            }
        }
        ProcessorSlotEntryCallback<DefaultNode> callback = new ParamFlowStatisticEntryCallback();
        StatisticSlotCallbackRegistry.addEntryCallback(ParamFlowStatisticEntryCallback.class.getName(), callback);
        return callback;
    }

    private static ProcessorSlotExitCallback installExitCallbackIfAbsent() {
        for (ProcessorSlotExitCallback registered : StatisticSlotCallbackRegistry.getExitCallbacks()) {
            if (registered instanceof ParamFlowStatisticExitCallback) {
                return null;
            }
        }
        ProcessorSlotExitCallback callback = new ParamFlowStatisticExitCallback();
        StatisticSlotCallbackRegistry.addExitCallback(ParamFlowStatisticExitCallback.class.getName(), callback);
        return callback;
    }

    private void removeCallbacksIfAdded() {
        if (addedEntryCallback != null) {
            // Only unregister the instance installed by this test.
            if (containsIdentity(StatisticSlotCallbackRegistry.getEntryCallbacks(), addedEntryCallback)) {
                StatisticSlotCallbackRegistry.removeEntryCallback(ParamFlowStatisticEntryCallback.class.getName());
            }
            addedEntryCallback = null;
        }
        if (addedExitCallback != null) {
            if (containsIdentity(StatisticSlotCallbackRegistry.getExitCallbacks(), addedExitCallback)) {
                StatisticSlotCallbackRegistry.removeExitCallback(ParamFlowStatisticExitCallback.class.getName());
            }
            addedExitCallback = null;
        }
    }

    private static boolean containsIdentity(Collection<?> registered, Object callback) {
        for (Object item : registered) {
            if (item == callback) {
                return true;
            }
        }
        return false;
    }

    private static final class OpenEntry {

        private final Entry entry;
        private final Object[] args;

        private OpenEntry(Entry entry, Object[] args) {
            this.entry = entry;
            this.args = args;
        }
    }
}
