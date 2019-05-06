package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author Carpenter Lee
 */
class FakeMetricExtension implements MetricExtension {
    long pass = 0;
    long block = 0;
    long success = 0;
    long exception = 0;
    long rt = 0;
    long thread = 0;

    @Override
    public void addPass(String resource, int n, Object... args) {
        pass += n;
    }

    @Override
    public void addBlock(String resource, int n, String origin, BlockException ex, Object... args) {
        block += n;
    }

    @Override
    public void addSuccess(String resource, int n, Object... args) {
        success += n;
    }

    @Override
    public void addException(String resource, int n, Throwable t) {
        exception += n;
    }

    @Override
    public void addRt(String resource, long rt, Object... args) {
        this.rt += rt;
    }

    @Override
    public void increaseThreadNum(String resource, Object... args) {
        thread++;
    }

    @Override
    public void decreaseThreadNum(String resource, Object... args) {
        thread--;
    }
}
