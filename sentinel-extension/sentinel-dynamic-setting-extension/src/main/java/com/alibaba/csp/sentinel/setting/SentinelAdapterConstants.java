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
package com.alibaba.csp.sentinel.setting;

/**
 * @author Eric Zhao
 */
public final class SentinelAdapterConstants {

    public static final int WEB_FALLBACK_TEXT_RESPONSE = 0;
    public static final int WEB_FALLBACK_REDIRECT = 1;

    public static final int WEB_FALLBACK_CONTENT_TYPE_TEXT = 0;
    public static final int WEB_FALLBACK_CONTENT_TYPE_JSON = 1;

    public static final String WEB_BLOCK_PAGE_URL_CONF_KEY = "csp.sentinel.web.servlet.block.page";
    public static final String WEB_BLOCK_PAGE_HTTP_STATUS_CONF_KEY = "csp.sentinel.web.servlet.block.status";

    private SentinelAdapterConstants() {}
}
