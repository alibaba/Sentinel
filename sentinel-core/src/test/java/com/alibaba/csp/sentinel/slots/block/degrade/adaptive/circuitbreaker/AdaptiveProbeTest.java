package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveProbe.ProbeResults.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AdaptiveProbe}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AdaptiveProbeTest {

    private AdaptiveCircuitBreaker cb;

    @Before
    public void setUp() {
        cb = new AdaptiveCircuitBreaker("res-probe");
    }

    private void configProbe(AdaptiveProbe probe, int limit) {
        probe.setReleaseRequestLimit(limit);
        probe.setHalfOpenTimeoutMs(System.currentTimeMillis() + (long) 200000);
    }

    private Context makeContext(boolean withError) {
        Context ctx = mock(Context.class);
        Entry entry = mock(Entry.class);
        when(ctx.getCurEntry()).thenReturn(entry);
        when(entry.getError()).thenReturn(withError ? new RuntimeException("err") : null);
        when(entry.getCreateTimestamp()).thenReturn(System.currentTimeMillis());
        when(entry.getCompleteTimestamp()).thenReturn(System.currentTimeMillis());
        return ctx;
    }

    private long[] aggregateCounters() {
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows = cb.getWindows();
        long total = 0, error = 0;
        for (WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> w : windows) {
            if (w == null || w.value() == null) continue;
            total += w.value().getTotalCount().sum();
            error += w.value().getErrorCount().sum();
        }
        return new long[]{total, error};
    }

    @Test
    public void handleProbeRequest_shouldAllowUpToLimit_thenRejectAfterLimit() {
        AdaptiveProbe probe = new AdaptiveProbe(cb);
        int limit = 3;
        configProbe(probe, limit /* 60s later */);
        for (int i = 0; i < limit; i++) {
            Context c = makeContext(false);
            assertTrue("should allow within limit", probe.handleProbeRequest(c));
        }
        assertFalse("should reject after reaching limit", probe.handleProbeRequest(makeContext(false)));
    }

    @Test
    public void handleProbeRequestOnComplete_shouldFailImmediately_whenTimeoutReached_andResetState() {
        AdaptiveProbe probe = new AdaptiveProbe(cb);
        probe.setHalfOpenTimeoutMs(System.currentTimeMillis() - 1);
        assertFalse("timeout -> handleProbeRequest should reject", probe.handleProbeRequest(makeContext(false)));
        AdaptiveProbe.ProbeResults r = probe.handleProbeRequestOnComplete(makeContext(false));
        assertEquals(FAIL, r);
        configProbe(probe, 2);
        assertTrue("after reset, should allow again", probe.handleProbeRequest(makeContext(false)));
    }

    @Test
    public void probeResults_shouldBeWaitingUntilAllCollected_thenSUCCESS_whenAllSuccess_noHistory() {
        AdaptiveProbe probe = new AdaptiveProbe(cb);
        int limit = 5;
        configProbe(probe, limit);
        Context[] cs = new Context[limit];
        for (int i = 0; i < limit; i++) {
            cs[i] = makeContext(false);
            assertTrue(probe.handleProbeRequest(cs[i]));
        }
        for (int i = 0; i < limit - 1; i++) {
            assertEquals(WAITING, probe.handleProbeRequestOnComplete(cs[i]));
        }
        assertEquals(SUCCESS, probe.handleProbeRequestOnComplete(cs[limit - 1]));
    }

    @Test
    public void probeResults_shouldFAIL_whenCurrentErrorRateExceedsDefaultThreshold_noHistory() {
        AdaptiveProbe probe = new AdaptiveProbe(cb);
        int limit = 5;
        configProbe(probe, limit);
        Context[] cs = new Context[limit];
        for (int i = 0; i < limit; i++) {
            boolean withError = (i == 0);
            cs[i] = makeContext(withError);
            assertTrue(probe.handleProbeRequest(cs[i]));
        }
        for (int i = 0; i < limit - 1; i++) {
            assertEquals(WAITING, probe.handleProbeRequestOnComplete(cs[i]));
        }
        assertEquals(FAIL, probe.handleProbeRequestOnComplete(cs[limit - 1]));
    }

    @Test
    public void probeResults_shouldUseHistoricalMeanPlus2Sigma_thresholdFromHistory_singleWindow() {
        AdaptiveProbe probe = new AdaptiveProbe(cb);
        int limit = 5;
        configProbe(probe, limit);
        for (int i = 0; i < 10; i++) {
            Context hx = makeContext(i < 4);
            cb.onRequestComplete(hx);
        }
        long[] agg = aggregateCounters();
        assertEquals("history total", 10L, agg[0]);
        assertEquals("history error", 4L, agg[1]);
        Context[] cs = new Context[limit];
        for (int i = 0; i < limit; i++) {
            boolean withError = (i == 0);
            cs[i] = makeContext(withError);
            assertTrue(probe.handleProbeRequest(cs[i]));
        }
        for (int i = 0; i < limit - 1; i++) {
            assertEquals(WAITING, probe.handleProbeRequestOnComplete(cs[i]));
        }
        assertEquals(SUCCESS, probe.handleProbeRequestOnComplete(cs[limit - 1]));
    }
}
