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

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * The configuration center for Web Servlet adapter (ported to Spring Web adapter).
 *
 * @since 1.8.8
 */
public final class WebServletLocalConfig {

    public static final String BLOCK_PAGE_URL_CONF_KEY = "csp.sentinel.web.servlet.block.page";
    public static final String BLOCK_PAGE_HTTP_STATUS_CONF_KEY = "csp.sentinel.web.servlet.block.status";
    public static final String BLOCK_PAGE_ALLOW_ORIGINS_CONF_KEY = "csp.sentinel.web.servlet.block.cors-allow-origins";

    private static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    /**
     * Get redirecting page when blocked by Sentinel.
     *
     * @return the block page URL, maybe null if not configured.
     */
    public static String getBlockPage() {
        return SentinelConfig.getConfig(BLOCK_PAGE_URL_CONF_KEY);
    }

    public static void setBlockPage(String blockPage) {
        SentinelConfig.setConfig(BLOCK_PAGE_URL_CONF_KEY, blockPage);
    }

    /**
     * <p>Get the HTTP status when using the default block page.</p>
     * <p>You can set the status code with the {@code -Dcsp.sentinel.web.servlet.block.status}
     * property. When the property is empty or invalid, Sentinel will use 429 (Too Many Requests)
     * as the default status code.</p>
     *
     * @return the HTTP status of the default block page
     */
    public static int getBlockPageHttpStatus() {
        String value = SentinelConfig.getConfig(BLOCK_PAGE_HTTP_STATUS_CONF_KEY);
        if (StringUtil.isEmpty(value)) {
            return HTTP_STATUS_TOO_MANY_REQUESTS;
        }
        try {
            int s = Integer.parseInt(value);
            if (s <= 0) {
                throw new IllegalArgumentException("Invalid status code: " + s);
            }
            return s;
        } catch (Exception e) {
            RecordLog.warn("[WebServletConfig] Invalid block HTTP status (" + value + "), using default 429");
            setBlockPageHttpStatus(HTTP_STATUS_TOO_MANY_REQUESTS);
        }
        return HTTP_STATUS_TOO_MANY_REQUESTS;
    }

    /**
     * Set the HTTP status of the default block page.
     *
     * @param httpStatus the HTTP status of the default block page
     */
    public static void setBlockPageHttpStatus(int httpStatus) {
        if (httpStatus <= 0) {
            throw new IllegalArgumentException("Invalid HTTP status code: " + httpStatus);
        }
        SentinelConfig.setConfig(BLOCK_PAGE_HTTP_STATUS_CONF_KEY, String.valueOf(httpStatus));
    }

    private WebServletLocalConfig() {}
}
