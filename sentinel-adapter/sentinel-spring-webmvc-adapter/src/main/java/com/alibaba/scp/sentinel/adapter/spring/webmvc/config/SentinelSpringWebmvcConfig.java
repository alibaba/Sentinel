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
package com.alibaba.scp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;

/**
 * @author zhaoyuguang
 */
public class SentinelSpringWebmvcConfig {

    public static final String SPRING_WEBMVC_CONTEXT_NAME = "sentinel_spring_webmvc_context";
    public static final String SENTINEL_REQUEST_ATTR_SPRING_WEBMVC_KEY = "sentinel_request_attr_spring_webmvc_key";

    private UrlCleaner urlCleaner;
    private RequestOriginParser originParser;
    private boolean httpMethodSpecify = false;

    public static final String BLOCK_PAGE = "csp.sentinel.spring.webmvc.block.page";

    public static String getBlockPage() {
        return SentinelConfig.getConfig(BLOCK_PAGE);
    }

    public static void setBlockPage(String blockPage) {
        SentinelConfig.setConfig(BLOCK_PAGE, blockPage);
    }

    public UrlCleaner getUrlCleaner() {
        return urlCleaner;
    }

    public void setUrlCleaner(UrlCleaner urlCleaner) {
        this.urlCleaner = urlCleaner;
    }

    public RequestOriginParser getOriginParser() {
        return originParser;
    }

    public void setOriginParser(RequestOriginParser originParser) {
        this.originParser = originParser;
    }

    public boolean isHttpMethodSpecify() {
        return httpMethodSpecify;
    }

    public void setHttpMethodSpecify(boolean httpMethodSpecify) {
        this.httpMethodSpecify = httpMethodSpecify;
    }
}
