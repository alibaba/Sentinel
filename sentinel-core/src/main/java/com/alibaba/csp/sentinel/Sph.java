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
package com.alibaba.csp.sentinel;

import java.lang.reflect.Method;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * The basic interface for recording statistics and performing rule checking for resources.
 *
 * @author qinan.qn
 * @author jialiang.linjl
 * @author leyou
 * @author Eric Zhao
 */
public interface Sph extends SphResourceTypeSupport {

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name the unique name of the protected resource
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(String name) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given method.
     *
     * @param method the protected method
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(Method method) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given method.
     *
     * @param method     the protected method
     * @param batchCount the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(Method method, int batchCount) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name       the unique string for the resource
     * @param batchCount the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(String name, int batchCount) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given method.
     *
     * @param method      the protected method
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(Method method, EntryType trafficType) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(String name, EntryType trafficType) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given method.
     *
     * @param method      the protected method
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(Method method, EntryType trafficType, int batchCount) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(String name, EntryType trafficType, int batchCount) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param method      the protected method
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param args        parameters of the method for flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data).
     * @throws BlockException if the block criteria is met
     */
    Entry entry(Method method, EntryType trafficType, int batchCount, Object... args) throws BlockException;

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param args        args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met
     */
    Entry entry(String name, EntryType trafficType, int batchCount, Object... args) throws BlockException;

    /**
     * Create a protected asynchronous resource.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param args        args for parameter flow control or customized slots
     * @return created asynchronous entry
     * @throws BlockException if the block criteria is met
     * @since 0.2.0
     */
    AsyncEntry asyncEntry(String name, EntryType trafficType, int batchCount, Object... args) throws BlockException;

    /**
     * Create a protected resource with priority.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param prioritized whether the entry is prioritized
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met
     * @since 1.4.0
     */
    Entry entryWithPriority(String name, EntryType trafficType, int batchCount, boolean prioritized)
        throws BlockException;

    /**
     * Create a protected resource with priority.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param prioritized whether the entry is prioritized
     * @param args        args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met
     * @since 1.5.0
     */
    Entry entryWithPriority(String name, EntryType trafficType, int batchCount, boolean prioritized, Object... args)
        throws BlockException;
}
