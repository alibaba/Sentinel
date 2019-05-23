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
package com.alibaba.csp.sentinel.adapter.spring.webflux;

import java.util.Optional;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.adapter.reactor.ContextConfig;
import com.alibaba.csp.sentinel.adapter.reactor.EntryConfig;
import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author Eric Zhao
 * @since 1.5.0
 */
public class SentinelWebFluxFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
            .transform(buildSentinelTransformer(exchange));
    }

    private SentinelReactorTransformer<Void> buildSentinelTransformer(ServerWebExchange exchange) {
        // Maybe we can get the URL pattern elsewhere via:
        // exchange.getAttributeOrDefault(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, path)

        String path = exchange.getRequest().getPath().value();
        String finalPath = Optional.ofNullable(WebFluxCallbackManager.getUrlCleaner())
            .map(f -> f.apply(exchange, path))
            .orElse(path);
        String origin = Optional.ofNullable(WebFluxCallbackManager.getRequestOriginParser())
            .map(f -> f.apply(exchange))
            .orElse(EMPTY_ORIGIN);

        return new SentinelReactorTransformer<>(
            new EntryConfig(finalPath, EntryType.IN, new ContextConfig(finalPath, origin)));
    }

    private static final String EMPTY_ORIGIN = "";
}
