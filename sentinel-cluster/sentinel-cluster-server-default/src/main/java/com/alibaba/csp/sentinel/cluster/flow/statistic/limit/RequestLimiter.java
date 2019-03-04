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
package com.alibaba.csp.sentinel.cluster.flow.statistic.limit;

import java.util.List;

import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.LongAdder;
import com.alibaba.csp.sentinel.slots.statistic.base.UnaryLeapArray;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.4.1
 */
public class RequestLimiter {

    private double qpsAllowed;

    private final LeapArray<LongAdder> data;

    public RequestLimiter(double qpsAllowed) {
        this(new UnaryLeapArray(10, 1000), qpsAllowed);
    }

    RequestLimiter(LeapArray<LongAdder> data, double qpsAllowed) {
        AssertUtil.isTrue(qpsAllowed >= 0, "max allowed QPS should > 0");
        this.data = data;
        this.qpsAllowed = qpsAllowed;
    }

    public void increment() {
        data.currentWindow().value().increment();
    }

    public void add(int x) {
        data.currentWindow().value().add(x);
    }

    public long getSum() {
        data.currentWindow();
        long success = 0;

        List<LongAdder> list = data.values();
        for (LongAdder window : list) {
            success += window.sum();
        }
        return success;
    }

    public double getQps() {
        return getSum() / data.getIntervalInSecond();
    }

    public double getQpsAllowed() {
        return qpsAllowed;
    }

    public boolean canPass() {
        return getQps() + 1 <= qpsAllowed;
    }

    public RequestLimiter setQpsAllowed(double qpsAllowed) {
        this.qpsAllowed = qpsAllowed;
        return this;
    }

    public boolean tryPass() {
        if (canPass()) {
            add(1);
            return true;
        }
        return false;
    }
}
