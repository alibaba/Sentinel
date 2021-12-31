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
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * <p>The fundamental Sentinel API for recording statistics and performing rule checking for resources.</p>
 * <p>
 * Conceptually, physical or logical resource that need protection should be
 * surrounded by an entry. The requests to this resource will be blocked if any
 * criteria is met, eg. when any {@link Rule}'s threshold is exceeded. Once blocked,
 * a {@link BlockException} will be thrown.
 * </p>
 * <p>
 * To configure the criteria, we can use <code>XxxRuleManager.loadRules()</code> to load rules.
 * </p>
 *
 * <p>
 * Following code is an example, {@code "abc"} represent a unique name for the
 * protected resource:
 * </p>
 *
 * <pre>
 *  public void foo() {
 *     Entry entry = null;
 *     try {
 *        entry = SphU.entry("abc");
 *        // resource that need protection
 *     } catch (BlockException blockException) {
 *         // when goes there, it is blocked
 *         // add blocked handle logic here
 *     } catch (Throwable bizException) {
 *         // business exception
 *         Tracer.trace(bizException);
 *     } finally {
 *         // ensure finally be executed
 *         if (entry != null){
 *             entry.exit();
 *         }
 *     }
 *  }
 * </pre>
 *
 * <p>
 * Make sure {@code SphU.entry()} and {@link Entry#exit()} be paired in the same thread,
 * otherwise {@link ErrorEntryFreeException} will be thrown.
 * </p>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @see SphO
 */
public class SphU {

    private static final Object[] OBJECTS0 = new Object[0];

    private SphU() {}

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name the unique name of the protected resource
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name) throws BlockException {
        return Env.sph.entry(name, EntryType.OUT, 1, OBJECTS0);
    }

    /**
     * Checking all {@link Rule}s about the protected method.
     *
     * @param method the protected method
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method) throws BlockException {
        return Env.sph.entry(method, EntryType.OUT, 1, OBJECTS0);
    }

    /**
     * Checking all {@link Rule}s about the protected method.
     *
     * @param method     the protected method
     * @param batchCount the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method, int batchCount) throws BlockException {
        return Env.sph.entry(method, EntryType.OUT, batchCount, OBJECTS0);
    }

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name       the unique string for the resource
     * @param batchCount the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name, int batchCount) throws BlockException {
        return Env.sph.entry(name, EntryType.OUT, batchCount, OBJECTS0);
    }

    /**
     * Checking all {@link Rule}s about the protected method.
     *
     * @param method      the protected method
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method, EntryType trafficType) throws BlockException {
        return Env.sph.entry(method, trafficType, 1, OBJECTS0);
    }

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name, EntryType trafficType) throws BlockException {
        return Env.sph.entry(name, trafficType, 1, OBJECTS0);
    }

    /**
     * Checking all {@link Rule}s about the protected method.
     *
     * @param method      the protected method
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method, EntryType trafficType, int batchCount) throws BlockException {
        return Env.sph.entry(method, trafficType, batchCount, OBJECTS0);
    }

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name, EntryType trafficType, int batchCount) throws BlockException {
        return Env.sph.entry(name, trafficType, batchCount, OBJECTS0);
    }

    /**
     * Checking all {@link Rule}s about the protected method.
     *
     * @param method      the protected method
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param args        args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(Method method, EntryType trafficType, int batchCount, Object... args)
        throws BlockException {
        return Env.sph.entry(method, trafficType, batchCount, args);
    }

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param args        args for parameter flow control
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     */
    public static Entry entry(String name, EntryType trafficType, int batchCount, Object... args)
        throws BlockException {
        return Env.sph.entry(name, trafficType, batchCount, args);
    }

    /**
     * Record statistics and check all rules of the resource that indicates an async invocation.
     *
     * @param name the unique name of the protected resource
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 0.2.0
     */
    public static AsyncEntry asyncEntry(String name) throws BlockException {
        return Env.sph.asyncEntry(name, EntryType.OUT, 1, OBJECTS0);
    }

    /**
     * Record statistics and check all rules of the resource that indicates an async invocation.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 0.2.0
     */
    public static AsyncEntry asyncEntry(String name, EntryType trafficType) throws BlockException {
        return Env.sph.asyncEntry(name, trafficType, 1, OBJECTS0);
    }

    /**
     * Record statistics and check all rules of the resource that indicates an async invocation.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @param batchCount  the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param args        args for parameter flow control
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 0.2.0
     */
    public static AsyncEntry asyncEntry(String name, EntryType trafficType, int batchCount, Object... args)
        throws BlockException {
        return Env.sph.asyncEntry(name, trafficType, batchCount, args);
    }

    /**
     * Record statistics and perform rule checking for the given resource. The entry is prioritized.
     *
     * @param name the unique name for the protected resource
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 1.4.0
     */
    public static Entry entryWithPriority(String name) throws BlockException {
        return Env.sph.entryWithPriority(name, EntryType.OUT, 1, true);
    }

    /**
     * Record statistics and perform rule checking for the given resource. The entry is prioritized.
     *
     * @param name        the unique name for the protected resource
     * @param trafficType the traffic type (inbound, outbound or internal). This is used
     *                    to mark whether it can be blocked when the system is unstable,
     *                    only inbound traffic could be blocked by {@link SystemRule}
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 1.4.0
     */
    public static Entry entryWithPriority(String name, EntryType trafficType) throws BlockException {
        return Env.sph.entryWithPriority(name, trafficType, 1, true);
    }

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name         the unique name for the protected resource
     * @param resourceType classification of the resource (e.g. Web or RPC)
     * @param trafficType  the traffic type (inbound, outbound or internal). This is used
     *                     to mark whether it can be blocked when the system is unstable,
     *                     only inbound traffic could be blocked by {@link SystemRule}
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static Entry entry(String name, int resourceType, EntryType trafficType) throws BlockException {
        return Env.sph.entryWithType(name, resourceType, trafficType, 1, OBJECTS0);
    }

    /**
     * Record statistics and perform rule checking for the given resource.
     *
     * @param name         the unique name for the protected resource
     * @param trafficType  the traffic type (inbound, outbound or internal). This is used
     *                     to mark whether it can be blocked when the system is unstable,
     *                     only inbound traffic could be blocked by {@link SystemRule}
     * @param resourceType classification of the resource (e.g. Web or RPC)
     * @param args         args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static Entry entry(String name, int resourceType, EntryType trafficType, Object[] args)
        throws BlockException {
        return Env.sph.entryWithType(name, resourceType, trafficType, 1, args);
    }

    /**
     * Record statistics and perform rule checking for the given resource that indicates an async invocation.
     *
     * @param name         the unique name for the protected resource
     * @param trafficType  the traffic type (inbound, outbound or internal). This is used
     *                     to mark whether it can be blocked when the system is unstable,
     *                     only inbound traffic could be blocked by {@link SystemRule}
     * @param resourceType classification of the resource (e.g. Web or RPC)
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static AsyncEntry asyncEntry(String name, int resourceType, EntryType trafficType)
        throws BlockException {
        return Env.sph.asyncEntryWithType(name, resourceType, trafficType, 1, false, OBJECTS0);
    }

    /**
     * Record statistics and perform rule checking for the given resource that indicates an async invocation.
     *
     * @param name         the unique name for the protected resource
     * @param trafficType  the traffic type (inbound, outbound or internal). This is used
     *                     to mark whether it can be blocked when the system is unstable,
     *                     only inbound traffic could be blocked by {@link SystemRule}
     * @param resourceType classification of the resource (e.g. Web or RPC)
     * @param args         args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static AsyncEntry asyncEntry(String name, int resourceType, EntryType trafficType, Object[] args)
        throws BlockException {
        return Env.sph.asyncEntryWithType(name, resourceType, trafficType, 1, false, args);
    }

    /**
     * Record statistics and perform rule checking for the given resource that indicates an async invocation.
     *
     * @param name         the unique name for the protected resource
     * @param trafficType  the traffic type (inbound, outbound or internal). This is used
     *                     to mark whether it can be blocked when the system is unstable,
     *                     only inbound traffic could be blocked by {@link SystemRule}
     * @param resourceType classification of the resource (e.g. Web or RPC)
     * @param batchCount   the amount of calls within the invocation (e.g. batchCount=2 means request for 2 tokens)
     * @param args         args for parameter flow control or customized slots
     * @return the {@link Entry} of this invocation (used for mark the invocation complete and get context data)
     * @throws BlockException if the block criteria is met (e.g. metric exceeded the threshold of any rules)
     * @since 1.7.0
     */
    public static AsyncEntry asyncEntry(String name, int resourceType, EntryType trafficType, int batchCount,
                                        Object[] args) throws BlockException {
        return Env.sph.asyncEntryWithType(name, resourceType, trafficType, batchCount, false, args);
    }
}
