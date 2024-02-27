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

import com.alibaba.csp.sentinel.adapter.web.common.UrlCleaner;

/**
 * @since 1.8.8
 */
public class SentinelWebMvcConfig extends BaseWebMvcConfig {

    public static final String DEFAULT_REQUEST_ATTRIBUTE_NAME = "$$sentinel_spring_web_entry_attr";

    /**
     * Specify the URL cleaner that unifies the URL resources.
     */
    private UrlCleaner urlCleaner;

    /**
     * Specify whether the URL resource name should contain the HTTP method prefix (e.g. {@code POST:}).
     */
    private boolean httpMethodSpecify;

    /**
     * Specify whether the URL resource name should contain the HTTP method prefix in MSE (e.g. {@code GET:}).
     */
    private static boolean mseHttpMethodSpecify;


    /**
     * Specify whether unify web context(i.e. use the default context name), and is true by default.
     *
     * @since 1.7.2
     */
    private boolean webContextUnify = true;

    /**
     * Specify whether the URL resource name should contain the context-path
     */
    private boolean contextPathSpecify = true;

    public SentinelWebMvcConfig() {
        super();
        setRequestAttributeName(DEFAULT_REQUEST_ATTRIBUTE_NAME);
        try {
            String enable = System.getProperty("spring.cloud.mse.sentinel.web.http-method-prefix","true");
            if(enable != null){
                mseHttpMethodSpecify = Boolean.parseBoolean(enable);
            }
            String enableContextPath = System.getProperty("spring.cloud.ahas.sentinel.web.context-path", "true");
            if (enableContextPath != null) {
                contextPathSpecify = Boolean.parseBoolean(enableContextPath);
            }
        } catch (Exception ignore) {
        }
    }

    public UrlCleaner getUrlCleaner() {
        return urlCleaner;
    }

    public SentinelWebMvcConfig setUrlCleaner(UrlCleaner urlCleaner) {
        this.urlCleaner = urlCleaner;
        return this;
    }

    public boolean isHttpMethodSpecify() {
        return httpMethodSpecify;
    }

    public SentinelWebMvcConfig setHttpMethodSpecify(boolean httpMethodSpecify) {
        this.httpMethodSpecify = httpMethodSpecify;
        return this;
    }

    public static boolean isMseHttpMethodSpecify() {
        return mseHttpMethodSpecify;
    }

    public boolean isWebContextUnify() {
        return webContextUnify;
    }

    public SentinelWebMvcConfig setWebContextUnify(boolean webContextUnify) {
        this.webContextUnify = webContextUnify;
        return this;
    }

    public boolean isContextPathSpecify() {
        return contextPathSpecify;
    }

    public SentinelWebMvcConfig setContextPathSpecify(boolean contextPathSpecify) {
        this.contextPathSpecify = contextPathSpecify;
        return this;
    }

    @Override
    public String toString() {
        return "SentinelWebMvcConfig{" +
            "urlCleaner=" + urlCleaner +
            ", httpMethodSpecify=" + httpMethodSpecify +
            ", webContextUnify=" + webContextUnify +
            ", contextPathSpecify=" + contextPathSpecify +
            ", requestAttributeName='" + requestAttributeName + '\'' +
            ", blockExceptionHandler=" + blockExceptionHandler +
            ", originParser=" + originParser +
            '}';
    }
}
