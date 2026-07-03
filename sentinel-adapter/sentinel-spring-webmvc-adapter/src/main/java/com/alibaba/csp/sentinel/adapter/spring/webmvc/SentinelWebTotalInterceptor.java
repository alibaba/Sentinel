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

import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcTotalConfig;

import javax.servlet.http.HttpServletRequest;

/**
 * The web interceptor for all requests, which will unify all URL as
 * a single resource name (configured in {@link SentinelWebMvcTotalConfig}).
 *
 * @author kaizi2009
 * @since 1.7.1
 */
public class SentinelWebTotalInterceptor extends AbstractSentinelInterceptor {

    private final SentinelWebMvcTotalConfig config;

    public SentinelWebTotalInterceptor(SentinelWebMvcTotalConfig config) {
        super(config);
        if (config == null) {
            this.config = new SentinelWebMvcTotalConfig();
        } else {
            this.config = config;
        }
    }

    public SentinelWebTotalInterceptor() {
        this(new SentinelWebMvcTotalConfig());
    }

    @Override
    protected String getResourceName(HttpServletRequest request) {
        return config.getTotalResourceName();
    }
}
