/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.log.RecordLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author wavesZh
 */
public class CompositeParam {
    private String ip;
    private String host;
    private Map<String, String> headers;
    private Map<String, String> urlParams;
    private Map<String, String> cookies;

    public void add(int parseStrategy, String fieldName, String value) {
        switch (parseStrategy) {
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP:
                ip = value;
                break;
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HOST:
                host = value;
                break;
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER:
                if (headers == null) {
                    headers = new HashMap<>(4);
                }
                headers.put(fieldName, value);
                break;
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM:
                if (urlParams == null) {
                    urlParams = new HashMap<>(4);
                }
                urlParams.put(fieldName, value);
                break;
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_COOKIE:
                if (cookies == null) {
                    cookies = new HashMap<>(4);
                }
                cookies.put(fieldName, value);
                break;
            default:
                // todo
                RecordLog.warn("");
                break;
        }
    }

    public String getIp() {
        return ip;
    }

    public String getHost() {
        return host;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getUrlParams() {
        return urlParams;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        CompositeParam that = (CompositeParam) o;

        if (!Objects.equals(ip, that.ip)) { return false; }
        if (!Objects.equals(host, that.host)) { return false; }
        if (!Objects.equals(headers, that.headers)) { return false; }
        if (!Objects.equals(urlParams, that.urlParams)) { return false; }
        return Objects.equals(cookies, that.cookies);
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (urlParams != null ? urlParams.hashCode() : 0);
        result = 31 * result + (cookies != null ? cookies.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompositeParam{" +
                "ip='" + ip + '\'' +
                ", host='" + host + '\'' +
                ", headers=" + headers +
                ", urlParams=" + urlParams +
                ", cookies=" + cookies +
                '}';
    }
}