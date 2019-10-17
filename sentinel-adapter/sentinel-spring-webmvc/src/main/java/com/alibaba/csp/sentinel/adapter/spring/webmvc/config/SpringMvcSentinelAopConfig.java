/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

/**
 * @author zhangkai
 */
public class SpringMvcSentinelAopConfig {

    public static final String DEFAULT_SPRING_MVC_CONTEXT_NAME = "spring_mvc_context";
    public static final String DEFAULT_REQUEST_ATTRKEY = "sentinel_spring_mvc_entity_container";
    private String contextName = DEFAULT_SPRING_MVC_CONTEXT_NAME;
    private SpringMvcUrlCleaner urlCleaner = new DefaultSpringMvcUrlCleaner();
    private SpringMvcRequestOriginParser originParser;
    private boolean httpMethodSpecify;
    private String requestAttrKey = DEFAULT_REQUEST_ATTRKEY;
    public SpringMvcSentinelAopConfig setContextName(String contextName) {
        this.contextName = contextName;
        return this;
    }

    public SpringMvcSentinelAopConfig setUrlCleaner(SpringMvcUrlCleaner urlCleaner) {
        this.urlCleaner = urlCleaner;
        return this;
    }

    public SpringMvcSentinelAopConfig setOriginParser(SpringMvcRequestOriginParser originParser) {
        this.originParser = originParser;
        return this;
    }

    public SpringMvcSentinelAopConfig setHttpMethodSpecify(boolean httpMethodSpecify) {
        this.httpMethodSpecify = httpMethodSpecify;
        return this;
    }

    public SpringMvcSentinelAopConfig setRequestAttrKey(String requestAttrKey) {
        this.requestAttrKey = requestAttrKey;
        return this;
    }

    public String getContextName() {
        return contextName;
    }

    public SpringMvcUrlCleaner getUrlCleaner() {
        return urlCleaner;
    }

    public boolean isHttpMethodSpecify() {
        return httpMethodSpecify;
    }

    public SpringMvcRequestOriginParser getOriginParser() {
        return originParser;
    }

    public String getRequestAttrKey() {
        return requestAttrKey;
    }
}
