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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;

/**
 * <p>
 * Callback registry for {@link StatisticSlot}. Now two kind of callbacks are supported:
 * <ul>
 * <li>{@link ProcessorSlotEntryCallback}: callback for entry (passed and blocked)</li>
 * <li>{@link ProcessorSlotExitCallback}: callback for exiting {@link StatisticSlot}</li>
 * </ul>
 * </p>
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public final class StatisticSlotCallbackRegistry {

    private static final Map<String, ProcessorSlotEntryCallback<DefaultNode>> entryCallbackMap
        = new ConcurrentHashMap<String, ProcessorSlotEntryCallback<DefaultNode>>();

    private static final Map<String, ProcessorSlotExitCallback> exitCallbackMap
        = new ConcurrentHashMap<String, ProcessorSlotExitCallback>();

    public static void clearEntryCallback() {
        entryCallbackMap.clear();
    }

    public static void clearExitCallback() {
        exitCallbackMap.clear();
    }

    public static void addEntryCallback(String key, ProcessorSlotEntryCallback<DefaultNode> callback) {
        entryCallbackMap.put(key, callback);
    }

    public static void addExitCallback(String key, ProcessorSlotExitCallback callback) {
        exitCallbackMap.put(key, callback);
    }

    public static ProcessorSlotEntryCallback<DefaultNode> removeEntryCallback(String key) {
        if (key == null) {
            return null;
        }
        return entryCallbackMap.remove(key);
    }

    public static ProcessorSlotExitCallback removeExitCallback(String key) {
        if (key == null) {
            return null;
        }
        return exitCallbackMap.remove(key);
    }

    public static Collection<ProcessorSlotEntryCallback<DefaultNode>> getEntryCallbacks() {
        return entryCallbackMap.values();
    }

    public static Collection<ProcessorSlotExitCallback> getExitCallbacks() {
        return exitCallbackMap.values();
    }

    private StatisticSlotCallbackRegistry() {}
}
