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
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.alibaba.csp.sentinel.log.RecordLog;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Spring mvc interceptor that integrates with sentinel.
 *
 * @author kaizi2009
 */
public class SentinelInterceptor extends AbstractSentinelInterceptor {
    private SentinelWebMvcConfig config;

    public SentinelInterceptor(SentinelWebMvcConfig config) {
        super();
        setConfig(config);
        super.setBaseWebMvcConfig(config);
    }

    public SentinelInterceptor() {
        this(new SentinelWebMvcConfig());
    }

    public SentinelInterceptor setConfig(SentinelWebMvcConfig config) {
        if (config == null) {
            this.config = new SentinelWebMvcConfig();
            RecordLog.info("Config is null, use default config");
        } else {
            this.config = config;
        }
        RecordLog.info(String.format("SentinelInterceptor config: requestAttributeName=%s, originParser=%s, httpMethodSpecify=%s, blockExceptionHandler=%s, urlCleaner=%s", config.getRequestAttributeName(), config.getOriginParser(), config.isHttpMethodSpecify(), config.getBlockExceptionHandler(), config.getUrlCleaner()));
        return this;
    }

    /**
     * Get target in HttpServletRequest
     *
     * @param request
     * @return
     */
    @Override
    protected String getResourceName(HttpServletRequest request) {
        Object resourceNameObject = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (resourceNameObject == null || !(resourceNameObject instanceof String)) {
            return null;
        }
        String resourceName = (String) resourceNameObject;
        UrlCleaner urlCleaner = config.getUrlCleaner();
        if (urlCleaner != null) {
            resourceName = urlCleaner.clean(resourceName);
        }
        // Add method specification if necessary
        if (StringUtil.isNotEmpty(resourceName) && config.isHttpMethodSpecify()) {
            resourceName = request.getMethod().toUpperCase() + COLON + resourceName;
        }
        return resourceName;
    }

}
