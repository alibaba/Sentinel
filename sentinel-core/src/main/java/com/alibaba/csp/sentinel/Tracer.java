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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionProvider;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * This class is used to record other exceptions except block exception.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class Tracer {

    protected static Class<? extends Throwable>[] traceClasses;
    protected static Class<? extends Throwable>[] ignoreClasses;

    protected Tracer() {}

    /**
     * Trace provided {@link Throwable} and increment exception count to entry in current context.
     *
     * @param e exception to record
     */
    public static void trace(Throwable e) {
        trace(e, 1);
    }

    /**
     * Trace provided {@link Throwable} and add exception count to entry in current context.
     *
     * @param e     exception to record
     * @param count exception count to add
     */
    public static void trace(Throwable e, int count) {
        if (!shouldTrace(e)) {
            return;
        }

        Context context = ContextUtil.getContext();
        if (context == null) {
            return;
        }

        DefaultNode curNode = (DefaultNode)context.getCurNode();
        traceExceptionToNode(e, count, context.getCurEntry(), curNode);
    }

    /**
     * Trace provided {@link Throwable} and add exception count to current entry in provided context.
     *
     * @param e     exception to record
     * @param count exception count to add
     * @since 1.4.2
     */
    public static void traceContext(Throwable e, int count, Context context) {
        if (!shouldTrace(e)) {
            return;
        }
        if (context == null) {
            return;
        }

        DefaultNode curNode = (DefaultNode)context.getCurNode();
        traceExceptionToNode(e, count, context.getCurEntry(), curNode);
    }

    /**
     * Trace provided {@link Throwable} and increment exception count to provided entry.
     *
     * @param e exception to record
     * @since 1.4.2
     */
    public static void traceEntry(Throwable e, Entry entry) {
        traceEntry(e, 1, entry);
    }

    /**
     * Trace provided {@link Throwable} and add exception count to provided entry.
     *
     * @param e     exception to record
     * @param count exception count to add
     * @since 1.4.2
     */
    public static void traceEntry(Throwable e, int count, Entry entry) {
        if (!shouldTrace(e)) {
            return;
        }
        if (entry == null || entry.getCurNode() == null) {
            return;
        }

        DefaultNode curNode = (DefaultNode)entry.getCurNode();
        traceExceptionToNode(e, count, entry, curNode);
    }

    private static void traceExceptionToNode(Throwable t, int count, Entry entry, DefaultNode curNode) {
        if (curNode == null) {
            return;
        }
        for (MetricExtension m : MetricExtensionProvider.getMetricExtensions()) {
            m.addException(entry.getResourceWrapper().getName(), count, t);
        }

        // clusterNode can be null when Constants.ON is false.
        ClusterNode clusterNode = curNode.getClusterNode();
        if (clusterNode == null) {
            return;
        }
        clusterNode.trace(t, count);
    }

    /**
     * Set exception to trace. If not set, all Exception except for {@link BlockException} will be traced.
     * <p>
     * Note that if both {@link #setExceptionsToIgnore(Class[])} and this method is set,
     * the ExceptionsToIgnore will be of higher precedence.
     * </p>
     *
     * @param traceClasses the list of exception classes to trace.
     * @since 1.6.1
     */
    @SafeVarargs
    public static void setExceptionsToTrace(Class<? extends Throwable>... traceClasses) {
        checkNotNull(traceClasses);
        Tracer.traceClasses = traceClasses;
    }

    /**
     * Get exception classes to trace.
     *
     * @return an array of exception classes to trace.
     * @since 1.6.1
     */
    public static Class<? extends Throwable>[] getExceptionsToTrace() {
        return traceClasses;
    }

    /**
     * Set exceptions to ignore. if not set, all Exception except for {@link BlockException} will be traced.
     * <p>
     * Note that if both {@link #setExceptionsToTrace(Class[])} and this method is set,
     * the ExceptionsToIgnore will be of higher precedence.
     * </p>
     *
     * @param ignoreClasses the list of exception classes to ignore.
     * @since 1.6.1
     */
    @SafeVarargs
    public static void setExceptionsToIgnore(Class<? extends Throwable>... ignoreClasses) {
        checkNotNull(ignoreClasses);
        Tracer.ignoreClasses = ignoreClasses;
    }

    /**
     * Get exception classes to ignore.
     *
     * @return an array of exception classes to ignore.
     * @since 1.6.1
     */
    public static Class<? extends Throwable>[] getExceptionsToIgnore() {
        return ignoreClasses;
    }

    private static void checkNotNull(Class<? extends Throwable>[] classes) {
        AssertUtil.notNull(classes, "trace or ignore classes must not be null");
        for (Class<? extends Throwable> clazz : classes) {
            AssertUtil.notNull(clazz, "trace or ignore classes must not be null");
        }
    }

    /**
     * Check whether the throwable should be traced.
     *
     * @param t the throwable to check.
     * @return true if the throwable should be traced, else return false.
     */
    protected static boolean shouldTrace(Throwable t) {
        if (t == null || t instanceof BlockException) {
            return false;
        }
        if (ignoreClasses != null) {
            for (Class<? extends Throwable> clazz : ignoreClasses) {
                if (clazz != null && clazz.isAssignableFrom(t.getClass())) {
                    return false;
                }
            }
        }
        if (traceClasses != null) {
            for (Class<? extends Throwable> clazz : traceClasses) {
                if (clazz != null && clazz.isAssignableFrom(t.getClass())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
