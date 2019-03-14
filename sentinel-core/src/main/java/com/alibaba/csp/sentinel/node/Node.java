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
import com.alibaba.csp.sentinel.slots.statistic.metric.DebugSupport;

/**
 * Holds real-time statistics for resources.
 *
 * @author qinan.qn
 * @author leyou
 * @author Eric Zhao
 */
public interface Node extends OccupySupport, DebugSupport {

    /**
     * Get incoming request per minute ({@code pass + block}).
     *
     * @return total request count per minute
     */
    long totalRequest();

    /**
     * Get pass count per minute.
     *
     * @return total passed request count per minute
     * @since 1.5.0
     */
    long totalPass();

    /**
     * Get {@link Entry#exit()} count per minute.
     *
     * @return total completed request count per minute
     */
    long totalSuccess();

    /**
     * Get blocked request count per minute (totalBlockRequest).
     *
     * @return total blocked request count per minute
     */
    long blockRequest();

    /**
     * Get exception count per minute.
     *
     * @return total business exception count per minute
     */
    long totalException();

    /**
     * Get pass request per second.
     *
     * @return QPS of passed requests
     */
    double passQps();

    /**
     * Get block request per second.
     *
     * @return QPS of blocked requests
     */
    double blockQps();

    /**
     * Get {@link #passQps()} + {@link #blockQps()} request per second.
     *
     * @return QPS of passed and blocked requests
     */
    double totalQps();

    /**
     * Get {@link Entry#exit()} request per second.
     *
     * @return QPS of completed requests
     */
    double successQps();

    /**
     * Get estimated max success QPS till now.
     *
     * @return max completed QPS
     */
    double maxSuccessQps();

    /**
     * Get exception count per second.
     *
     * @return QPS of exception occurs
     */
    double exceptionQps();

    /**
     * Get average rt per second.
     *
     * @return average response time per second
     */
    double avgRt();

    /**
     * Get minimal response time.
     *
     * @return recorded minimal response time
     */
    double minRt();

    /**
     * Get current active thread count.
     *
     * @return current active thread count
     */
    int curThreadNum();

    /**
     * Get last second block QPS.
     */
    double previousBlockQps();

    /**
     * Last window QPS.
     */
    double previousPassQps();

    /**
     * Fetch all valid metric nodes of resources.
     *
     * @return valid metric nodes of resources
     */
    Map<Long, MetricNode> metrics();

    /**
     * Add pass count.
     *
     * @param count count to add pass
     */
    void addPassRequest(int count);

    /**
     * Add rt and success count.
     *
     * @param rt      response time
     * @param success success count to add
     */
    void addRtAndSuccess(long rt, int success);

    /**
     * Increase the block count.
     *
     * @param count count to add
     */
    void increaseBlockQps(int count);

    /**
     * Add the biz exception count.
     *
     * @param count count to add
     */
    void increaseExceptionQps(int count);

    /**
     * Increase current thread count.
     */
    void increaseThreadNum();

    /**
     * Decrease current thread count.
     */
    void decreaseThreadNum();

    /**
     * Reset the internal counter. Reset is needed when {@link IntervalProperty#INTERVAL} or
     * {@link SampleCountProperty#SAMPLE_COUNT} is changed.
     */
    void reset();
}
