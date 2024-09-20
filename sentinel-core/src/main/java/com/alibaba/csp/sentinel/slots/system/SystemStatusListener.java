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
 * @author <a href="mailto:doob.yi@qq.com">doob</a>
 */
public class SystemStatusListener implements Runnable {

    public static final String CALCULATE_BY_SINGLE_CORE_KEY =
            "csp.sentinel.system.status.cpuUsageCalculateBySingleCore";

    /**
     * when run app in docker or k8s,
     * if set --cpu-period=100000 & --cpu-quota=100001 on docker run command(or set cpu request limit 1001m on k8s),
     * {@link OperatingSystemMXBean#getAvailableProcessors()} will return 2;
     * in this case, currentCpuUsage does not accurately express the percentage of available CPU resources;
     * Therefore, we need this parameter to make the currentCpuUsage meaning accurate;
     *
     * @since 1.8.1
     */
    public static Boolean CPU_USAGE_CALCULATE_BY_SINGLE_CORE;

    volatile double currentLoad = -1;
    volatile double currentCpuUsage = -1;

    volatile String reason = StringUtil.EMPTY;

    volatile long processCpuTime = 0;
    volatile long processUpTime = 0;

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
            double processCpuUsage = getProcessCpuUsage(processCpuTimeDiffInMs, processUpTimeDiffInMs, cpuCores);
            processCpuTime = newProcessCpuTime;
            processUpTime = newProcessUpTime;

            currentCpuUsage = Math.max(processCpuUsage, systemCpuUsage);

            if (currentLoad > SystemRuleManager.getSystemLoadThreshold()) {
                writeSystemStatusLog();
            }
        } catch (Throwable e) {
            RecordLog.warn("[SystemStatusListener] Failed to get system metrics from JMX", e);
        }
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

    private double getProcessCpuUsage(long processCpuTimeDiffInMs, long processUpTimeDiffInMs, int cpuCores) {
        double cpuUsageCalculateBySingleCore = (double) processCpuTimeDiffInMs / processUpTimeDiffInMs;
        if (CPU_USAGE_CALCULATE_BY_SINGLE_CORE == null) {
            CPU_USAGE_CALCULATE_BY_SINGLE_CORE = Boolean.parseBoolean(System.getProperty(CALCULATE_BY_SINGLE_CORE_KEY));
        }
        if (CPU_USAGE_CALCULATE_BY_SINGLE_CORE) {
            return cpuUsageCalculateBySingleCore;
        }
        return cpuUsageCalculateBySingleCore / cpuCores;
    }

}
