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
package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

/**
 * @author kaizi2009
 * @since 1.7.1
 */
public class SentinelWebMvcTotalConfig extends BaseWebMvcConfig {

    public static final String DEFAULT_TOTAL_RESOURCE_NAME = "spring-mvc-total-url-request";
    public static final String DEFAULT_REQUEST_ATTRIBUTE_NAME = "$$sentinel_spring_web_total_entry_attr";

    private String totalResourceName = DEFAULT_TOTAL_RESOURCE_NAME;

    public SentinelWebMvcTotalConfig() {
        super();
        setRequestAttributeName(DEFAULT_REQUEST_ATTRIBUTE_NAME);
    }

    public String getTotalResourceName() {
        return totalResourceName;
    }

    public SentinelWebMvcTotalConfig setTotalResourceName(String totalResourceName) {
        this.totalResourceName = totalResourceName;
        return this;
    }

    @Override
    public String toString() {
        return "SentinelWebMvcTotalConfig{" +
            "totalResourceName='" + totalResourceName + '\'' +
            ", requestAttributeName='" + requestAttributeName + '\'' +
            ", blockExceptionHandler=" + blockExceptionHandler +
            ", originParser=" + originParser +
            '}';
    }
}
