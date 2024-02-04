/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webflux.support;

import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author icodening
 * @date 2022.03.29
 */
class RouterFunctionRequestPredicateRegistrarVisitor implements RouterFunctions.Visitor {

    private final RouterFunctionRequestPredicateRepository routerFunctionRequestPredicateRepository;

    RouterFunctionRequestPredicateRegistrarVisitor(RouterFunctionRequestPredicateRepository routerFunctionRequestPredicateRepository) {
        this.routerFunctionRequestPredicateRepository = routerFunctionRequestPredicateRepository;
    }

    @Override
    public void startNested(RequestPredicate predicate) {
        //ignore
    }

    @Override
    public void endNested(RequestPredicate predicate) {
        //ignore
    }

    @Override
    public void route(RequestPredicate predicate, HandlerFunction<?> handlerFunction) {
        //cache the predicate
        this.routerFunctionRequestPredicateRepository.addRequestPredicate(predicate);
    }

    @Override
    public void resources(Function<ServerRequest, Mono<Resource>> lookupFunction) {
        //ignore
    }

    @Override
    public void unknown(RouterFunction<?> routerFunction) {
        //ignore
    }
}
