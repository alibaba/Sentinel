/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.metric.extension.AdvancedMetricExtension;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author bill_yip
 * @author Eric Zhao
 */
class FakeAdvancedMetricExtension implements AdvancedMetricExtension {

    long pass = 0;
    long block = 0;
    long complete = 0;
    long exception = 0;
    long rt = 0;

    long concurrency = 0;

    @Override
    public void onPass(ResourceWrapper rw, int batchCount, Object[] args) {
        this.pass += batchCount;
        this.concurrency++;
    }

    @Override
    public void onBlocked(ResourceWrapper rw, int batchCount, String origin, BlockException e, Object[] args) {
        this.block += batchCount;
    }

    @Override
    public void onComplete(ResourceWrapper rw, long rt, int batchCount, Object[] args) {
        this.complete += batchCount;
        this.rt += rt;
        this.concurrency--;
    }

    @Override
    public void onError(ResourceWrapper rw, Throwable throwable, int batchCount, Object[] args) {
        this.exception += batchCount;
    }

    @Override
    public void addPass(String resource, int n, Object... args) {
        // Do nothing because of using the enhanced one
    }

    @Override
    public void addBlock(String resource, int n, String origin, BlockException blockException, Object... args) {
        // Do nothing because of using the enhanced one
    }

    @Override
    public void addSuccess(String resource, int n, Object... args) {
        // Do nothing because of using the enhanced one
    }

    @Override
    public void addException(String resource, int n, Throwable throwable) {
        // Do nothing because of using the enhanced one
    }

    @Override
    public void addRt(String resource, long rt, Object... args) {
        // Do nothing because of using the enhanced one
    }

    @Override
    public void increaseThreadNum(String resource, Object... args) {
        // Do nothing because of using the enhanced one
    }

    @Override
    public void decreaseThreadNum(String resource, Object... args) {
        // Do nothing because of using the enhanced one
    }
}
