package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AdaptiveCircuitBreaker}.
 *
 * @author ylnxwlp
 */
@RunWith(MockitoJUnitRunner.class)
public class AdaptiveCircuitBreakerTest {

    @Mock
    private Context context;

    @Mock
    private Entry entry;

    @Before
    public void setUp() {
        when(context.getCurEntry()).thenReturn(entry);
        when(entry.getError()).thenReturn(null);
        when(entry.getCreateTimestamp()).thenReturn(1_000_000L);
        when(entry.getCompleteTimestamp()).thenReturn(1_000_050L);
    }

    private static long[] aggregateCounters(AdaptiveCircuitBreaker cb) {
        long total = 0L, error = 0L, rt = 0L;
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = cb.getWindows();
        assertNotNull("windows should not be null", windows);
        for (WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> w : windows) {
            if (w == null || w.value() == null) continue;
            AdaptiveCircuitBreaker.AdaptiveCounter c = w.value();
            total += sum(c.getTotalCount());
            error += sum(c.getErrorCount());
            rt += sum(c.getOverallRTTime());
        }
        return new long[]{total, error, rt};
    }

    private static long sum(LongAdder adder) {
        return adder == null ? 0L : adder.sum();
    }

    @Test
    public void tryPass_whenClosed_shouldReturnTrue() {
        AdaptiveCircuitBreaker cb = new AdaptiveCircuitBreaker("res-closed");
        boolean pass = cb.tryPass(context);
        assertTrue("CLOSED state should always pass", pass);
    }

    @Test
    public void onRequestComplete_shouldAccumulateSuccessAndErrorAndRT() {
        AdaptiveCircuitBreaker cb = new AdaptiveCircuitBreaker("res-count");
        cb.onRequestComplete(context);
        long[] agg1 = aggregateCounters(cb);
        assertEquals("total after 1st success", 1L, agg1[0]);
        assertEquals("error after 1st success", 0L, agg1[1]);
        assertEquals("rt sum after 1st success", 50L, agg1[2]);
        when(entry.getError()).thenReturn(new RuntimeException("boom"));
        when(entry.getCreateTimestamp()).thenReturn(2_000_000L);
        when(entry.getCompleteTimestamp()).thenReturn(2_000_020L);
        cb.onRequestComplete(context);
        long[] agg2 = aggregateCounters(cb);
        assertEquals("total after 2 calls", 2L, agg2[0]);
        assertEquals("error after 2 calls", 1L, agg2[1]);
        assertEquals("rt after 2 calls (50 + 20)", 70L, agg2[2]);
    }

    @Test
    public void resetStat_shouldClearCurrentBucketOnly() {
        AdaptiveCircuitBreaker cb = new AdaptiveCircuitBreaker("res-reset");
        cb.onRequestComplete(context);
        long[] beforeReset = aggregateCounters(cb);
        assertEquals(1L, beforeReset[0]);
        assertEquals(0L, beforeReset[1]);
        assertEquals(50L, beforeReset[2]);
        cb.resetStat();
        long[] afterReset = aggregateCounters(cb);
        assertEquals("total after reset", 0L, afterReset[0]);
        assertEquals("error after reset", 0L, afterReset[1]);
        assertEquals("rt after reset", 0L, afterReset[2]);
    }

    @Test
    public void getScenario_defaultShouldBeInvalidAdaptiveScenario() {
        AdaptiveCircuitBreaker cb = new AdaptiveCircuitBreaker("res-scenario");
        assertEquals("Invalid adaptive scenario", cb.getScenario());
    }
}
