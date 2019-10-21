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
 * @Author kaizi2009
 */
public class SentinelSpringMvcTotalConfig {
    public static final String DEFAULT_TOTAL_TARGET = "spring_mvc_total_url_request";
    public static final String DEFAULT_REQUEST_ATTRIBUTE_NAME = "sentinel_spring_mvc_total_entity_container";

    private String totalTarget = DEFAULT_TOTAL_TARGET;
    private String requestAttributeName = DEFAULT_REQUEST_ATTRIBUTE_NAME;

    public String getTotalTarget() {
        return totalTarget;
    }

    public SentinelSpringMvcTotalConfig setTotalTarget(String totalTarget) {
        this.totalTarget = totalTarget;
        return this;
    }

    public String getRequestAttributeName() {
        return requestAttributeName;
    }

    public SentinelSpringMvcTotalConfig setRequestAttributeName(String requestAttributeName) {
        this.requestAttributeName = requestAttributeName;
        return this;
    }
}
