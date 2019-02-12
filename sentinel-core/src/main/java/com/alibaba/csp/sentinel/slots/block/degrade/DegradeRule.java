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
package com.alibaba.csp.sentinel.slots.block.degrade;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

/**
 * <p>
 * Degrade is used when the resources are in an unstable state, these resources
 * will be degraded within the next defined time window. There are two ways to
 * measure whether a resource is stable or not:
 * </p>
 * <ul>
 * <li>
 * Average response time ({@code DEGRADE_GRADE_RT}): When
 * the average RT exceeds the threshold ('count' in 'DegradeRule', in milliseconds), the
 * resource enters a quasi-degraded state. If the RT of next coming 5
 * requests still exceed this threshold, this resource will be downgraded, which
 * means that in the next time window (defined in 'timeWindow', in seconds) all the
 * access to this resource will be blocked.
 * </li>
 * <li>
 * Exception ratio: When the ratio of exception count per second and the
 * success qps exceeds the threshold, access to the resource will be blocked in
 * the coming window.
 * </li>
 * </ul>
 *
 * @author jialiang.linjl
 */
public class DegradeRule extends AbstractRule {

    private static final int RT_MAX_EXCEED_N = 5;

    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("sentinel-degrade-reset-task", true));

    public DegradeRule() {}

    public DegradeRule(String resourceName) {
        setResource(resourceName);
    }

    /**
     * RT threshold or exception ratio threshold count.
     */
    private double count;

    /**
     * Degrade recover timeout (in seconds) when degradation occurs.
     */
    private int timeWindow;

    /**
     * Degrade strategy (0: average RT, 1: exception ratio).
     */
    private int grade = RuleConstant.DEGRADE_GRADE_RT;

    private volatile boolean cut = false;

    public int getGrade() {
        return grade;
    }

    public DegradeRule setGrade(int grade) {
        this.grade = grade;
        return this;
    }

    private AtomicLong passCount = new AtomicLong(0);

    private final Object lock = new Object();

    public double getCount() {
        return count;
    }

    public DegradeRule setCount(double count) {
        this.count = count;
        return this;
    }

    public boolean isCut() {
        return cut;
    }

    private void setCut(boolean cut) {
        this.cut = cut;
    }

    public AtomicLong getPassCount() {
        return passCount;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public DegradeRule setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DegradeRule)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DegradeRule that = (DegradeRule)o;

        if (count != that.count) {
            return false;
        }
        if (timeWindow != that.timeWindow) {
            return false;
        }
        if (grade != that.grade) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + new Double(count).hashCode();
        result = 31 * result + timeWindow;
        result = 31 * result + grade;
        return result;
    }

    @Override
    public boolean passCheck(Context context, DefaultNode node, int acquireCount, Object... args) {
        if (cut) {
            return false;
        }

        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNode(this.getResource());
        if (clusterNode == null) {
            return true;
        }

        if (grade == RuleConstant.DEGRADE_GRADE_RT) {
            double rt = clusterNode.avgRt();
            if (rt < this.count) {
                passCount.set(0);
                return true;
            }

            // Sentinel will degrade the service only if count exceeds.
            if (passCount.incrementAndGet() < RT_MAX_EXCEED_N) {
                return true;
            }
        } else if (grade == RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO) {
            double exception = clusterNode.exceptionQps();
            double success = clusterNode.successQps();
            long total = clusterNode.totalQps();
            // if total qps less than RT_MAX_EXCEED_N, pass.
            if (total < RT_MAX_EXCEED_N) {
                return true;
            }

            double realSuccess = success - exception;
            if (realSuccess <= 0 && exception < RT_MAX_EXCEED_N) {
                return true;
            }

            if (exception / success < count) {
                return true;
            }
        } else if (grade == RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
            double exception = clusterNode.totalException();
            if (exception < count) {
                return true;
            }
        }

        synchronized (lock) {
            if (!cut) {
                // Automatically degrade.
                cut = true;
                ResetTask resetTask = new ResetTask(this);
                pool.schedule(resetTask, timeWindow, TimeUnit.SECONDS);
            }

            return false;
        }
    }

    @Override
    public String toString() {
        return "DegradeRule{" +
            "resource=" + getResource() +
            ", grade=" + grade +
            ", count=" + count +
            ", limitApp=" + getLimitApp() +
            ", timeWindow=" + timeWindow +
            "}";
    }

    private static final class ResetTask implements Runnable {

        private DegradeRule rule;

        ResetTask(DegradeRule rule) {
            this.rule = rule;
        }

        @Override
        public void run() {
            rule.getPassCount().set(0);
            rule.setCut(false);
        }
    }
}

