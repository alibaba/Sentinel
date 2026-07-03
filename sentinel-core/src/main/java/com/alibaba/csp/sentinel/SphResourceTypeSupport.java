/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * @author Eric Zhao
 * @since 1.7.0
 */
public interface SphResourceTypeSupport {

    /**
     * Record statistics and perform rule checking for the given resource with provided classification.
     *
     * @param name         the unique name of the protected resource
     * @param resourceType the classification of the resource
     * @param trafficType  the traffic type (inbound, outbound or internal). This is used
     *                     to mark whether it can be blocked when the system is unstable,
     *                     only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount   the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param args         args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met
     */
    Entry entryWithType(String name, int resourceType, EntryType trafficType, int batchCount, Object[] args)
        throws BlockException;

    /**
     * Record statistics and perform rule checking for the given resource with the provided classification.
     *
     * @param name         the unique name of the protected resource
     * @param resourceType classification of the resource (e.g. Web or RPC)
     * @param trafficType  the traffic type (inbound, outbound or internal). This is used
     *                     to mark whether it can be blocked when the system is unstable,
     *                     only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount   the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param prioritized  whether the entry is prioritized
     * @param args         args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met
     */
    Entry entryWithType(String name, int resourceType, EntryType trafficType, int batchCount, boolean prioritized,
                        Object[] args) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given resource that indicates an async invocation.
     *
     * @param name         the unique name for the protected resource
     * @param resourceType classification of the resource (e.g. Web or RPC)
     * @param trafficType  the traffic type (inbound, outbound or internal). This is used
     *                     to mark whether it can be blocked when the system is unstable,
     *                     only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount   the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param prioritized  whether the entry is prioritized
     * @param args         args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met
     */
    AsyncEntry asyncEntryWithType(String name, int resourceType, EntryType trafficType, int batchCount,
                                  boolean prioritized,
                                  Object[] args) throws BlockException;
}
