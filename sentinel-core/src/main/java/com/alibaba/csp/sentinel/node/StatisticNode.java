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
package com.alibaba.csp.sentinel.node;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.metric.ArrayMetric;
import com.alibaba.csp.sentinel.slots.statistic.metric.Metric;

/**
 * @author qinan.qn
 * @author jialiang.linjl
 */
public class StatisticNode implements Node {

    private transient volatile Metric rollingCounterInSecond = new ArrayMetric(1000 / SampleCountProperty.SAMPLE_COUNT,
        IntervalProperty.INTERVAL);

    /**
     * Holds statistics of the recent 60 seconds. The windowLengthInMs is deliberately set to 1000 milliseconds,
     * meaning each bucket per second, in this way we can get accurate statistics of each second.
     */
    private transient Metric rollingCounterInMinute = new ArrayMetric(1000, 60);

    private AtomicInteger curThreadNum = new AtomicInteger(0);

    private long lastFetchTime = -1;

    @Override
    public Map<Long, MetricNode> metrics() {
        long currentTime = TimeUtil.currentTimeMillis();
        currentTime = currentTime - currentTime % 1000;
        Map<Long, MetricNode> metrics = new ConcurrentHashMap<Long, MetricNode>();
        List<MetricNode> nodesOfEverySecond = rollingCounterInMinute.details();
        long newLastFetchTime = lastFetchTime;
        for (MetricNode node : nodesOfEverySecond) {
            if (node.getTimestamp() > lastFetchTime && node.getTimestamp() < currentTime) {
                if (node.getPassQps() != 0
                    || node.getBlockQps() != 0
                    || node.getSuccessQps() != 0
                    || node.getExceptionQps() != 0
                    || node.getRt() != 0) {
                    metrics.put(node.getTimestamp(), node);
                    newLastFetchTime = Math.max(newLastFetchTime, node.getTimestamp());
                }
            }
        }
        lastFetchTime = newLastFetchTime;

        return metrics;
    }

    @Override
    public void reset() {
        rollingCounterInSecond = new ArrayMetric(1000 / SampleCountProperty.SAMPLE_COUNT, IntervalProperty.INTERVAL);
    }

    @Override
    public long totalRequest() {
        long totalRequest = rollingCounterInMinute.pass() + rollingCounterInMinute.block();
        return totalRequest;
    }

    @Override
    public long blockRequest() {
        return rollingCounterInMinute.block();
    }

    @Override
    public long blockQps() {
        return rollingCounterInSecond.block() / IntervalProperty.INTERVAL;
    }

    @Override
    public long previousBlockQps() {
        return this.rollingCounterInMinute.previousWindowBlock();
    }

    @Override
    public long previousPassQps() {
        return this.rollingCounterInMinute.previousWindowPass();
    }

    @Override
    public long totalQps() {
        return passQps() + blockQps();
    }

    @Override
    public long totalSuccess() {
        return rollingCounterInMinute.success();
    }

    @Override
    public long exceptionQps() {
        return rollingCounterInSecond.exception() / IntervalProperty.INTERVAL;
    }

    @Override
    public long totalException() {
        return rollingCounterInMinute.exception();
    }

    @Override
    public long passQps() {
        return rollingCounterInSecond.pass() / IntervalProperty.INTERVAL;
    }

    @Override
    public long successQps() {
        return rollingCounterInSecond.success() / IntervalProperty.INTERVAL;
    }

    @Override
    public long maxSuccessQps() {
        return rollingCounterInSecond.maxSuccess() * SampleCountProperty.SAMPLE_COUNT;
    }

    @Override
    public long avgRt() {
        long successCount = rollingCounterInSecond.success();
        if (successCount == 0) {
            return 0;
        }

        return rollingCounterInSecond.rt() / successCount;
    }

    @Override
    public long minRt() {
        return rollingCounterInSecond.minRt();
    }

    @Override
    public int curThreadNum() {
        return curThreadNum.get();
    }

    @Override
    public void addPassRequest() {
        rollingCounterInSecond.addPass();
        rollingCounterInMinute.addPass();
    }

    @Override
    public void rt(long rt) {
        rollingCounterInSecond.addSuccess();
        rollingCounterInSecond.addRT(rt);

        rollingCounterInMinute.addSuccess();
        rollingCounterInMinute.addRT(rt);
    }

    @Override
    public void increaseBlockQps() {
        rollingCounterInSecond.addBlock();
        rollingCounterInMinute.addBlock();
    }

    @Override
    public void increaseExceptionQps() {
        rollingCounterInSecond.addException();
        rollingCounterInMinute.addException();

    }

    @Override
    public void increaseThreadNum() {
        curThreadNum.incrementAndGet();
    }

    @Override
    public void decreaseThreadNum() {
        curThreadNum.decrementAndGet();
    }

    @Override
    public void debug() {
        rollingCounterInSecond.debugQps();
    }
}
