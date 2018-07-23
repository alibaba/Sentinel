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
import java.lang.management.OperatingSystemMXBean;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.Constants;

/**
 * @author jialiang.linjl
 */
public class SystemStatusListener implements Runnable {

    volatile double currentLoad = -1;

    volatile String reason = StringUtil.EMPTY;

    static final int processor = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

    public double getSystemAverageLoad() {
        return currentLoad;
    }

    @Override
    public void run() {
        try {
            if (!SystemRuleManager.getCheckSystemStatus()) {
                return;
            }

            // system average load
            OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            currentLoad = operatingSystemMXBean.getSystemLoadAverage();

            StringBuilder sb = new StringBuilder();
            if (currentLoad > SystemRuleManager.getHighestSystemLoad()) {
                sb.append("load:").append(currentLoad).append(";");
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
