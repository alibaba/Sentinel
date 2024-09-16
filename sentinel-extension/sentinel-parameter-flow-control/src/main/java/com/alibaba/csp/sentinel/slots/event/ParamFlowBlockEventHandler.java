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
import com.alibaba.csp.sentinel.event.SentinelEventBus;
import com.alibaba.csp.sentinel.event.model.ParamFlowBlockEvent;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.event.inte.BlockEventHandler;

/**
 * Used to publish param flow block event.
 *
 * @author Daydreamer-ia
 */
public class ParamFlowBlockEventHandler implements BlockEventHandler {

    @Override
    public void publish(BlockException blockException, ResourceWrapper resourceWrapper, Context context) {
        if (blockException instanceof ParamFlowException) {
            ParamFlowException paramFlowException = (ParamFlowException) blockException;
            SentinelEventBus.getInstance().publish(new ParamFlowBlockEvent(resourceWrapper.getName(), paramFlowException.getRule()));
        }
    }
}
