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
package com.alibaba.csp.sentinel.metric.extension;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Extended {@link MetricExtension} extending input parameters of each metric
 * collection method with {@link EntryType}.
 *
 * @author bill_yip
 * @author Eric Zhao
 * @since 1.8.0
 */
public interface AdvancedMetricExtension extends MetricExtension {

    /**
     * Add current pass count of the resource name.
     *
     * @param rw          resource representation (including resource name, traffic type, etc.)
     * @param batchCount  count to add
     * @param args        additional arguments of the resource, eg. if the resource is a method name,
     *                    the args will be the parameters of the method.
     */
    void onPass(ResourceWrapper rw, int batchCount, Object[] args);

    /**
     * Add current block count of the resource name.
     *
     * @param rw         resource representation (including resource name, traffic type, etc.)
     * @param batchCount count to add
     * @param origin     the origin of caller (if present)
     * @param e          the associated {@code BlockException}
     * @param args       additional arguments of the resource, eg. if the resource is a method name,
     *                   the args will be the parameters of the method.
     */
    void onBlocked(ResourceWrapper rw, int batchCount, String origin, BlockException e,
                   Object[] args);

    /**
     * Add current completed count of the resource name.
     *
     * @param rw         resource representation (including resource name, traffic type, etc.)
     * @param batchCount count to add
     * @param rt         response time of current invocation
     * @param args       additional arguments of the resource
     */
    void onComplete(ResourceWrapper rw, long rt, int batchCount, Object[] args);

    /**
     * Add current exception count of the resource name.
     *
     * @param rw         resource representation (including resource name, traffic type, etc.)
     * @param batchCount count to add
     * @param throwable  exception related.
     * @param args       additional arguments of the resource
     */
    void onError(ResourceWrapper rw, Throwable throwable, int batchCount, Object[] args);
}
