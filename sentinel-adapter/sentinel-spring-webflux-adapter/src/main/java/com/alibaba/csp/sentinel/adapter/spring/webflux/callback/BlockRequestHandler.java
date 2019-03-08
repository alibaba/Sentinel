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
package com.alibaba.csp.sentinel.adapter.spring.webflux.callback;

import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive handler for the blocked request.
 *
 * @author Eric Zhao
 * @since 1.5.0
 */
@FunctionalInterface
public interface BlockRequestHandler {

    /**
     * Handle the blocked request.
     *
     * @param exchange server exchange object
     * @param t block exception
     * @return server response to return
     */
    Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t);
}
