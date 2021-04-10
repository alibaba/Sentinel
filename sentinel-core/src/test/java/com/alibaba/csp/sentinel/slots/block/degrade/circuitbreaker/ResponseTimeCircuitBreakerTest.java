package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
        assertFalse(entryAndSleepFor(resource,20));
        sleep(1000);
        assertFalse(entryAndSleepFor(resource,20));
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
     * If maxAllowedRT=5000, and a entry created at 980 exits at 6050,
     * then we can judge that all inflight request can be treated as slow request.
     */
    @Test
    public void testDegradeByOneSlowRT() {
        String resource = "testDegradeByOneSlowRT";
        DegradeRule rule = new DegradeRule(resource)
                .setCount(5000)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(1000)
                .setTimeWindow(1);

        AtomicReference<State> prevStateRef = new AtomicReference<>();
        AtomicReference<State> newStateRef = new AtomicReference<>();
        AtomicReference<Double> snapshotValueRef = new AtomicReference<>();
        EventObserverRegistry.getInstance().addStateChangeObserver(resource, new CircuitBreakerStateChangeObserver() {
            @Override
            public void onStateChange(State prevState, State newState, DegradeRule rule, Double snapshotValue) {
                prevStateRef.set(prevState);
                newStateRef.set(newState);
                snapshotValueRef.set(snapshotValue);
            }
        });
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        List<Entry> entryList = new ArrayList<>(20);
        /*
         * If maxAllowedRT=5000, and a entry created at 980 exits at 6050,
         * then we can judge that all inflight request can be treated as slow request.
         */
        assertTrue(entryAndSleepFor(resource, 0));
        sleep(800);
        // Entry: 0 --- 800,810,820...990  Exited: 0
        batchEntryEvenly(resource, 20, 10, entryList);
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
        // Entry 810 slow exits at 5850, that`s timeout but will not trigger breaker.
        safeExit(entryList.get(1));
        assertTrue(entryAndSleepFor(resource, 0));
        // Entry 980 exits at 5850, that`s not timeout and will not trigger breaker.
        safeExit(entryList.get(18));
        assertTrue(entryAndSleepFor(resource, 0));
        sleep(200);
        // Entry 970 slow exits at 6050, will trigger breaker.
        safeExit(entryList.get(17));
        assertSame(prevStateRef.get(), State.CLOSED);
        assertSame(newStateRef.get(), State.OPEN);
        // Entry 0,800,900,980 are not slow RT
        assertEquals(0, Double.compare(snapshotValueRef.get(), 1.0d * 17 / 21));

        EventObserverRegistry.getInstance().removeStateChangeObserver(resource);
        batchExitImmediately(entryList);
    }

    /**
     * If maxAllowedRT=5000, and entries created at range [0-1000) not exit at 6000,
     * then we can judge that all entries can be treated as slow request.
     */
    @Test
    public void testDegradeBySuccessorEntry() {
        String resource = "testDegradeByEntry";
        DegradeRule rule = new DegradeRule(resource)
                .setCount(5000)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(1000)
                .setTimeWindow(1);

        AtomicReference<State> prevStateRef = new AtomicReference<>();
        AtomicReference<State> newStateRef = new AtomicReference<>();
        AtomicReference<Double> snapshotValueRef = new AtomicReference<>();
        EventObserverRegistry.getInstance().addStateChangeObserver(resource, new CircuitBreakerStateChangeObserver() {
            @Override
            public void onStateChange(State prevState, State newState, DegradeRule rule, Double snapshotValue) {
                prevStateRef.set(prevState);
                newStateRef.set(newState);
                snapshotValueRef.set(snapshotValue);
            }
        });
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        List<Entry> entryList = new ArrayList<>(20);
        /*
         * If maxAllowedRT=5000, and entries created at range [0-1000) not exit at 6000,
         * then we can judge that all entries can be treated as slow request.
         */
        // Entry: 0,50,100...900,950
        batchEntryEvenly(resource, 20, 50, entryList);
        assertTrue(noOneBlocked(entryList));

        // Entry: 0,50,100...900,950 --- 4000,4100..5400  Exited: 5000,5100..5400
        sleep(3000);
        List<Entry> anotherEntryList = new ArrayList<>(15);
        batchEntryEvenly(resource, 15, 100, anotherEntryList);
        assertTrue(noOneBlocked(entryList));
        batchExitImmediately(anotherEntryList);

        // Entry: 0,50,100...900,950 --- 4000,4100..5400 --- 5500,5550...6500
        List<Entry> mayBlockedEntryList = new ArrayList<>(20);
        batchEntryEvenly(resource, 20, 50, mayBlockedEntryList);
        assertFalse(noOneBlocked(mayBlockedEntryList));
        // Entry is null means blocked. Entry 5950 will not blocked, while entry 6000 will blocked.
        assertNotNull(mayBlockedEntryList.get(9));
        assertNull(mayBlockedEntryList.get(10));
        assertSame(prevStateRef.get(), State.CLOSED);
        assertSame(newStateRef.get(), State.OPEN);
        assertEquals(0, Double.compare(snapshotValueRef.get(), 1.0d));

        EventObserverRegistry.getInstance().removeStateChangeObserver(resource);
        batchExitImmediately(entryList);
        batchExitImmediately(mayBlockedEntryList);
    }

    /**
     * If maxAllowedRT=5000, and entries created at range [0-1000) not exit at 8000,
     * then we can judge that all entries can be treated as slow request.
     */
    @Test
    public void testDegradeBySkipEntry() {
        String resource = "testDegradeByEntry";
        DegradeRule rule = new DegradeRule(resource)
                .setCount(5000)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.5)
                .setStatIntervalMs(1000)
                .setTimeWindow(1);

        AtomicReference<State> prevStateRef = new AtomicReference<>();
        AtomicReference<State> newStateRef = new AtomicReference<>();
        AtomicReference<Double> snapshotValueRef = new AtomicReference<>();
        EventObserverRegistry.getInstance().addStateChangeObserver(resource, new CircuitBreakerStateChangeObserver() {
            @Override
            public void onStateChange(State prevState, State newState, DegradeRule rule, Double snapshotValue) {
                prevStateRef.set(prevState);
                newStateRef.set(newState);
                snapshotValueRef.set(snapshotValue);
            }
        });
        DegradeRuleManager.loadRules(Collections.singletonList(rule));

        List<Entry> entryList = new ArrayList<>(20);
        /*
         * If maxAllowedRT=5000, and entries created at range [0-1000) not exit at 8000,
         * then we can judge that all entries can be treated as slow request.
         */
        // Entry: 0,50,100...900,950
        batchEntryEvenly(resource, 20, 50, entryList);
        assertTrue(noOneBlocked(entryList));

        sleep(7000);
        assertFalse(entryAndSleepFor(resource, 0));
        assertSame(prevStateRef.get(), State.CLOSED);
        assertSame(newStateRef.get(), State.OPEN);
        assertEquals(0, Double.compare(snapshotValueRef.get(), 1.0d));

        EventObserverRegistry.getInstance().removeStateChangeObserver(resource);
        batchExitImmediately(entryList);
    }
}
