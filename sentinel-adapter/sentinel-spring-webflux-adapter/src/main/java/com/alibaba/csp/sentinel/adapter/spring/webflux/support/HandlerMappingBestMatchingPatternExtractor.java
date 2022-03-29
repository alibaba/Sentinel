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
import org.springframework.web.server.ServerWebExchange;

/**
 * "BEST_MATCHING_HANDLER_ATTRIBUTE" extractor
 *
 * @author icodening
 * @date 2022.03.29
 * @see HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
 */
public interface HandlerMappingBestMatchingPatternExtractor {

    /**
     * whether to process the given HandlerMapping
     *
     * @param handlerMapping Spring HandlerMapping
     * @return "true" is support extract
     */
    boolean supportExtract(HandlerMapping handlerMapping);

    /**
     * extract BEST_MATCHING_HANDLER_ATTRIBUTE for current given ServerWebExchange
     *
     * @param handlerMapping current HandlerMapping, eg. RouterFunctionMapping, RequestMappingHandlerMapping, SimpleUrlHandlerMapping
     * @param exchange       current request and response
     * @return best matching pattern, return null when if not found , eg. /users/{id}
     */
    String extract(HandlerMapping handlerMapping, ServerWebExchange exchange);
}
