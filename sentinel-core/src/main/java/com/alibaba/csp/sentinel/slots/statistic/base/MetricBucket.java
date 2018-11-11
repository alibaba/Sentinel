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
package com.alibaba.csp.sentinel.slots.statistic.base;

import com.alibaba.csp.sentinel.Constants;

/**
 * Represents metrics data in a period of time window.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class MetricBucket {

    private final LongAdder pass = new LongAdder();
    private final LongAdder block = new LongAdder();
    private final LongAdder exception = new LongAdder();
    private final LongAdder rt = new LongAdder();
    private final LongAdder success = new LongAdder();

    private volatile long minRt;

    public MetricBucket() {
        initMinRt();
    }

    private void initMinRt() {
        this.minRt = Constants.TIME_DROP_VALVE;
    }

    /**
     * Clean the adders and reset window to provided start time.
     *
     * @return new clean window
     */
    public MetricBucket reset() {
        pass.reset();
        block.reset();
        exception.reset();
        rt.reset();
        success.reset();
        initMinRt();
        return this;
    }

    public long pass() {
        return pass.sum();
    }

    public long block() {
        return block.sum();
    }

    public long exception() {
        return exception.sum();
    }

    public long rt() {
        return rt.sum();
    }

    public long minRt() {
        return minRt;
    }

    public long success() {
        return success.sum();
    }

    public void addPass() {
        pass.add(1L);
    }

    public void addException() {
        exception.add(1L);
    }

    public void addBlock() {
        block.add(1L);
    }

    public void addSuccess() {
        success.add(1L);
    }

    public void addRT(long rt) {
        this.rt.add(rt);

        // Not thread-safe, but it's okay.
        if (rt < minRt) {
            minRt = rt;
        }
    }
}
