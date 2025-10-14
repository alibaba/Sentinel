package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AdaptiveDegradeSlot}.
 *
 * @author ylnxwlp
 */
@RunWith(MockitoJUnitRunner.class)
public class AdaptiveDegradeSlotTest {

    private AdaptiveDegradeSlot slot;

    @Mock
    private Context context;

    @Mock
    private ResourceWrapper resourceWrapper;

    @Mock
    private Entry entry;

    @Before
    public void setUp() {
        slot = new AdaptiveDegradeSlot();
        when(context.getCurEntry()).thenReturn(entry);
        when(resourceWrapper.getName()).thenReturn("test");
    }

    @Test
    public void testEntryRuleDisabled() {
        AdaptiveDegradeRule rule = new AdaptiveDegradeRule("test");
        rule.setEnabled(false);
        try (MockedStatic<AdaptiveDegradeRuleManager> mockedRuleManager = mockStatic(AdaptiveDegradeRuleManager.class)) {
            mockedRuleManager.when(() -> AdaptiveDegradeRuleManager.getRule("test")).thenReturn(rule);
            try {
                slot.performChecking(context, resourceWrapper);
            } catch (BlockException e) {
                fail("Should not throw exception when rule is disabled");
            }
        }
    }

    @Test
    public void testEntryResourceTypeIN() {
        AdaptiveDegradeRule rule = new AdaptiveDegradeRule("test");
        rule.setEnabled(true);
        try (MockedStatic<AdaptiveDegradeRuleManager> mockedRuleManager = mockStatic(AdaptiveDegradeRuleManager.class)) {
            mockedRuleManager.when(() -> AdaptiveDegradeRuleManager.getRule("test")).thenReturn(rule);
            when(resourceWrapper.getEntryType()).thenReturn(EntryType.IN);
            try {
                slot.performChecking(context, resourceWrapper);
            } catch (BlockException e) {
                fail("Should not throw exception for EntryType.IN");
            }
        }
    }

    @Test
    public void testEntryPass() {
        AdaptiveDegradeRule rule = new AdaptiveDegradeRule("test");
        rule.setEnabled(true);
        try (MockedStatic<AdaptiveDegradeRuleManager> mockedRuleManager = mockStatic(AdaptiveDegradeRuleManager.class);
             MockedStatic<AdaptiveCircuitBreakerManager> mockedCBManager = mockStatic(AdaptiveCircuitBreakerManager.class)) {
            mockedRuleManager.when(() -> AdaptiveDegradeRuleManager.getRule("test")).thenReturn(rule);
            when(resourceWrapper.getEntryType()).thenReturn(EntryType.OUT);
            AdaptiveCircuitBreaker circuitBreaker = mock(AdaptiveCircuitBreaker.class);
            when(circuitBreaker.tryPass(context)).thenReturn(true);
            mockedCBManager.when(() -> AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker("test"))
                    .thenReturn(circuitBreaker);
            try {
                slot.performChecking(context, resourceWrapper);
            } catch (BlockException e) {
                fail("Should not throw exception when circuit breaker allows pass");
            }
        }
    }

    @Test
    public void testEntryBlocked() {
        AdaptiveDegradeRule rule = new AdaptiveDegradeRule("test");
        rule.setEnabled(true);
        try (MockedStatic<AdaptiveDegradeRuleManager> mockedRuleManager = mockStatic(AdaptiveDegradeRuleManager.class);
             MockedStatic<AdaptiveCircuitBreakerManager> mockedCBManager = mockStatic(AdaptiveCircuitBreakerManager.class)) {
            mockedRuleManager.when(() -> AdaptiveDegradeRuleManager.getRule("test")).thenReturn(rule);
            when(resourceWrapper.getEntryType()).thenReturn(EntryType.OUT);
            AdaptiveCircuitBreaker circuitBreaker = mock(AdaptiveCircuitBreaker.class);
            when(circuitBreaker.tryPass(context)).thenReturn(false);
            when(circuitBreaker.getScenario()).thenReturn("System is overloaded");
            when(circuitBreaker.getProbability()).thenReturn(0.05);
            when(circuitBreaker.currentState()).thenReturn(AdaptiveCircuitBreaker.State.THROTTLING);
            mockedCBManager.when(() -> AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker("test"))
                    .thenReturn(circuitBreaker);
            try {
                slot.performChecking(context, resourceWrapper);
                fail("Should throw DegradeException when circuit breaker blocks");
            } catch (AdaptiveDegradeException e) {
                assertNotNull(e.getMessage());
                assertTrue(e.getMessage().contains("0.05"));
                assertTrue(e.getMessage().contains("System is overloaded"));
                assertTrue(e.getMessage().contains("THROTTLING"));
            } catch (BlockException e) {
                fail("Should throw DegradeException, not BlockException");
            }
        }
    }

    @Test
    public void testExitRequestBlocked() {
        when(entry.getBlockError()).thenReturn(mock(BlockException.class));
        try (MockedStatic<AdaptiveCircuitBreakerManager> mockedCBManager = mockStatic(AdaptiveCircuitBreakerManager.class)) {
            AdaptiveCircuitBreaker circuitBreaker = mock(AdaptiveCircuitBreaker.class);
            mockedCBManager.when(() -> AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker("test"))
                    .thenReturn(circuitBreaker);
            slot.exit(context, resourceWrapper, 1);
            verify(circuitBreaker, never()).onRequestComplete(any(Context.class));
        }
    }

    @Test
    public void testExitRequestNotBlocked() {
        when(entry.getBlockError()).thenReturn(null);
        try (MockedStatic<AdaptiveCircuitBreakerManager> mockedCBManager = mockStatic(AdaptiveCircuitBreakerManager.class)) {
            AdaptiveCircuitBreaker circuitBreaker = mock(AdaptiveCircuitBreaker.class);
            mockedCBManager.when(() -> AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker("test"))
                    .thenReturn(circuitBreaker);
            slot.exit(context, resourceWrapper, 1);
            verify(circuitBreaker, times(1)).onRequestComplete(context);
        }
    }

    @Test
    public void testExitNoCircuitBreaker() {
        when(entry.getBlockError()).thenReturn(null);
        try (MockedStatic<AdaptiveCircuitBreakerManager> mockedCBManager = mockStatic(AdaptiveCircuitBreakerManager.class)) {
            mockedCBManager.when(() -> AdaptiveCircuitBreakerManager.getAdaptiveCircuitBreaker("test"))
                    .thenReturn(null);
            try {
                slot.exit(context, resourceWrapper, 1);
            } catch (Exception e) {
                fail("Should not throw exception when no circuit breaker");
            }
        }
    }

    @Test
    public void testPerformCheckingNoRule() {
        try (MockedStatic<AdaptiveDegradeRuleManager> mockedRuleManager = mockStatic(AdaptiveDegradeRuleManager.class)) {
            mockedRuleManager.when(() -> AdaptiveDegradeRuleManager.getRule("test")).thenReturn(null);
            try {
                slot.performChecking(context, resourceWrapper);
                fail("Should throw NullPointerException when rule is null");
            } catch (NullPointerException e) {
                // Expect
            } catch (BlockException e) {
                fail("Should throw NullPointerException, not BlockException");
            }
        }
    }
}