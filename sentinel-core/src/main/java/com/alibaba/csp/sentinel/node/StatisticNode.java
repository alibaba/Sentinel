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

    private transient Metric rollingCounterInSecond = new ArrayMetric(1000 / SampleCountProperty.sampleCount,
        IntervalProperty.INTERVAL);

    private transient Metric rollingCounterInMinute = new ArrayMetric(1000, 2 * 60);

    private AtomicInteger curThreadNum = new AtomicInteger(0);

    private long lastFetchTime = -1;

    @Override
    public Map<Long, MetricNode> metrics() {
        long currentTime = TimeUtil.currentTimeMillis();
        currentTime = currentTime - currentTime % 1000;
        Map<Long, MetricNode> metrics = new ConcurrentHashMap<Long, MetricNode>();
        List<MetricNode> minutes = rollingCounterInMinute.details();
        for (MetricNode node : minutes) {
            if (node.getTimestamp() > lastFetchTime && node.getTimestamp() < currentTime) {
                if (node.getPassedQps() != 0 || node.getBlockedQps() != 0) {
                    metrics.put(node.getTimestamp(), node);
                    lastFetchTime = node.getTimestamp();
                }
            }
        }

        return metrics;
    }

    @Override
    public void reset() {
        rollingCounterInSecond = new ArrayMetric(1000 / SampleCountProperty.sampleCount, IntervalProperty.INTERVAL);
    }

    @Override
    public long totalRequest() {
        long totalRequest = rollingCounterInMinute.pass() + rollingCounterInMinute.block();
        return totalRequest / 2;
    }

    @Override
    public long blockedRequest() {
        return rollingCounterInMinute.block() / 2;
    }

    @Override
    public long blockedQps() {
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
        return passQps() + blockedQps();
    }

    @Override
    public long totalSuccess() {
        return rollingCounterInMinute.success() / 2;
    }

    @Override
    public long exceptionQps() {
        return rollingCounterInSecond.exception() / IntervalProperty.INTERVAL;
    }

    @Override
    public long totalException() {
        return rollingCounterInMinute.exception() / 2;
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
        return rollingCounterInSecond.maxSuccess() * SampleCountProperty.sampleCount;
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
    public void increaseBlockedQps() {
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
