/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.reactor;

import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import reactor.core.publisher.Mono;

/**
 * A {@link SphU} adapter with Project Reactor.
 *
 * @author Eric Zhao
 * @since 1.5.0
 */
public final class ReactorSphU {

    public static <R> Mono<R> entryWith(String resourceName, Mono<R> actual) {
        return entryWith(resourceName, EntryType.OUT, actual);
    }

    public static <R> Mono<R> entryWith(String resourceName, EntryType entryType, Mono<R> actual) {
        final AtomicReference<AsyncEntry> entryWrapper = new AtomicReference<>(null);
        return Mono.defer(() -> {
            try {
                AsyncEntry entry = SphU.asyncEntry(resourceName, entryType);
                entryWrapper.set(entry);
                return actual.subscriberContext(context -> {
                    if (entry == null) {
                        return context;
                    }
                    Context sentinelContext = entry.getAsyncContext();
                    if (sentinelContext == null) {
                        return context;
                    }
                    // TODO: check GC friendly?
                    return context.put(SentinelReactorConstants.SENTINEL_CONTEXT_KEY, sentinelContext);
                }).doOnSuccessOrError((o, t) -> {
                    if (entry != null && entryWrapper.compareAndSet(entry, null)) {
                        if (t != null) {
                            Tracer.traceContext(t, 1, entry.getAsyncContext());
                        }
                        entry.exit();
                    }
                });
            } catch (BlockException ex) {
                return Mono.error(ex);
            }
        });
    }

    private ReactorSphU() {}
}
