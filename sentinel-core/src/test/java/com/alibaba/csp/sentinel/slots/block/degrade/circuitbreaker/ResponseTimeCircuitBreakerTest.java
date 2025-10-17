package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;

/**
 * @author xierz
 * @date 2020/10/4
 */
public class ResponseTimeCircuitBreakerTest extends AbstractTimeBasedTest {
    @Before
    public void setUp() {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @After
    public void tearDown() throws Exception {
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @Test
    public void testMaxSlowRatioThreshold() {
        String resource = "testMaxSlowRatioThreshold";
        DegradeRule rule = new DegradeRule("resource")
                .setCount(10)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(1)
                .setStatIntervalMs(5000)
                .setTimeWindow(5);
        rule.setResource(resource);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        assertTrue(entryAndSleepFor(resource, 20));
        assertTrue(entryAndSleepFor(resource, 20));
        assertTrue(entryAndSleepFor(resource, 20));

        // should be blocked, cause 3/3 requests' rt is bigger than max rt.
        assertFalse(entryAndSleepFor(resource, 20));
        sleep(1000);
        assertFalse(entryAndSleepFor(resource, 20));
        sleep(4000);
        // HALF_OPEN to OPEN
        assertTrue(entryAndSleepFor(resource, 20));
        assertFalse(entryAndSleepFor(resource, 20));
        // HALF_OPEN to CLOSE
        sleep(5000);
        assertTrue(entryAndSleepFor(resource, 10));
        assertTrue(entryAndSleepFor(resource, 10));
    }

    /**
     * If maxAllowedRT=5000, statIntervalMs=1000 and a entry created at 970 exits at 6050,
     * then we can judge that all inflight requests created at range [0-1000) can be treated as slow request.
     */
    @Test
    public void testDegradeByOneSlowRT() {
        CircuitBreakerStateChangeObserver observer = mock(CircuitBreakerStateChangeObserver.class);
        String resource = "testDegradeByOneSlowRT";
        DegradeRule rule = new DegradeRule(resource)
                .setCount(5000)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(1000)
                .setTimeWindow(1);

        EventObserverRegistry.getInstance().addStateChangeObserver(resource, observer);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        List<Entry> entryList = new ArrayList<>(20);
        /*
         * If maxAllowedRT=5000, statIntervalMs=1000 and a entry created at 970 exits at 6050,
         * then we can judge that all inflight requests created at range [0-1000) can be treated as slow request.
         */
        assertTrue(entryAndSleepFor(resource, 0));
        sleep(800);
        // Entry: 0 --- 800,810,820...990  Exited: 0
        batchEntryPeriodically(resource, 20, 10, entryList);
        assertTrue(noOneBlocked(entryList));
        assertTrue(entryAndSleepFor(resource, 0));

        // Entry: 0 --- 800,810,820...990 2000,3000,4000  Exited: 0,800,990,2000,3000,4000
        sleep(1000);
        assertTrue(entryAndSleepFor(resource, 1000));
        safeExit(entryList.get(0));
        assertTrue(entryAndSleepFor(resource, 1000));
        safeExit(entryList.get(19));
        assertTrue(entryAndSleepFor(resource, 1000));

        sleep(850);
        assertTrue(entryAndSleepFor(resource, 0));
        // Entry created at 810 exits at 5850, that`s timeout but will not trigger breaker.
        safeExit(entryList.get(1));
        assertTrue(entryAndSleepFor(resource, 0));
        // Entry created at 980 exits at 5850, that`s not timeout and will not trigger breaker.
        safeExit(entryList.get(18));
        assertTrue(entryAndSleepFor(resource, 0));
        sleep(200);
        // Entry created at 970 exits at 6050, will trigger breaker.
        safeExit(entryList.get(17));
        // Entry 0,800,900,980 are not slow RT
        verify(observer)
                .onStateChange(eq(State.CLOSED), eq(State.OPEN), any(DegradeRule.class), eq(1.0d * 17 / 21));

        EventObserverRegistry.getInstance().removeStateChangeObserver(resource);
        batchExitImmediately(entryList);
    }

    /**
     * If maxAllowedRT=5000, statIntervalMs=1000 and entries created at range [0-1000) not exit at 6000,
     * then we can judge that all inflight requests can be treated as slow request.
     */
    @Test
    public void testDegradeBySuccessorEntry() {
        CircuitBreakerStateChangeObserver observer = mock(CircuitBreakerStateChangeObserver.class);
        String resource = "testDegradeBySuccessorEntry";
        DegradeRule rule = new DegradeRule(resource)
                .setCount(5000)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(1000)
                .setTimeWindow(1);

        EventObserverRegistry.getInstance().addStateChangeObserver(resource, observer);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        List<Entry> entryList = new ArrayList<>(20);
        /*
         * If maxAllowedRT=5000, and entries created at range [0-1000) not exit at 6000,
         * then we can judge that all entries can be treated as slow request.
         */
        // Entry: 0,50,100...900,950
        batchEntryPeriodically(resource, 20, 50, entryList);
        assertTrue(noOneBlocked(entryList));

        // Entry: 0,50,100...900,950 --- 4000,4100...5400  Exited: 4000,4100...5400
        sleep(3000);
        List<Entry> anotherEntryList = new ArrayList<>(15);
        batchEntryPeriodically(resource, 15, 100, anotherEntryList);
        assertTrue(noOneBlocked(entryList));
        batchExitImmediately(anotherEntryList);

        // Entry: 0,50,100...900,950 --- 4000,4100..5400 --- 5500,5550...6500
        List<Entry> mayBlockedEntryList = new ArrayList<>(20);
        batchEntryPeriodically(resource, 20, 50, mayBlockedEntryList);
        assertFalse(noOneBlocked(mayBlockedEntryList));
        // Entry is null means blocked. Entry created at 5950 will not blocked
        // while entry created at 6000 will blocked.
        assertNotNull(mayBlockedEntryList.get(9));
        assertNull(mayBlockedEntryList.get(10));
        verify(observer)
                .onStateChange(eq(State.CLOSED), eq(State.OPEN), any(DegradeRule.class), eq(1.0d));

        EventObserverRegistry.getInstance().removeStateChangeObserver(resource);
        batchExitImmediately(entryList);
        batchExitImmediately(mayBlockedEntryList);
    }

    /**
     * If maxAllowedRT=5000, statIntervalMs=1000 and entries created at range [0-1000) not exit at 8000,
     * then we can judge that all inflight requests can be treated as slow request.
     */
    @Test
    public void testDegradeBySkipEntry() {
        CircuitBreakerStateChangeObserver observer = mock(CircuitBreakerStateChangeObserver.class);
        String resource = "testDegradeBySkipEntry";
        DegradeRule rule = new DegradeRule(resource)
                .setCount(5000)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(1000)
                .setTimeWindow(1);

        EventObserverRegistry.getInstance().addStateChangeObserver(resource, observer);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        List<Entry> entryList = new ArrayList<>(20);
        /*
         * If maxAllowedRT=5000, and entries created at range [0-1000) not exit at 8000,
         * then we can judge that all entries can be treated as slow request.
         */
        // Entry: 0,50,100...900,950
        batchEntryPeriodically(resource, 20, 50, entryList);
        assertTrue(noOneBlocked(entryList));

        sleep(7000);
        // Entry created at 8000 will check previous buckets
        assertFalse(entryAndSleepFor(resource, 0));
        verify(observer)
                .onStateChange(eq(State.CLOSED), eq(State.OPEN), any(DegradeRule.class), eq(1.0d));

        EventObserverRegistry.getInstance().removeStateChangeObserver(resource);
        batchExitImmediately(entryList);
    }

    /**
     * If maxAllowedRT=50, statIntervalMs=1000 and maxSlowRequestRatio=0.5.
     */
    @Test
    public void testDegradeBySimplyReachRatio() {
        CircuitBreakerStateChangeObserver observer = mock(CircuitBreakerStateChangeObserver.class);
        String resource = "testDegradeBySimplyReachRatio";
        DegradeRule rule = new DegradeRule(resource)
                .setCount(50)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(1000)
                .setTimeWindow(1);

        EventObserverRegistry.getInstance().addStateChangeObserver(resource, observer);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        List<Entry> entryList = new ArrayList<>(20);
        /*
         * If maxAllowedRT=50, statIntervalMs=1000 and maxSlowRequestRatio=0.5
         */
        // Entry: 0,50,100...900,950
        batchEntryPeriodically(resource, 20, 50, entryList);
        assertTrue(noOneBlocked(entryList));

        // Entry: 0,50,100...900,950  Exited: 0,50,100...450
        batchExitImmediately(entryList.subList(0, 10));
        verifyZeroInteractions(observer);
        // Entry created at 500 exits at 1000, slowRatio=11/20 which is bigger than maxSlowRequestRatio=0.5
        safeExit(entryList.get(10));
        verify(observer)
                .onStateChange(eq(State.CLOSED), eq(State.OPEN), any(DegradeRule.class), eq(1.0d * 11 / 20));

        EventObserverRegistry.getInstance().removeStateChangeObserver(resource);
        batchExitImmediately(entryList);
    }

    /**
     * If maxAllowedRT=5000, statIntervalMs=1000
     */
    @Test
    public void testDegradeAfterPrevCheck() {
        CircuitBreakerStateChangeObserver observer = mock(CircuitBreakerStateChangeObserver.class);
        String resource = "testDegradeAfterPrevCheck";
        DegradeRule rule = new DegradeRule(resource)
                .setCount(5000)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(1000)
                .setTimeWindow(1);

        EventObserverRegistry.getInstance().addStateChangeObserver(resource, observer);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        /*
         * If maxAllowedRT=50, statIntervalMs=1000 and maxSlowRequestRatio=0.5
         */
        List<Entry> entryList = new ArrayList<>(20);
        for (int i = 0; i < 5; i++) {
            // Entry: 0,250,500...4500,4750  Exited: all
            batchEntryPeriodically(resource, 4, 250, entryList);
            assertTrue(noOneBlocked(entryList));
            batchExitImmediately(entryList.subList(4 * i, 4 * (i + 1)));
        }
        sleep(7000);
        // Entry created at 12000 should check all previous buckets.
        assertTrue(entryAndSleepFor(resource, 1000));

        entryList.clear();
        // Entry: 13000,13050,130100...130950
        batchEntryPeriodically(resource, 20, 50, entryList);
        assertTrue(noOneBlocked(entryList));
        sleep(7000);
        // Entry created at 21000 should check all previous buckets.
        assertFalse(entryAndSleepFor(resource, 0));
        verify(observer)
                .onStateChange(eq(State.CLOSED), eq(State.OPEN), any(DegradeRule.class), eq(1.0d));

        sleep(1000);
        entryList.clear();
        batchEntryPeriodically(resource, 1, 0, entryList);
        assertTrue(noOneBlocked(entryList));
        verify(observer)
                .onStateChange(eq(State.OPEN), eq(State.HALF_OPEN), any(DegradeRule.class), isNull());
        batchExitImmediately(entryList);
        verify(observer)
                .onStateChange(eq(State.HALF_OPEN), eq(State.CLOSED), any(DegradeRule.class), isNull());


        EventObserverRegistry.getInstance().removeStateChangeObserver(resource);
        batchExitImmediately(entryList);
    }
}
