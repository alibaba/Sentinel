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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

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

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
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
        return true;
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
}

