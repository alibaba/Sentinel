/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.DefaultBlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.RequestOriginParser;

/**
 * Common base configuration for Spring Web MVC adapter.
 *
 * @since 1.8.8
 */
public abstract class BaseWebMvcConfig {
    public final static String REQUEST_REF_EXCEPTION_NAME = "$$sentinel_spring_web_entry_attr-exception";

    protected String requestAttributeName;
    protected String requestRefName;
    protected BlockExceptionHandler blockExceptionHandler = new DefaultBlockExceptionHandler();
    protected RequestOriginParser originParser;

    public String getRequestAttributeName() {
        return requestAttributeName;
    }

    public void setRequestAttributeName(String requestAttributeName) {
        this.requestAttributeName = requestAttributeName;
        this.requestRefName = this.requestAttributeName + "-rc";
    }

    /**
     * Paired with attr name used to track reference count.
     *
     * @return
     */
    public String getRequestRefName() {
        return requestRefName;
    }

    public BlockExceptionHandler getBlockExceptionHandler() {
        return blockExceptionHandler;
    }

    public void setBlockExceptionHandler(BlockExceptionHandler blockExceptionHandler) {
        this.blockExceptionHandler = blockExceptionHandler;
    }

    public RequestOriginParser getOriginParser() {
        return originParser;
    }

    public void setOriginParser(RequestOriginParser originParser) {
        this.originParser = originParser;
    }
}
