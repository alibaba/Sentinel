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
package com.alibaba.csp.sentinel.slots.system;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author jialiang.linjl
 */
public class SystemStatusListener implements Runnable {

    /**
     * Size of the sliding window (in samples) used to smooth the process CPU usage.
     * The listener ticks once per second, so the window covers ~10 seconds. A window of
     * this size also absorbs the coarse read granularity of {@code getProcessCpuTime()}
     * observed in some containerized environments (see #3669).
     */
    private static final int CPU_USAGE_SMOOTH_WINDOW = 10;

    volatile double currentLoad = -1;
    volatile double currentCpuUsage = -1;

    volatile String reason = StringUtil.EMPTY;

    volatile long processCpuTime = 0;
    volatile long processUpTime = 0;

    private final double[] processCpuUsageWindow = new double[CPU_USAGE_SMOOTH_WINDOW];
    private int processCpuUsageWindowIndex = 0;
    private int processCpuUsageWindowCount = 0;
    private double lastSmoothedProcessCpuUsage = -1;

    public double getSystemAverageLoad() {
        return currentLoad;
    }

    public double getCpuUsage() {
        return currentCpuUsage;
    }

    @Override
    public void run() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            currentLoad = osBean.getSystemLoadAverage();

            /*
             * Java Doc copied from {@link OperatingSystemMXBean#getSystemCpuLoad()}:</br>
             * Returns the "recent cpu usage" for the whole system. This value is a double in the [0.0,1.0] interval.
             * A value of 0.0 means that all CPUs were idle during the recent period of time observed, while a value
             * of 1.0 means that all CPUs were actively running 100% of the time during the recent period being
             * observed. All values between 0.0 and 1.0 are possible depending of the activities going on in the
             * system. If the system recent cpu usage is not available, the method returns a negative value.
             */
            double systemCpuUsage = osBean.getSystemCpuLoad();

            // calculate process cpu usage to support application running in container environment
            RuntimeMXBean runtimeBean = ManagementFactory.getPlatformMXBean(RuntimeMXBean.class);
            long newProcessCpuTime = osBean.getProcessCpuTime();
            long newProcessUpTime = runtimeBean.getUptime();
            int cpuCores = osBean.getAvailableProcessors();
            long processCpuTimeDiffInMs = TimeUnit.NANOSECONDS
                    .toMillis(newProcessCpuTime - processCpuTime);
            long processUpTimeDiffInMs = newProcessUpTime - processUpTime;
            processCpuTime = newProcessCpuTime;
            processUpTime = newProcessUpTime;

            double processCpuUsage = computeSmoothedProcessCpuUsage(
                    processCpuTimeDiffInMs, processUpTimeDiffInMs, cpuCores);

            currentCpuUsage = Math.max(processCpuUsage, systemCpuUsage);

            if (currentLoad > SystemRuleManager.getSystemLoadThreshold()) {
                writeSystemStatusLog();
            }
        } catch (Throwable e) {
            RecordLog.warn("[SystemStatusListener] Failed to get system metrics from JMX", e);
        }
    }

    /**
     * Computes the process CPU usage from the given sample deltas, clamped to the physical
     * bound [0.0, 1.0] and smoothed with a sliding window over the latest
     * {@link #CPU_USAGE_SMOOTH_WINDOW} samples.
     *
     * <p>The raw per-sample value is {@code delta(processCpuTime) / delta(uptime) / availableProcessors}.
     * The listener ticks once per second, and the raw value is consumed directly by
     * {@code SystemRuleManager#checkSystem}. Without bounding and smoothing, a single abnormal
     * sample (e.g. caused by GC bursts or the coarse read granularity of {@code getProcessCpuTime()}
     * in containerized environments &mdash; we observed snapshot values up to 28.75 while the real
     * process CPU usage was below 0.05, see #3669) blocks all inbound requests for that second.
     *
     * <p>If the sample is not usable (negative deltas, or an unreadable {@code processCpuTime}),
     * the last smoothed value is kept.
     */
    double computeSmoothedProcessCpuUsage(long processCpuTimeDiffInMs,
                                          long processUpTimeDiffInMs,
                                          int cpuCores) {
        double last = lastSmoothedProcessCpuUsage >= 0 ? lastSmoothedProcessCpuUsage : 0.0;
        if (processCpuTimeDiffInMs < 0 || processUpTimeDiffInMs <= 0) {
            return last;
        }
        double raw = (double) processCpuTimeDiffInMs / processUpTimeDiffInMs / cpuCores;
        // The container's cgroup quota bounds the whole process at availableProcessors
        // CPU-seconds per second, so a value outside [0.0, 1.0] is physically impossible
        // (measurement anomaly) and must not be fed into the system-rule check.
        raw = Math.min(1.0, Math.max(0.0, raw));
        processCpuUsageWindow[processCpuUsageWindowIndex] = raw;
        processCpuUsageWindowIndex = (processCpuUsageWindowIndex + 1) % CPU_USAGE_SMOOTH_WINDOW;
        if (processCpuUsageWindowCount < CPU_USAGE_SMOOTH_WINDOW) {
            processCpuUsageWindowCount++;
        }
        double sum = 0.0;
        for (int i = 0; i < processCpuUsageWindowCount; i++) {
            sum += processCpuUsageWindow[i];
        }
        lastSmoothedProcessCpuUsage = sum / processCpuUsageWindowCount;
        return lastSmoothedProcessCpuUsage;
    }

    private void writeSystemStatusLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("Load exceeds the threshold: ");
        sb.append("load:").append(String.format("%.4f", currentLoad)).append("; ");
        sb.append("cpuUsage:").append(String.format("%.4f", currentCpuUsage)).append("; ");
        sb.append("qps:").append(String.format("%.4f", Constants.ENTRY_NODE.passQps())).append("; ");
        sb.append("rt:").append(String.format("%.4f", Constants.ENTRY_NODE.avgRt())).append("; ");
        sb.append("thread:").append(Constants.ENTRY_NODE.curThreadNum()).append("; ");
        sb.append("success:").append(String.format("%.4f", Constants.ENTRY_NODE.successQps())).append("; ");
        sb.append("minRt:").append(String.format("%.2f", Constants.ENTRY_NODE.minRt())).append("; ");
        sb.append("maxSuccess:").append(String.format("%.2f", Constants.ENTRY_NODE.maxSuccessQps())).append("; ");
        RecordLog.info(sb.toString());
    }
}
