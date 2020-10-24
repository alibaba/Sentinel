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
package com.alibaba.csp.sentinel.tracing.core;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;

/**
 * Sentinel tracing for ProcessorSlotEntryCallback. It would output a SENTINEL span in the Block callback method  
 *
 * @author Haojun Ren
 * @since 1.8.1
 */
public abstract class SentinelTracingProcessorSlotEntryCallback<S> implements ProcessorSlotEntryCallback<DefaultNode> {
    @Override
    public void onPass(Context context, ResourceWrapper resourceWrapper, DefaultNode param, int count, Object... args) throws Exception {

    }

    @Override
    public void onBlocked(BlockException e, Context context, ResourceWrapper resourceWrapper, DefaultNode param, int count, Object... args) {
        S span = buildSpan();

        outputSpan(span, SentinelTracingConstants.ORIGIN, context.getOrigin());
        outputSpan(span, SentinelTracingConstants.ASYNC, String.valueOf(context.isAsync()));
        outputSpan(span, SentinelTracingConstants.RESOURCE_NAME, resourceWrapper.getName());
        outputSpan(span, SentinelTracingConstants.RESOURCE_SHOW_NAME, resourceWrapper.getShowName());
        outputSpan(span, SentinelTracingConstants.RESOURCE_TYPE, String.valueOf(resourceWrapper.getResourceType()));
        outputSpan(span, SentinelTracingConstants.ENTRY_TYPE, resourceWrapper.getEntryType().toString());
        outputSpan(span, SentinelTracingConstants.RULE_LIMIT_APP, e.getRuleLimitApp());
        outputSpan(span, SentinelTracingConstants.RULE, e.getRule().toString());
        outputSpan(span, SentinelTracingConstants.CAUSE, e.getClass().getName());
        outputSpan(span, SentinelTracingConstants.BLOCK_EXCEPTION, e.getMessage());
        outputSpan(span, SentinelTracingConstants.COUNT, String.valueOf(count));
        outputSpan(span, SentinelTracingConstants.ARGS, JSON.toJSONString(args));

        finishSpan(span);
    }

    protected abstract S buildSpan();

    protected abstract void outputSpan(S span, String key, String value);

    protected abstract void finishSpan(S span);
}