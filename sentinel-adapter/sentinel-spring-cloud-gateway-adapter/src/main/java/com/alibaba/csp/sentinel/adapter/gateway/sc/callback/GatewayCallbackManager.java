/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.sc.callback;

import java.util.function.Function;

import com.alibaba.csp.sentinel.util.AssertUtil;

import org.springframework.web.server.ServerWebExchange;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public final class GatewayCallbackManager {

    private static final Function<ServerWebExchange, String> DEFAULT_ORIGIN_PARSER = (w) -> "";

    /**
     * BlockRequestHandler: (serverExchange, exception) -> response
     */
    private static volatile BlockRequestHandler blockHandler = new DefaultBlockRequestHandler();
    /**
     * RequestOriginParser: (serverExchange) -> origin
     */
    private static volatile Function<ServerWebExchange, String> requestOriginParser = DEFAULT_ORIGIN_PARSER;

    public static /*@NonNull*/ BlockRequestHandler getBlockHandler() {
        return blockHandler;
    }

    public static void resetBlockHandler() {
        GatewayCallbackManager.blockHandler = new DefaultBlockRequestHandler();
    }

    public static void setBlockHandler(BlockRequestHandler blockHandler) {
        AssertUtil.notNull(blockHandler, "blockHandler cannot be null");
        GatewayCallbackManager.blockHandler = blockHandler;
    }

    public static /*@NonNull*/ Function<ServerWebExchange, String> getRequestOriginParser() {
        return requestOriginParser;
    }

    public static void resetRequestOriginParser() {
        GatewayCallbackManager.requestOriginParser = DEFAULT_ORIGIN_PARSER;
    }

    public static void setRequestOriginParser(Function<ServerWebExchange, String> requestOriginParser) {
        AssertUtil.notNull(requestOriginParser, "requestOriginParser cannot be null");
        GatewayCallbackManager.requestOriginParser = requestOriginParser;
    }

    private GatewayCallbackManager() {}
}
