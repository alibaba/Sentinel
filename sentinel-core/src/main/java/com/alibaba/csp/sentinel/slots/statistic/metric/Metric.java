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

import java.util.List;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;

/**
 * Represents a basic structure recording invocation metrics of protected resources.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public interface Metric {

    /**
     * Get total success count.
     *
     * @return success count
     */
    long success();

    /**
     * Get max success count.
     *
     * @return max success count
     */
    long maxSuccess();

    /**
     * Get total exception count.
     *
     * @return exception count
     */
    long exception();

    /**
     * Get total block count.
     *
     * @return block count
     */
    long block();

    /**
     * Get total pass count.
     *
     * @return pass count
     */
    long pass();

    /**
     * Get total response time.
     *
     * @return total RT
     */
    long rt();

    /**
     * Get the minimal RT.
     *
     * @return minimal RT
     */
    long minRt();

    /**
     * Get aggregated metric nodes of all resources.
     *
     * @return metric node list of all resources
     */
    List<MetricNode> details();

    /**
     * Get the raw window array.
     *
     * @return window metric array
     */
    MetricBucket[] windows();

    /**
     * Increment by one the current exception count.
     */
    void addException(int n);

    /**
     * Increment by one the current block count.
     */
    void addBlock(int n);

    /**
     * Increment by one the current success count.
     */
    void addSuccess(int n);

    /**
     * Increment by one the current pass count.
     */
    void addPass(int n);

    /**
     * Add given RT to current total RT.
     *
     * @param rt RT
     */
    void addRT(long rt);

    double getWindowIntervalInSec();

    int getSampleCount();

    // Tool methods.

    void debugQps();

    long previousWindowBlock();

    long previousWindowPass();
}
