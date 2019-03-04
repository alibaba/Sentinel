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
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * This class is used to record other exceptions except block exception.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public final class Tracer {

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
        if (e == null || e instanceof BlockException) {
            return;
        }

        Context context = ContextUtil.getContext();
        if (context == null) {
            return;
        }

        DefaultNode curNode = (DefaultNode)context.getCurNode();
        traceExceptionToNode(e, count, curNode);
    }

    /**
     * Trace provided {@link Throwable} and add exception count to current entry in provided context.
     *
     * @param e     exception to record
     * @param count exception count to add
     * @since 1.4.2
     */
    public static void traceContext(Throwable e, int count, Context context) {
        if (e == null || e instanceof BlockException) {
            return;
        }
        if (context == null) {
            return;
        }

        DefaultNode curNode = (DefaultNode)context.getCurNode();
        traceExceptionToNode(e, count, curNode);
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
        if (e == null || e instanceof BlockException) {
            return;
        }
        if (entry == null || entry.getCurNode() == null) {
            return;
        }

        DefaultNode curNode = (DefaultNode)entry.getCurNode();
        traceExceptionToNode(e, count, curNode);
    }

    private static void traceExceptionToNode(Throwable t, int count, DefaultNode curNode) {
        if (curNode == null) {
            return;
        }

        // clusterNode can be null when Constants.ON is false.
        ClusterNode clusterNode = curNode.getClusterNode();
        if (clusterNode == null) {
            return;
        }
        clusterNode.trace(t, count);
    }

    private Tracer() {}
}
