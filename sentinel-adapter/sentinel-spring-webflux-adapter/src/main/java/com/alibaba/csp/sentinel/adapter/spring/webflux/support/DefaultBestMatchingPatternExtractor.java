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

import com.alibaba.csp.sentinel.util.AssertUtil;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Function;

/**
 * default extractor for "BEST_MATCHING_HANDLER_ATTRIBUTE"
 *
 * @author icodening
 * @date 2022.03.29
 */
public class DefaultBestMatchingPatternExtractor implements Function<ServerWebExchange, String> {

    private final List<HandlerMapping> handlerMappings;

    private final List<HandlerMappingBestMatchingPatternExtractor> handlerMappingBestMatchingPatternExtractors;

    public DefaultBestMatchingPatternExtractor(List<HandlerMapping> handlerMappings,
                                               List<HandlerMappingBestMatchingPatternExtractor> handlerMappingBestMatchingPatternExtractors) {
        AssertUtil.notNull(handlerMappings, "handlerMappings cannot be null");
        AssertUtil.notNull(handlerMappingBestMatchingPatternExtractors, "handlerMappingBestMatchingPatternExtractors cannot be null");
        this.handlerMappings = handlerMappings;
        this.handlerMappingBestMatchingPatternExtractors = handlerMappingBestMatchingPatternExtractors;
    }

    @Override
    public String apply(ServerWebExchange exchange) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            for (HandlerMappingBestMatchingPatternExtractor handlerMappingBestMatchingPatternExtractor : handlerMappingBestMatchingPatternExtractors) {
                if (!handlerMappingBestMatchingPatternExtractor.supportExtract(handlerMapping)) {
                    continue;
                }
                String bestMatchingPath = handlerMappingBestMatchingPatternExtractor.extract(handlerMapping, exchange);
                if (bestMatchingPath != null) {
                    return bestMatchingPath;
                }
            }
        }
        return null;
    }
}
