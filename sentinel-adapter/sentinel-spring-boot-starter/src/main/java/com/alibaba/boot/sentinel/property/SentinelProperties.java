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
package com.alibaba.boot.sentinel.property;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * @author Eric Zhao
 */
@ConfigurationProperties(prefix = "spring.sentinel")
public class SentinelProperties {

    private boolean enabled = true;

    private ServletFilterConfig servletFilter;

    public boolean isEnabled() {
        return enabled;
    }

    public SentinelProperties setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ServletFilterConfig getServletFilter() {
        return servletFilter;
    }

    public SentinelProperties setServletFilter(
        ServletFilterConfig servletFilter) {
        this.servletFilter = servletFilter;
        return this;
    }

    public static class DubboFilterConfig {}

    public static class ServletFilterConfig {

        private boolean enabled = true;

        /**
         * Chain order for Sentinel servlet filter.
         */
        private int order = Ordered.HIGHEST_PRECEDENCE;

        /**
         * URL pattern for Sentinel servlet filter.
         */
        private List<String> urlPatterns;

        public int getOrder() {
            return this.order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public List<String> getUrlPatterns() {
            return urlPatterns;
        }

        public void setUrlPatterns(List<String> urlPatterns) {
            this.urlPatterns = urlPatterns;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public ServletFilterConfig setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
    }
}
