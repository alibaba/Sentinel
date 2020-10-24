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
package com.alibaba.csp.sentinel.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;

/**
 * Utility class to get or create {@link Context} in current thread.
 *
 * <p>
 * Each {@link SphU}#entry() or {@link SphO}#entry() should be in a {@link Context}.
 * If we don't invoke {@link ContextUtil}#enter() explicitly, DEFAULT context will be used.
 * </p>
 *
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 */
public class ContextUtil {

    /**
     * Store the context in ThreadLocal for easy access.
     */
    private static ThreadLocal<Context> contextHolder = new ThreadLocal<>();

    /**
     * Holds all {@link EntranceNode}. Each {@link EntranceNode} is associated with a distinct context name.
     */
    private static volatile Map<String, DefaultNode> contextNameNodeMap = new HashMap<>();

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Context NULL_CONTEXT = new NullContext();

    static {
        // Cache the entrance node for default context.
        initDefaultContext();
    }

    private static void initDefaultContext() {
        String defaultContextName = Constants.CONTEXT_DEFAULT_NAME;
        EntranceNode node = new EntranceNode(new StringResourceWrapper(defaultContextName, EntryType.IN), null);
        Constants.ROOT.addChild(node);
        contextNameNodeMap.put(defaultContextName, node);
    }

    /**
     * Not thread-safe, only for test.
     */
    static void resetContextMap() {
        if (contextNameNodeMap != null) {
            RecordLog.warn("Context map cleared and reset to initial state");
            contextNameNodeMap.clear();
            initDefaultContext();
        }
    }

    /**
     * <p>
     * Enter the invocation context, which marks as the entrance of an invocation chain.
     * The context is wrapped with {@code ThreadLocal}, meaning that each thread has it's own {@link Context}.
     * New context will be created if current thread doesn't have one.
     * </p>
     * <p>
     * A context will be bound with an {@link EntranceNode}, which represents the entrance statistic node
     * of the invocation chain. New {@link EntranceNode} will be created if
     * current context does't have one. Note that same context name will share
     * same {@link EntranceNode} globally.
     * </p>
     * <p>
     * The origin node will be created in {@link com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot}.
     * Note that each distinct {@code origin} of different resources will lead to creating different new
     * {@link Node}, meaning that total amount of created origin statistic nodes will be:<br/>
     * {@code distinct resource name amount * distinct origin count}.<br/>
     * So when there are too many origins, memory footprint should be carefully considered.
     * </p>
     * <p>
     * Same resource in different context will count separately, see {@link NodeSelectorSlot}.
     * </p>
     *
     * @param name   the context name
     * @param origin the origin of this invocation, usually the origin could be the Service
     *               Consumer's app name. The origin is useful when we want to control different
     *               invoker/consumer separately.
     * @return The invocation context of the current thread
     */
    public static Context enter(String name, String origin) {
        if (Constants.CONTEXT_DEFAULT_NAME.equals(name)) {
            throw new ContextNameDefineException(
                "The " + Constants.CONTEXT_DEFAULT_NAME + " can't be permit to defined!");
        }
        return trueEnter(name, origin);
    }

    protected static Context trueEnter(String name, String origin) {
        Context context = contextHolder.get();
        if (context == null) {
            Map<String, DefaultNode> localCacheNameMap = contextNameNodeMap;
            DefaultNode node = localCacheNameMap.get(name);
            if (node == null) {
                if (localCacheNameMap.size() > Constants.MAX_CONTEXT_NAME_SIZE) {
                    setNullContext();
                    return NULL_CONTEXT;
                } else {
                    LOCK.lock();
                    try {
                        node = contextNameNodeMap.get(name);
                        if (node == null) {
                            if (contextNameNodeMap.size() > Constants.MAX_CONTEXT_NAME_SIZE) {
                                setNullContext();
                                return NULL_CONTEXT;
                            } else {
                                node = new EntranceNode(new StringResourceWrapper(name, EntryType.IN), null);
                                // Add entrance node.
                                Constants.ROOT.addChild(node);

                                Map<String, DefaultNode> newMap = new HashMap<>(contextNameNodeMap.size() + 1);
                                newMap.putAll(contextNameNodeMap);
                                newMap.put(name, node);
                                contextNameNodeMap = newMap;
                            }
                        }
                    } finally {
                        LOCK.unlock();
                    }
                }
            }
            context = new Context(node, name);
            context.setOrigin(origin);
            contextHolder.set(context);
        }

        return context;
    }

