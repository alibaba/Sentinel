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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Supplier;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

/**
 * @author Eric Zhao
 * @since 1.5.0
 */
public class SentinelReactorSubscriber<T> extends InheritableBaseSubscriber<T> {

    private final EntryConfig entryConfig;

    private final CoreSubscriber<? super T> actual;
    private final boolean unary;

    private volatile AsyncEntry currentEntry;
    private final AtomicBoolean entryExited = new AtomicBoolean(false);

    public SentinelReactorSubscriber(EntryConfig entryConfig,
                                     CoreSubscriber<? super T> actual,
                                     boolean unary) {
        checkEntryConfig(entryConfig);
        this.entryConfig = entryConfig;
        this.actual = actual;
        this.unary = unary;
    }

    private void checkEntryConfig(EntryConfig config) {
        AssertUtil.notNull(config, "entryConfig cannot be null");
    }

    @Override
    public Context currentContext() {
        if (currentEntry == null || entryExited.get()) {
            return actual.currentContext();
        }
        com.alibaba.csp.sentinel.context.Context sentinelContext = currentEntry.getAsyncContext();
        if (sentinelContext == null) {
            return actual.currentContext();
        }
        return actual.currentContext()
            .put(SentinelReactorConstants.SENTINEL_CONTEXT_KEY, currentEntry.getAsyncContext());
    }

    private void doWithContextOrCurrent(Supplier<Optional<com.alibaba.csp.sentinel.context.Context>> contextSupplier,
                                        Runnable f) {
        Optional<com.alibaba.csp.sentinel.context.Context> contextOpt = contextSupplier.get();
        if (!contextOpt.isPresent()) {
            // Provided context is absent, use current context.
            f.run();
        } else {
            // Run on provided context.
            ContextUtil.runOnContext(contextOpt.get(), f);
        }
    }

    private void entryWhenSubscribed() {
        ContextConfig sentinelContextConfig = entryConfig.getContextConfig();
        if (sentinelContextConfig != null) {
            // If current we're already in a context, the context config won't work.
            ContextUtil.enter(sentinelContextConfig.getContextName(), sentinelContextConfig.getOrigin());
        }
        try {
            AsyncEntry entry = SphU.asyncEntry(entryConfig.getResourceName(), entryConfig.getEntryType(),
                entryConfig.getAcquireCount(), entryConfig.getArgs());
            this.currentEntry = entry;
            actual.onSubscribe(this);
        } catch (BlockException ex) {
            // Mark as completed (exited) explicitly.
            entryExited.set(true);
            // Signal cancel and propagate the {@code BlockException}.
            cancel();
            actual.onSubscribe(this);
            actual.onError(ex);
        } finally {
            if (sentinelContextConfig != null) {
                ContextUtil.exit();
            }
        }
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        doWithContextOrCurrent(() -> currentContext().getOrEmpty(SentinelReactorConstants.SENTINEL_CONTEXT_KEY),
            this::entryWhenSubscribed);
    }

    @Override
    protected void hookOnNext(T value) {
        if (isDisposed()) {
            tryCompleteEntry();
            return;
        }
        doWithContextOrCurrent(() -> Optional.ofNullable(currentEntry).map(AsyncEntry::getAsyncContext),
            () -> actual.onNext(value));

        if (unary) {
            // For some cases of unary operator (Mono), we have to do this during onNext hook.
            // e.g. this kind of order: onSubscribe() -> onNext() -> cancel() -> onComplete()
            // the onComplete hook will not be executed so we'll need to complete the entry in advance.
            tryCompleteEntry();
        }
    }

    @Override
    protected void hookOnComplete() {
        tryCompleteEntry();
        actual.onComplete();
    }

    @Override
    protected boolean shouldCallErrorDropHook() {
        // When flow control triggered or stream terminated, the incoming
        // deprecated exceptions should be dropped implicitly, so we'll not call the `onErrorDropped` hook.
        return !entryExited.get();
    }

    @Override
    protected void hookOnError(Throwable t) {
        if (currentEntry != null && currentEntry.getAsyncContext() != null) {
            // Normal requests with non-BlockException will go through here.
            Tracer.traceContext(t, 1, currentEntry.getAsyncContext());
        }
        tryCompleteEntry();
        actual.onError(t);
    }

    @Override
    protected void hookOnCancel() {

    }

    private boolean tryCompleteEntry() {
        if (currentEntry != null && entryExited.compareAndSet(false, true)) {
            currentEntry.exit(1, entryConfig.getArgs());
            return true;
        }
        return false;
    }
}
