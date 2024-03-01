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
package com.alibaba.csp.sentinel.setting.adapter;

/**
 * @author Eric Zhao
 */
public class SentinelAdapterSettingEntity {

    private Integer webFallbackMode;
    /**
     * Items for text response mode.
     */
    private Integer webRespStatusCode;
    /**
     * Items for text response mode.
     *
     * @since 1.8.2
     */
    private String webRespMessage;
    /**
     * Items for text response mode.
     *
     * @since 1.8.2
     */
    private Integer webRespContentType;
    /**
     * Items for redirect mode.
     */
    private String webRedirectUrl;
    /**
     * Items for Web servlet URL cleaner (pre-configured).
     */
    private String webUrlPrefixCleanItems;

    public Integer getWebFallbackMode() {
        return webFallbackMode;
    }

    public SentinelAdapterSettingEntity setWebFallbackMode(Integer webFallbackMode) {
        this.webFallbackMode = webFallbackMode;
        return this;
    }

    public Integer getWebRespStatusCode() {
        return webRespStatusCode;
    }

    public SentinelAdapterSettingEntity setWebRespStatusCode(Integer webRespStatusCode) {
        this.webRespStatusCode = webRespStatusCode;
        return this;
    }

    public String getWebRespMessage() {
        return webRespMessage;
    }

    public SentinelAdapterSettingEntity setWebRespMessage(String webRespMessage) {
        this.webRespMessage = webRespMessage;
        return this;
    }

    public Integer getWebRespContentType() {
        return webRespContentType;
    }

    public SentinelAdapterSettingEntity setWebRespContentType(Integer webRespContentType) {
        this.webRespContentType = webRespContentType;
        return this;
    }

    public String getWebRedirectUrl() {
        return webRedirectUrl;
    }

    public SentinelAdapterSettingEntity setWebRedirectUrl(String webRedirectUrl) {
        this.webRedirectUrl = webRedirectUrl;
        return this;
    }

    public String getWebUrlPrefixCleanItems() {
        return webUrlPrefixCleanItems;
    }

    public SentinelAdapterSettingEntity setWebUrlPrefixCleanItems(String webUrlPrefixCleanItems) {
        this.webUrlPrefixCleanItems = webUrlPrefixCleanItems;
        return this;
    }

    @Override
    public String toString() {
        return "SentinelAdapterSettingEntity{" +
                "webFallbackMode=" + webFallbackMode +
                ", webRespStatusCode=" + webRespStatusCode +
                ", webRespMessage='" + webRespMessage + '\'' +
                ", webRespContentType=" + webRespContentType +
                ", webRedirectUrl='" + webRedirectUrl + '\'' +
                ", webUrlPrefixCleanItems='" + webUrlPrefixCleanItems + '\'' +
                '}';
    }
}
