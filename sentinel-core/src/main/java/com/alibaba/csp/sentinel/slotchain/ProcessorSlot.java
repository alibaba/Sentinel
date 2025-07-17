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
package com.alibaba.csp.sentinel.slotchain;

import com.alibaba.csp.sentinel.context.Context;

/**
 * A container of some process and ways of notification when the process is finished.
 * 用于封装一个处理过程，并提供处理完成后的通知机制。
 * @author qinan.qn
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 */
public interface ProcessorSlot<T> {

    /**
     * Entrance of this slot.
     *
     * @param context         current {@link Context} 当前请求上下文
     * @param resourceWrapper current resource 资源包装对象，表示被保护的资源
     * @param param           generics parameter, usually is a {@link com.alibaba.csp.sentinel.node.Node} 泛型参数，通常为节点对象
     * @param count           tokens needed 需要的令牌数
     * @param prioritized     whether the entry is prioritized 是否优先级请求
     * @param args            parameters of the original call 原始调用参数
     * @throws Throwable blocked exception or unexpected error
     */
    void entry(Context context, ResourceWrapper resourceWrapper, T param, int count, boolean prioritized,
               Object... args) throws Throwable;

    /**
     * Means finish of {@link #entry(Context, ResourceWrapper, Object, int, boolean, Object...)}.
     *
     * @param context         current {@link Context}
     * @param resourceWrapper current resource
     * @param obj             relevant object (e.g. Node)
     * @param count           tokens needed
     * @param prioritized     whether the entry is prioritized
     * @param args            parameters of the original call
     * @throws Throwable blocked exception or unexpected error
     */
    void fireEntry(Context context, ResourceWrapper resourceWrapper, Object obj, int count, boolean prioritized,
                   Object... args) throws Throwable;

    /**
     * Exit of this slot.
     *
     * @param context         current {@link Context}
     * @param resourceWrapper current resource
     * @param count           tokens needed
     * @param args            parameters of the original call
     */
    void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args);

    /**
     * Means finish of {@link #exit(Context, ResourceWrapper, int, Object...)}.
     *
     * @param context         current {@link Context}
     * @param resourceWrapper current resource
     * @param count           tokens needed
     * @param args            parameters of the original call
     */
    void fireExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args);
}
