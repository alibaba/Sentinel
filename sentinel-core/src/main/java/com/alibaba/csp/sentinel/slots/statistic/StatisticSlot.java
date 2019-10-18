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
package com.alibaba.csp.sentinel.slots.statistic;

import java.util.Collection;

import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slots.block.flow.PriorityWaitException;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * <p>
 * A processor slot that dedicates to real time statistics.
 * When entering this slot, we need to separately count the following
 * information:
 * <ul>
 * <li>{@link ClusterNode}: total statistics of a cluster node of the resource ID.</li>
 * <li>Origin node: statistics of a cluster node from different callers/origins.</li>
 * <li>{@link DefaultNode}: statistics for specific resource name in the specific context.</li>
 * <li>Finally, the sum statistics of all entrances.</li>
 * </ul>
 * </p>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class StatisticSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    /**
     * StatisticSlot在责任链中的调用入口
     * 在demo中调用SphO.entry进入获取token逻辑
     * 通过前面的Slot后到达这里
     * @param context         current {@link Context}
     * @param resourceWrapper current resource
     * @param node
     * @param count           tokens needed
     * @param prioritized     whether the entry is prioritized
     * @param args            parameters of the original call
     * @throws Throwable
     */
    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        try {
            // Do some checking.
            // 直接触发下游的slot entry操作
            fireEntry(context, resourceWrapper, node, count, prioritized, args);

            // Request passed, add thread count and pass count.
            // 如果到达这里说明没有被限流或降级，可以继续操作
            // 增加访问资源的线程数
            node.increaseThreadNum();
            // 增加pass数
            node.addPassRequest(count);

            // 如果在调用entry之前指定了调用的origin，即调用方
            if (context.getCurEntry().getOriginNode() != null) {
                // Add count for origin node.
                // 则会有一个originNode，我们也需要做上面两个增加操作
                // 方便针对调用方的统计，为后续的限流做准备
                context.getCurEntry().getOriginNode().increaseThreadNum();
                context.getCurEntry().getOriginNode().addPassRequest(count);
            }

            // 入站流量的全局统计节点。通常用于SystemRule检查。
            if (resourceWrapper.getType() == EntryType.IN) {
                // Add count for global inbound entry node for global statistics.
                Constants.ENTRY_NODE.increaseThreadNum();
                Constants.ENTRY_NODE.addPassRequest(count);
            }

            // Handle pass event with registered entry callback handlers.
            // metric扩展回调onPass方法调用，默认是空
            for (ProcessorSlotEntryCallback<DefaultNode> handler : StatisticSlotCallbackRegistry.getEntryCallbacks()) {
                handler.onPass(context, resourceWrapper, node, count, args);
            }
        } catch (PriorityWaitException ex) {
            // 捕获优先级等待异常PriorityWaitException，则仅增加线程数
            node.increaseThreadNum();
            if (context.getCurEntry().getOriginNode() != null) {
                // Add count for origin node.
                context.getCurEntry().getOriginNode().increaseThreadNum();
            }

            if (resourceWrapper.getType() == EntryType.IN) {
                // Add count for global inbound entry node for global statistics.
                Constants.ENTRY_NODE.increaseThreadNum();
            }
            // Handle pass event with registered entry callback handlers.
            for (ProcessorSlotEntryCallback<DefaultNode> handler : StatisticSlotCallbackRegistry.getEntryCallbacks()) {
                handler.onPass(context, resourceWrapper, node, count, args);
            }
        } catch (BlockException e) {
            // Blocked, set block exception to current entry.
            context.getCurEntry().setError(e);

            // 如果触发了BlockException，则说明获取token失败，被限流
            // 因此增加当前秒Block的请求数
            // Add block count.
            node.increaseBlockQps(count);
            if (context.getCurEntry().getOriginNode() != null) {
                context.getCurEntry().getOriginNode().increaseBlockQps(count);
            }
            // 入站流量的全局统计节点。通常用于SystemRule检查。
            if (resourceWrapper.getType() == EntryType.IN) {
                // Add count for global inbound entry node for global statistics.
                Constants.ENTRY_NODE.increaseBlockQps(count);
            }

            // metric扩展回调onBlocked方法调用，默认是空
            // Handle block event with registered entry callback handlers.
            for (ProcessorSlotEntryCallback<DefaultNode> handler : StatisticSlotCallbackRegistry.getEntryCallbacks()) {
                handler.onBlocked(e, context, resourceWrapper, node, count, args);
            }

            throw e;
        } catch (Throwable e) {
            // Unexpected error, set error to current entry.
            context.getCurEntry().setError(e);

            // 如果触发了exception
            // 增加这个请求当前秒Exception的数目
            // This should not happen.
            node.increaseExceptionQps(count);
            if (context.getCurEntry().getOriginNode() != null) {
                context.getCurEntry().getOriginNode().increaseExceptionQps(count);
            }

            if (resourceWrapper.getType() == EntryType.IN) {
                Constants.ENTRY_NODE.increaseExceptionQps(count);
            }
            throw e;
        }
    }

    /**
     * 调用SphO.exit进入
     * 通过前面的Slot后到达这里
     * @param context         current {@link Context}
     * @param resourceWrapper current resource
     * @param count           tokens needed
     * @param args            parameters of the original call
     */
    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        DefaultNode node = (DefaultNode)context.getCurNode();

        if (context.getCurEntry().getError() == null) {
            // Calculate response time (max RT is TIME_DROP_VALVE).
            // 计算响应时间（最大RT为TIME_DROP_VALVE）
            long rt = TimeUtil.currentTimeMillis() - context.getCurEntry().getCreateTime();
            if (rt > Constants.TIME_DROP_VALVE) {
                rt = Constants.TIME_DROP_VALVE;
            }

            // Record response time and success count.
            // 记录响应时间和增加成功数
            node.addRtAndSuccess(rt, count);
            // 按调用方记录响应时间和增加成功数
            if (context.getCurEntry().getOriginNode() != null) {
                context.getCurEntry().getOriginNode().addRtAndSuccess(rt, count);
            }

            // 减少当前资源的线程数
            node.decreaseThreadNum();
            // 按调用方减少资源的线程数
            if (context.getCurEntry().getOriginNode() != null) {
                context.getCurEntry().getOriginNode().decreaseThreadNum();
            }

            // 全局统计节点记录
            if (resourceWrapper.getType() == EntryType.IN) {
                Constants.ENTRY_NODE.addRtAndSuccess(rt, count);
                Constants.ENTRY_NODE.decreaseThreadNum();
            }
        } else {
            // Error may happen.
        }

        // Handle exit event with registered exit callback handlers.
        // 使用注册的退出回调处理程序处理退出事件。
        // 钩子函数拓展使用，metric扩展回调onPass方法调用，默认是空
        Collection<ProcessorSlotExitCallback> exitCallbacks = StatisticSlotCallbackRegistry.getExitCallbacks();
        for (ProcessorSlotExitCallback handler : exitCallbacks) {
            handler.onExit(context, resourceWrapper, count, args);
        }
        // 调用下游的slot exit方法
        fireExit(context, resourceWrapper, count);
    }
}
