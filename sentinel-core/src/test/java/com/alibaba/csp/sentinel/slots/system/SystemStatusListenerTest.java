package com.alibaba.csp.sentinel.slots.system;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SystemStatusListenerTest {

    private static final int CORES = 8;

    /**
     * Builds a delta(processCpuTime) in ms that yields the given raw usage value
     * on a single 1-second tick with {@code CORES} available processors.
     */
    private static long deltaFor(double rawUsage) {
        return Math.round(rawUsage * 1000.0 * CORES);
    }

    @Test
    public void testRawValueClampedToPhysicalBound() {
        SystemStatusListener listener = new SystemStatusListener();
        // 28.75 CPU-seconds per second is physically impossible under an 8-core cgroup quota
        double v = listener.computeSmoothedProcessCpuUsage(28_750, 1000, CORES);
        assertEquals(1.0, v, 1e-9);
    }

    @Test
    public void testSingleSpikeIsDilutedBySlidingWindow() {
        SystemStatusListener listener = new SystemStatusListener();
        double v = 0;
        for (int i = 0; i < 4; i++) {
            v = listener.computeSmoothedProcessCpuUsage(deltaFor(0.4), 1000, CORES);
        }
        assertEquals(0.4, v, 1e-9);
        // single anomalous tick (clamped to 1.0) is diluted to 1/5 of the window
        v = listener.computeSmoothedProcessCpuUsage(28_750, 1000, CORES);
        assertEquals((0.4 * 4 + 1.0) / 5, v, 1e-9);
    }

    @Test
    public void testNegativeDeltaKeepsLastValue() {
        SystemStatusListener listener = new SystemStatusListener();
        double v = listener.computeSmoothedProcessCpuUsage(deltaFor(0.5), 1000, CORES);
        assertEquals(0.5, v, 1e-9);
        // unreadable processCpuTime (negative delta) keeps the last smoothed value
        v = listener.computeSmoothedProcessCpuUsage(-12345, 1000, CORES);
        assertEquals(0.5, v, 1e-9);
    }

    @Test
    public void testSustainedOverloadStillTriggers() {
        SystemStatusListener listener = new SystemStatusListener();
        double v = 0;
        for (int i = 0; i < 10; i++) {
            v = listener.computeSmoothedProcessCpuUsage(deltaFor(0.9), 1000, CORES);
        }
        assertTrue("sustained overload must stay above the 0.7 threshold", v > 0.7);
    }

    @Test
    public void testRampUpProtectionDelayIsBounded() {
        SystemStatusListener listener = new SystemStatusListener();
        double v = 0;
        for (double raw : new double[] {0.3, 0.5, 0.7, 0.9, 0.95}) {
            v = listener.computeSmoothedProcessCpuUsage(deltaFor(raw), 1000, CORES);
        }
        // window mean of (0.3, 0.5, 0.7, 0.9, 0.95): protection kicks in within a couple of ticks
        assertEquals(0.67, v, 0.01);
    }

    @Test
    public void testWindowNotFullAveragesActualSamples() {
        SystemStatusListener listener = new SystemStatusListener();
        double v = listener.computeSmoothedProcessCpuUsage(deltaFor(0.8), 1000, CORES);
        // only one sample in the window so far -> the average of that single sample
        assertEquals(0.8, v, 1e-9);
    }
}
