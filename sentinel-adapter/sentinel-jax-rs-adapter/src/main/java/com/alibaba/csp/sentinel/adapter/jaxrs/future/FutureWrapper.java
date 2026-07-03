/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.jaxrs.future;

import com.alibaba.csp.sentinel.AsyncEntry;

import java.util.concurrent.*;

/**
 * wrap Future to ensure entry exit
 * @author sea
 */
public class FutureWrapper<V> implements Future<V> {

    AsyncEntry entry;

    Future<V> future;

    public FutureWrapper(AsyncEntry entry, Future<V> future) {
        this.entry = entry;
        this.future = future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            return future.cancel(mayInterruptIfRunning);
        } finally {
            exitEntry();
        }

    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return future.get();
        } finally {
            exitEntry();
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return future.get(timeout, unit);
        } finally {
            exitEntry();
        }
    }

    private void exitEntry() {
        if (entry != null) {
            entry.exit();
        }
    }

}
