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
package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.base.MetricBucket;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

/**
 * The basic metric class in Sentinel using a {@link MetricsLeapArray} internal.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class ArrayMetric implements Metric {

    private final MetricsLeapArray data;

    /**
     * Constructor
     *
     * @param windowLengthInMs a single window bucket's time length in milliseconds.
     * @param intervalInSec    the total time span of this {@link ArrayMetric} in seconds.
     */
    public ArrayMetric(int windowLengthInMs, int intervalInSec) {
        this.data = new MetricsLeapArray(windowLengthInMs, intervalInSec);
    }

    /**
     * For unit test.
     */
    public ArrayMetric(MetricsLeapArray array) {
        this.data = array;
    }

    @Override
    public long success() {
        data.currentWindow();
        long success = 0;

        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            success += window.success();
        }
        return success;
    }

    @Override
    public long maxSuccess() {
        data.currentWindow();
        long success = 0;

        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            if (window.success() > success) {
                success = window.success();
            }
        }
        return Math.max(success, 1);
    }

    @Override
    public long exception() {
        data.currentWindow();
        long exception = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            exception += window.exception();
        }
        return exception;
    }

    @Override
    public long block() {
        data.currentWindow();
        long block = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            block += window.block();
        }
        return block;
    }

    @Override
    public long pass() {
        data.currentWindow();
        long pass = 0;
        List<MetricBucket> list = data.values();

        for (MetricBucket window : list) {
            pass += window.pass();
        }
        return pass;
    }

    @Override
    public long rt() {
        data.currentWindow();
        long rt = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            rt += window.rt();
        }
        return rt;
    }

    @Override
    public long minRt() {
        data.currentWindow();
        long rt = Constants.TIME_DROP_VALVE;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            if (window.minRt() < rt) {
                rt = window.minRt();
            }
        }

        return Math.max(1, rt);
    }

    @Override
    public List<MetricNode> details() {
        List<MetricNode> details = new ArrayList<MetricNode>();
        data.currentWindow();
        for (WindowWrap<MetricBucket> window : data.list()) {
            if (window == null) {
                continue;
            }
            MetricNode node = new MetricNode();
            node.setBlockQps(window.value().block());
            node.setExceptionQps(window.value().exception());
            node.setPassQps(window.value().pass());
            long passQps = window.value().success();
            node.setSuccessQps(passQps);
            if (passQps != 0) {
                node.setRt(window.value().rt() / passQps);
            } else {
                node.setRt(window.value().rt());
            }
            node.setTimestamp(window.windowStart());
            details.add(node);
        }

        return details;
    }

    @Override
    public MetricBucket[] windows() {
        data.currentWindow();
        return data.values().toArray(new MetricBucket[data.values().size()]);
    }

    @Override
    public void addException() {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addException();
    }

    @Override
    public void addBlock() {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addBlock();
    }

    @Override
    public void addSuccess() {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addSuccess();
    }

    @Override
    public void addPass() {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addPass();
    }

    @Override
    public void addRT(long rt) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addRT(rt);
    }

    @Override
    public void debugQps() {
        data.currentWindow();
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().getId()).append("_");
        for (WindowWrap<MetricBucket> windowWrap : data.list()) {

            sb.append(windowWrap.windowStart()).append(":").append(windowWrap.value().pass()).append(":")
                .append(windowWrap.value().block());
            sb.append(",");

        }
        System.out.println(sb);
    }

    @Override
    public long previousWindowBlock() {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap = data.getPreviousWindow();
        if (wrap == null) {
            return 0;
        }
        return wrap.value().block();
    }

    @Override
    public long previousWindowPass() {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap = data.getPreviousWindow();
        if (wrap == null) {
            return 0;
        }
        return wrap.value().pass();
    }
}
