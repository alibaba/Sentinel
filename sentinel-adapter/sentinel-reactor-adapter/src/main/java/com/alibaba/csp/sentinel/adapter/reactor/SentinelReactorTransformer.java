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

import java.util.function.Function;

import com.alibaba.csp.sentinel.util.AssertUtil;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A transformer that transforms given {@code Publisher} to a wrapped Sentinel reactor operator.
 *
 * @author Eric Zhao
 * @since 1.5.0
 */
public class SentinelReactorTransformer<T> implements Function<Publisher<T>, Publisher<T>> {

    private final EntryConfig entryConfig;

    public SentinelReactorTransformer(String resourceName) {
        this(new EntryConfig(resourceName));
    }

    public SentinelReactorTransformer(EntryConfig entryConfig) {
        AssertUtil.notNull(entryConfig, "entryConfig cannot be null");
        this.entryConfig = entryConfig;
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if (publisher instanceof Mono) {
            return new MonoSentinelOperator<>((Mono<T>) publisher, entryConfig);
        }
        if (publisher instanceof Flux) {
            return new FluxSentinelOperator<>((Flux<T>) publisher, entryConfig);
        }

        throw new IllegalStateException("Publisher type is not supported: " + publisher.getClass().getCanonicalName());
    }
}