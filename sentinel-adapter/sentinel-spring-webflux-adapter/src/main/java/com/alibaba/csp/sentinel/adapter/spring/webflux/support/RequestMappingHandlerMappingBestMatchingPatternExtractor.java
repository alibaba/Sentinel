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
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

/**
 * best matching pattern extractor for RequestMappingHandlerMapping
 *
 * @author icodening
 * @date 2022.03.29
 * @see RequestMappingHandlerMapping
 */
public class RequestMappingHandlerMappingBestMatchingPatternExtractor implements HandlerMappingBestMatchingPatternExtractor {

    @Override
    public String extract(HandlerMapping handlerMapping, ServerWebExchange exchange) {
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) handlerMapping;
        requestMappingHandlerMapping.getHandlerInternal(exchange);
        Object attribute = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (attribute instanceof PathPattern) {
            return ((PathPattern) attribute).getPatternString();
        }
        return null;
    }

    @Override
    public boolean supportExtract(HandlerMapping handlerMapping) {
        return handlerMapping instanceof RequestMappingHandlerMapping;
    }
}
