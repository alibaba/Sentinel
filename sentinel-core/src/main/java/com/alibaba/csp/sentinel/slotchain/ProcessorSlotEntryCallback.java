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
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Callback for entering {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot} (passed and blocked).
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public interface ProcessorSlotEntryCallback<T> {

    void onPass(Context context, ResourceWrapper resourceWrapper, T param, int count, Object... args) throws Exception;

    void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, T param, int count, Object... args);
}
