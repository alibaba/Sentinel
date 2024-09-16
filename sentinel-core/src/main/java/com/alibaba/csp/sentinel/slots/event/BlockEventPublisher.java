/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.event;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.event.inte.BlockEventHandler;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Ops to publish event.
 *
 * @author Daydreamer-ia
 */
public class BlockEventPublisher {

    private static List<BlockEventHandler> blockEventHandlers = new ArrayList<>();

    static {
        List<BlockEventHandler> blockEventHandlers = SpiLoader.of(BlockEventHandler.class).loadInstanceList();
        BlockEventPublisher.blockEventHandlers = new ArrayList<>(blockEventHandlers);
    }

    /**
     * publish block event to listeners.
     *
     * @param context         context
     * @param resourceWrapper resource
     * @param blockException  blockException
     */
    public static void publishBlockEvent(Context context, ResourceWrapper resourceWrapper, BlockException blockException) {
        for (BlockEventHandler blockEventHandler : blockEventHandlers) {
            blockEventHandler.publish(blockException, resourceWrapper, context);
        }
    }

}
