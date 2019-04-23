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

import com.alibaba.csp.sentinel.util.AssertUtil;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

/**
 * @author Eric Zhao
 * @since 1.5.0
 */
public class FluxSentinelOperator<T> extends FluxOperator<T, T> {

    private final EntryConfig entryConfig;

    public FluxSentinelOperator(Flux<? extends T> source, EntryConfig entryConfig) {
        super(source);
        AssertUtil.notNull(entryConfig, "entryConfig cannot be null");
        this.entryConfig = entryConfig;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        source.subscribe(new SentinelReactorSubscriber<>(entryConfig, actual, false));
    }
}
