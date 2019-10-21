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

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultUrlCleaner;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;

/**
 * @Author kaizi2009
 */
public class SentinelSpringMvcConfig {

    public static final String DEFAULT_REQUEST_ATTRIBUTE_NAME = "sentinel_spring_mvc_entity_container";
    private UrlCleaner urlCleaner = new DefaultUrlCleaner();
    private RequestOriginParser originParser;
    private boolean httpMethodSpecify;
    private String requestAttributeName = DEFAULT_REQUEST_ATTRIBUTE_NAME;

    public SentinelSpringMvcConfig setUrlCleaner(UrlCleaner urlCleaner) {
        this.urlCleaner = urlCleaner;
        return this;
    }

    public SentinelSpringMvcConfig setOriginParser(RequestOriginParser originParser) {
        this.originParser = originParser;
        return this;
    }

    public SentinelSpringMvcConfig setHttpMethodSpecify(boolean httpMethodSpecify) {
        this.httpMethodSpecify = httpMethodSpecify;
        return this;
    }

    public SentinelSpringMvcConfig setRequestAttributeName(String requestAttributeName) {
        this.requestAttributeName = requestAttributeName;
        return this;
    }

    public UrlCleaner getUrlCleaner() {
        return urlCleaner;
    }

    public boolean isHttpMethodSpecify() {
        return httpMethodSpecify;
    }

    public RequestOriginParser getOriginParser() {
        return originParser;
    }

    public String getRequestAttributeName() {
        return requestAttributeName;
    }
}