    private static boolean shouldWarn = true;

    private static void setNullContext() {
        contextHolder.set(NULL_CONTEXT);
        // Don't need to be thread-safe.
        if (shouldWarn) {
            RecordLog.warn("[SentinelStatusChecker] WARN: Amount of context exceeds the threshold "
                + Constants.MAX_CONTEXT_NAME_SIZE + ". Entries in new contexts will NOT take effect!");
            shouldWarn = false;
        }
    }

    /**
     * <p>
     * Enter the invocation context, which marks as the entrance of an invocation chain.
     * The context is wrapped with {@code ThreadLocal}, meaning that each thread has it's own {@link Context}.
     * New context will be created if current thread doesn't have one.
     * </p>
     * <p>
     * A context will be bound with an {@link EntranceNode}, which represents the entrance statistic node
     * of the invocation chain. New {@link EntranceNode} will be created if
     * current context does't have one. Note that same context name will share
     * same {@link EntranceNode} globally.
     * </p>
     * <p>
     * Same resource in different context will count separately, see {@link NodeSelectorSlot}.
     * </p>
     *
     * @param name the context name
     * @return The invocation context of the current thread
     */
    public static Context enter(String name) {
        return enter(name, "");
    }

    /**
     * Exit context of current thread, that is removing {@link Context} in the
     * ThreadLocal.
     */
    public static void exit() {
        Context context = contextHolder.get();
        if (context != null && context.getCurEntry() == null) {
            contextHolder.set(null);
        }
    }

    /**
     * Get current size of context entrance node map.
     *
     * @return current size of context entrance node map
     * @since 0.2.0
     */
    public static int contextSize() {
        return contextNameNodeMap.size();
    }

    /**
     * Check if provided context is a default auto-created context.
     *
     * @param context context to check
     * @return true if it is a default context, otherwise false
     * @since 0.2.0
     */
    public static boolean isDefaultContext(Context context) {
        if (context == null) {
            return false;
        }
        return Constants.CONTEXT_DEFAULT_NAME.equals(context.getName());
    }

    /**
     * Get {@link Context} of current thread.
     *
     * @return context of current thread. Null value will be return if current
     * thread does't have context.
     */
    public static Context getContext() {
        return contextHolder.get();
    }

    /**
     * <p>
     * Replace current context with the provided context.
     * This is mainly designed for context switching (e.g. in asynchronous invocation).
     * </p>
     * <p>
     * Note: When switching context manually, remember to restore the original context.
     * For common scenarios, you can use {@link #runOnContext(Context, Runnable)}.
     * </p>
     *
     * @param newContext new context to set
     * @return old context
     * @since 0.2.0
     */
    static Context replaceContext(Context newContext) {
        Context backupContext = contextHolder.get();
        if (newContext == null) {
            contextHolder.remove();
        } else {
            contextHolder.set(newContext);
        }
        return backupContext;
    }

    /**
     * Execute the code within provided context.
     * This is mainly designed for context switching (e.g. in asynchronous invocation).
     *
     * @param context the context
     * @param f       lambda to run within the context
     * @since 0.2.0
     */
    public static void runOnContext(Context context, Runnable f) {
        Context curContext = replaceContext(context);
        try {
            f.run();
        } finally {
            replaceContext(curContext);
        }
    }
}
