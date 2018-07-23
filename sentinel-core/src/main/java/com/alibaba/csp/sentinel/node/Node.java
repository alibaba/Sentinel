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

import com.alibaba.csp.sentinel.node.metric.MetricNode;

/**
 * This class holds real-time statistics for a resource.
 *
 * @author qinan.qn
 * @author leyou
 */
public interface Node {

    /**
     * Incoming request per minute.
     */
    long totalRequest();

    long totalSuccess();

    /**
     * Blocked request count per minute.
     */
    long blockedRequest();

    /**
     * Exception count per minute.
     */
    long totalException();

    /**
     * Incoming request per second.
     */
    long passQps();

    /**
     * Blocked request per second.
     */
    long blockedQps();

    /**
     * Incoming request + block request per second.
     */
    long totalQps();

    /**
     * Outgoing request per second.
     */
    long successQps();

    long maxSuccessQps();

    /**
     * Exception count per second.
     */
    long exceptionQps();

    /**
     * Average response per second.
     */
    long avgRt();

    long minRt();

    /**
     * Current active thread.
     */
    int curThreadNum();

    /**
     * Last seconds block QPS.
     */
    long previousBlockQps();

    /**
     * Last window QPS.
     */
    long previousPassQps();

    Map<Long, MetricNode> metrics();

    void addPassRequest();

    void rt(long rt);

    void increaseBlockedQps();

    void increaseExceptionQps();

    void increaseThreadNum();

    void decreaseThreadNum();

    /**
     * Reset the internal counter.
     */
    void reset();

    /**
     * Debug only.
     */
    void debug();
}
