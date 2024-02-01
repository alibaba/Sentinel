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

import com.alibaba.csp.sentinel.slots.block.AbstractRule;

/**
 * <p>
 * Sentinel System Rule makes the inbound traffic and capacity meet. It takes
 * average RT, QPS and thread count of requests into account. And it also
 * provides a measurement of system's load, but only available on Linux.
 * </p>
 * <p>
 * We recommend to coordinate {@link SystemMetricType#LOAD}, {@link SystemMetricType#INBOUND_QPS}, {@link SystemMetricType#AVG_RT}
 * and {@link SystemMetricType#CONCURRENCY} to make sure your system run in safety level.
 * </p>
 * <p>
 * To set the threshold appropriately, performance test may be needed.
 * </p>
 *
 * @author jialiang.linjl
 * @author Carpenter Lee
 * @author guozhong.huang
 * @see SystemRuleManager
 */
public class SystemRule extends AbstractRule {


    /**
     * MetricType indicates the type of the trigger metric.
     */
    private SystemMetricType systemMetricType;

    /**
     * TriggerCount represents the lower bound trigger of the adaptive strategy.
     * Adaptive strategies will not be activated until target metric has reached the trigger count.
     */
    private double triggerCount;

    public SystemMetricType getSystemMetricType() {
        return systemMetricType;
    }

    public SystemRule setSystemMetricType(SystemMetricType systemMetricType) {
        this.systemMetricType = systemMetricType;
        return this;
    }


    public double getTriggerCount() {
        return triggerCount;
    }

    public SystemRule setTriggerCount(double triggerCount) {
        this.triggerCount = triggerCount;
        return this;
    }

    @Override
    public String toString() {
        return "SystemRule{" +
                "systemMetricType=" + systemMetricType +
                ", triggerCount=" + triggerCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SystemRule that = (SystemRule) o;

        if (Double.compare(that.triggerCount, triggerCount) != 0) {
            return false;
        }
        return systemMetricType == that.systemMetricType;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + systemMetricType.hashCode();
        temp = Double.doubleToLongBits(triggerCount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
