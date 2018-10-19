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

import java.util.Map;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.node.metric.MetricNode;

/**
 * This class holds real-time statistics for a resource.
 *
 * @author qinan.qn
 * @author leyou
 */
public interface Node {

    /**
     * Get incoming request per minute. {@code pass + block}
     */
    long totalRequest();

    /**
     * Get {@link Entry#exit()} count per minute.
     *
     * @return Outgoing request per minute.
     */
    long totalSuccess();

    /**
     * Get block request count per minute.
     */
    long blockRequest();

    /**
     * Get exception count per minute.
     */
    long totalException();

    /**
     * Get pass request per second.
     */
    long passQps();

    /**
     * Get block request per second.
     */
    long blockQps();

    /**
     * Get {@link #passQps()} + {@link #blockQps()} request per second.
     */
    long totalQps();

    /**
     * Get {@link Entry#exit()} request per second.
     */
    long successQps();

    long maxSuccessQps();

    /**
     * Get exception count per second.
     */
    long exceptionQps();

    /**
     * Get average rt per second.
     */
    long avgRt();

    long minRt();

    /**
     * Get current active thread count.
     */
    int curThreadNum();

    /**
     * Get last second block QPS.
     */
    long previousBlockQps();

    /**
     * Last window QPS.
     */
    long previousPassQps();

    Map<Long, MetricNode> metrics();

    void addPassRequest();

    /**
     * Add rt and success count.
     *
     * @param rt
     */
    void rt(long rt);

    void increaseBlockQps();

    void increaseExceptionQps();

    void increaseThreadNum();

    void decreaseThreadNum();

    /**
     * Reset the internal counter. Reset is needed when {@link IntervalProperty#INTERVAL} or
     * {@link SampleCountProperty#SAMPLE_COUNT} is changed.
     */
    void reset();

    /**
     * Debug only.
     */
    void debug();
}
