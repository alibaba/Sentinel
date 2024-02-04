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

import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Collections;
import java.util.List;

/**
 * best matching pattern extractor for RouterFunctionMapping
 *
 * @author icodening
 * @date 2022.03.29
 * @see RouterFunctionMapping
 */
public class RouterFunctionMappingBestMatchingPatternExtractor implements HandlerMappingBestMatchingPatternExtractor {

    private List<RequestPredicate> registeredRequestPredicates = Collections.emptyList();

    private volatile boolean initialized = false;

    @Override
    public boolean supportExtract(HandlerMapping handlerMapping) {
        return handlerMapping instanceof RouterFunctionMapping;
    }

    public void setRegisteredRequestPredicates(List<RequestPredicate> registeredRequestPredicates) {
        if (registeredRequestPredicates != null) {
            this.registeredRequestPredicates = registeredRequestPredicates;
        }
    }

    @Override
    public String extract(HandlerMapping handlerMapping, ServerWebExchange exchange) {
        initRequestPredicatesIfNecessary(handlerMapping);
        SentinelServerRequest sentinelServerRequest = new SentinelServerRequest(exchange);
        for (RequestPredicate requestPredicate : registeredRequestPredicates) {
            if (requestPredicate.test(sentinelServerRequest)) {
                Object attribute = exchange.getAttribute(RouterFunctions.MATCHING_PATTERN_ATTRIBUTE);
                if (attribute instanceof PathPattern) {
                    exchange.getAttributes().remove(RouterFunctions.MATCHING_PATTERN_ATTRIBUTE);
                    return ((PathPattern) attribute).getPatternString();
                }
            }
        }
        return null;
    }

    private void initRequestPredicatesIfNecessary(HandlerMapping handlerMapping){
        if (!this.initialized) {
            RouterFunctionMapping routerFunctionMapping = (RouterFunctionMapping) handlerMapping;
            RouterFunction<?> routerFunction = routerFunctionMapping.getRouterFunction();
            RouterFunctionRequestPredicateRepository routerFunctionRequestPredicateRepository = new RouterFunctionRequestPredicateRepository();
            if (routerFunction != null) {
                routerFunction.accept(new RouterFunctionRequestPredicateRegistrarVisitor(routerFunctionRequestPredicateRepository));
            }
            setRegisteredRequestPredicates(routerFunctionRequestPredicateRepository.getRequestPredicates());
            this.initialized = true;
        }
    }
}
