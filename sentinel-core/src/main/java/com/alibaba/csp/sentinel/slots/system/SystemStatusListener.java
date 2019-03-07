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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author jialiang.linjl
 */
public class SystemStatusListener implements Runnable {

    volatile double currentLoad = -1;
    volatile double currentCpuUsage = -1;

    volatile String reason = StringUtil.EMPTY;

    static final int processor = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

    public double getSystemAverageLoad() {
        return currentLoad;
    }

    public double getCpuUsage() {
        return currentCpuUsage;
    }

    @Override
    public void run() {
        try {
            if (!SystemRuleManager.getCheckSystemStatus()) {
                return;
            }

            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            currentLoad = osBean.getSystemLoadAverage();
            /**
             * Java Doc copied from {@link OperatingSystemMXBean#getSystemCpuLoad()}:</br>
             * Returns the "recent cpu usage" for the whole system. This value is a double in the [0.0,1.0] interval.
             * A value of 0.0 means that all CPUs were idle during the recent period of time observed, while a value
             * of 1.0 means that all CPUs were actively running 100% of the time during the recent period being
             * observed. All values betweens 0.0 and 1.0 are possible depending of the activities going on in the
             * system. If the system recent cpu usage is not available, the method returns a negative value.
             */
            currentCpuUsage = osBean.getSystemCpuLoad();

            StringBuilder sb = new StringBuilder();
            if (currentLoad > SystemRuleManager.getHighestSystemLoad()) {
                sb.append("load:").append(currentLoad).append(";");
                sb.append("cpu:").append(currentCpuUsage).append(";");
                sb.append("qps:").append(Constants.ENTRY_NODE.passQps()).append(";");
                sb.append("rt:").append(Constants.ENTRY_NODE.avgRt()).append(";");
                sb.append("thread:").append(Constants.ENTRY_NODE.curThreadNum()).append(";");
                sb.append("success:").append(Constants.ENTRY_NODE.successQps()).append(";");
                sb.append("minRt:").append(Constants.ENTRY_NODE.minRt()).append(";");
                sb.append("maxSuccess:").append(Constants.ENTRY_NODE.maxSuccessQps()).append(";");
                RecordLog.info(sb.toString());
            }

        } catch (Throwable e) {
            RecordLog.info("could not get system error ", e);
        }
    }

}
