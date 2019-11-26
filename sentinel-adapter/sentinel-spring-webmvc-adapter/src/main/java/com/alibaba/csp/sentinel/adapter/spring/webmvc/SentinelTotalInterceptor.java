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
import com.alibaba.csp.sentinel.log.RecordLog;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring mvc interceptor for all requests.
 *
 * @author kaizi2009
 */
public class SentinelTotalInterceptor extends AbstractSentinelInterceptor {
    private SentinelWebMvcTotalConfig config;

    public SentinelTotalInterceptor(SentinelWebMvcTotalConfig config) {
        super();
        setConfig(config);
        setBaseWebMvcConfig(config);
    }

    public SentinelTotalInterceptor() {
        this(new SentinelWebMvcTotalConfig());
    }

    public SentinelTotalInterceptor setConfig(SentinelWebMvcTotalConfig config) {
        if (config == null) {
            this.config = new SentinelWebMvcTotalConfig();
            RecordLog.info("Config is null, use default config");
        } else {
            this.config = config;
        }
        RecordLog.info(String.format("SentinelInterceptor config: requestAttributeName=%s, originParser=%s, blockExceptionHandler=%s, totalResourceName=%s", config.getRequestAttributeName(), config.getOriginParser(), config.getBlockExceptionHandler(), config.getTotalResourceName()));
        return this;
    }

    @Override
    protected String getResourceName(HttpServletRequest request) {
        return config.getTotalResourceName();
    }
}
