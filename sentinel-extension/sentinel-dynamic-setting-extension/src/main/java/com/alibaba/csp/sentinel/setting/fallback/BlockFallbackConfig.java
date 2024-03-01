/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.setting.fallback;

import java.util.Map;
import java.util.Set;

/**
 * @author Eric Zhao
 * @author guanyu
 */
public class BlockFallbackConfig<T> {

    private Integer targetResourceType;
    /**
     * (targetResource, [targetBlockType...])
     */
    private Map<String, Set<Integer>> targetMap;

    private T fallbackBehavior;

    public Integer getTargetResourceType() {
        return targetResourceType;
    }

    public BlockFallbackConfig<T> setTargetResourceType(Integer targetResourceType) {
        this.targetResourceType = targetResourceType;
        return this;
    }

    public Map<String, Set<Integer>> getTargetMap() {
        return targetMap;
    }

    public BlockFallbackConfig<T> setTargetMap(
            Map<String, Set<Integer>> targetMap) {
        this.targetMap = targetMap;
        return this;
    }

    public T getFallbackBehavior() {
        return fallbackBehavior;
    }

    public BlockFallbackConfig<T> setFallbackBehavior(T fallbackBehavior) {
        this.fallbackBehavior = fallbackBehavior;
        return this;
    }

    @Override
    public String toString() {
        return "BlockFallbackConfig{" +
                "targetResourceType=" + targetResourceType +
                ", targetMap=" + targetMap +
                ", fallbackBehavior=" + fallbackBehavior +
                '}';
    }

    public static class WebBlockFallbackBehavior {

        private Integer webFallbackMode;
        /**
         * Items for text response mode.
         */
        private Integer webRespStatusCode;
        /**
         * Items for text response mode.
         */
        private String webRespMessage;
        /**
         * Items for text response mode.
         */
        private Integer webRespContentType;

        /**
         * Items for redirect mode.
         */
        private String webRedirectUrl;

        public Integer getWebFallbackMode() {
            return webFallbackMode;
        }

        public WebBlockFallbackBehavior setWebFallbackMode(Integer webFallbackMode) {
            this.webFallbackMode = webFallbackMode;
            return this;
        }

        public Integer getWebRespStatusCode() {
            return webRespStatusCode;
        }

        public WebBlockFallbackBehavior setWebRespStatusCode(Integer webRespStatusCode) {
            this.webRespStatusCode = webRespStatusCode;
            return this;
        }

        public String getWebRespMessage() {
            return webRespMessage;
        }

        public WebBlockFallbackBehavior setWebRespMessage(String webRespMessage) {
            this.webRespMessage = webRespMessage;
            return this;
        }

        public Integer getWebRespContentType() {
            return webRespContentType;
        }

        public WebBlockFallbackBehavior setWebRespContentType(Integer webRespContentType) {
            this.webRespContentType = webRespContentType;
            return this;
        }

        public String getWebRedirectUrl() {
            return webRedirectUrl;
        }

        public WebBlockFallbackBehavior setWebRedirectUrl(String webRedirectUrl) {
            this.webRedirectUrl = webRedirectUrl;
            return this;
        }

        @Override
        public String toString() {
            return "WebBlockFallbackBehavior{" +
                    "webFallbackMode=" + webFallbackMode +
                    ", webRespStatusCode=" + webRespStatusCode +
                    ", webRespMessage='" + webRespMessage + '\'' +
                    ", webRespContentType=" + webRespContentType +
                    ", webRedirectUrl='" + webRedirectUrl + '\'' +
                    '}';
        }
    }

    public static class RpcBlockFallbackBehavior {

        /**
         * Items for rpc response mode.
         */
        private Integer rpcFallbackMode;

        /**
         * Items for cache rpc instance.
         */
        private Integer rpcFallbackCacheMode;

        /**
         * Items for response fallback class name.
         */
        private String rpcRespFallbackClassName;

        /**
         * Items for exception error message.
         */
        private String rpcFallbackExceptionMessage;

        /**
         * Items for return object body.
         */
        private String rpcRespContentBody;

        public Integer getRpcFallbackMode() {
            return rpcFallbackMode;
        }

        public RpcBlockFallbackBehavior setRpcFallbackMode(Integer rpcFallbackMode) {
            this.rpcFallbackMode = rpcFallbackMode;
            return this;
        }

        public Integer getRpcFallbackCacheMode() {
            return rpcFallbackCacheMode;
        }

        public RpcBlockFallbackBehavior setRpcFallbackCacheMode(Integer rpcFallbackCacheMode) {
            this.rpcFallbackCacheMode = rpcFallbackCacheMode;
            return this;
        }

        public String getRpcRespFallbackClassName() {
            return rpcRespFallbackClassName;
        }

        public RpcBlockFallbackBehavior setRpcRespFallbackClassName(String rpcRespFallbackClassName) {
            this.rpcRespFallbackClassName = rpcRespFallbackClassName;
            return this;
        }

        public String getRpcFallbackExceptionMessage() {
            return rpcFallbackExceptionMessage;
        }

        public RpcBlockFallbackBehavior setRpcFallbackExceptionMessage(String rpcFallbackExceptionMessage) {
            this.rpcFallbackExceptionMessage = rpcFallbackExceptionMessage;
            return this;
        }

        public String getRpcRespContentBody() {
            return rpcRespContentBody;
        }

        public RpcBlockFallbackBehavior setRpcRespContentBody(String rpcRespContentBody) {
            this.rpcRespContentBody = rpcRespContentBody;
            return this;
        }

        @Override
        public String toString() {
            return "RpcBlockFallbackBehavior{" +
                    "rpcFallbackMode=" + rpcFallbackMode +
                    ", rpcFallbackCacheMode='" + rpcFallbackCacheMode + '\'' +
                    ", rpcRespFallbackClassName='" + rpcRespFallbackClassName + '\'' +
                    ", rpcRespContentBody='" + rpcRespContentBody + '\'' +
                    ", rpcFallbackExceptionMessage=" + rpcFallbackExceptionMessage +
                    '}';
        }
    }
}
