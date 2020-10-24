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
package com.alibaba.csp.sentinel.tracing.opentracing;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

import com.alibaba.csp.sentinel.tracing.core.SentinelTracingConstants;
import com.alibaba.csp.sentinel.tracing.core.SentinelTracingProcessorSlotEntryCallback;

/**
 * Sentinel for OpenTracing ProcessorSlotEntryCallback
 *
 * @author Haojun Ren
 * @since 1.8.1
 */
public class SentinelOpenTracingProcessorSlotEntryCallback extends SentinelTracingProcessorSlotEntryCallback<Span> {
    @Override
    protected Span buildSpan() {
        return GlobalTracer.get().buildSpan(SentinelTracingConstants.SPAN_NAME).start();
    }

    @Override
    protected void outputSpan(Span span, String key, String value) {
        span.setTag(key, value);
    }

    @Override
    protected void finishSpan(Span span) {
        span.finish();
    }
}