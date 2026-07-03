/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.common.param;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * delegate RequestItemParser, support add extractors to customize request item parse.
 * <p>
 * example:
 * if you want to get client real ip in multi nginx proxy, you can register SentinelGatewayFilter bean as follows
 *
 * ConfigurableRequestItemParser<ServerWebExchange> parser = new  ConfigurableRequestItemParser<>(new ServerWebExchangeItemParser());
 * List<String> headerNames = Arrays.asList("X-Real-IP", "Client-IP");
 * parser.addRemoteAddressExtractor(serverWebExchange -> {
 *      for (String headerKey : headerNames) {
 *          String remoteAddress = serverWebExchange.getRequest().getHeaders().getFirst(headerKey);
 *          if (StringUtils.hasLength(remoteAddress)) {
 *              return remoteAddress;
 *          }
 *      }
 *      return null;
 * });
 * return new SentinelGatewayFilter(parser);
 *
 * @author icodening
 * @date 2022.01.14
 */
public class ConfigurableRequestItemParser<T> implements RequestItemParser<T> {

    private final List<Function<T, String>> pathExtractors = new ArrayList<>(2);

    private final List<Function<T, String>> remoteAddressExtractors = new ArrayList<>(2);

    private final List<BiFunction<T, String, String>> headerExtractors = new ArrayList<>(2);

    private final List<BiFunction<T, String, String>> urlParamExtractors = new ArrayList<>(2);

    private final List<BiFunction<T, String, String>> cookieValueExtractors = new ArrayList<>(2);

    private final RequestItemParser<T> delegate;

    public ConfigurableRequestItemParser(RequestItemParser<T> delegate) {
        AssertUtil.notNull(delegate, "delegate can not be null");
        this.delegate = delegate;
    }

    @Override
    public String getPath(T request) {
        for (Function<T, String> extractor : pathExtractors) {
            String pathValue = extractor.apply(request);
            if (StringUtil.isNotBlank(pathValue)) {
                return pathValue;
            }
        }
        return delegate.getPath(request);
    }

    @Override
    public String getRemoteAddress(T request) {
        for (Function<T, String> extractor : remoteAddressExtractors) {
            String remoteAddress = extractor.apply(request);
            if (StringUtil.isNotBlank(remoteAddress)) {
                return remoteAddress;
            }
        }
        return delegate.getRemoteAddress(request);
    }

    @Override
    public String getHeader(T request, String key) {
        for (BiFunction<T, String, String> extractor : headerExtractors) {
            String headerValue = extractor.apply(request, key);
            if (StringUtil.isNotBlank(headerValue)) {
                return headerValue;
            }
        }
        return delegate.getHeader(request, key);
    }

    @Override
    public String getUrlParam(T request, String paramName) {
        for (BiFunction<T, String, String> extractor : urlParamExtractors) {
            String urlParam = extractor.apply(request, paramName);
            if (StringUtil.isNotBlank(urlParam)) {
                return urlParam;
            }
        }
        return delegate.getUrlParam(request, paramName);
    }

    @Override
    public String getCookieValue(T request, String cookieName) {
        for (BiFunction<T, String, String> extractor : cookieValueExtractors) {
            String cookie = extractor.apply(request, cookieName);
            if (StringUtil.isNotBlank(cookie)) {
                return cookie;
            }
        }
        return delegate.getCookieValue(request, cookieName);
    }

    public ConfigurableRequestItemParser<T> addPathExtractor(Function<T, String> extractor) {
        if (extractor == null) {
            return this;
        }
        pathExtractors.add(extractor);
        return this;
    }

    public ConfigurableRequestItemParser<T> addRemoteAddressExtractor(Function<T, String> extractor) {
        if (extractor == null) {
            return this;
        }
        remoteAddressExtractors.add(extractor);
        return this;
    }

    public ConfigurableRequestItemParser<T> addHeaderExtractor(BiFunction<T, String, String> extractor) {
        if (extractor == null) {
            return this;
        }
        headerExtractors.add(extractor);
        return this;
    }

    public ConfigurableRequestItemParser<T> addUrlParamExtractor(BiFunction<T, String, String> extractor) {
        if (extractor == null) {
            return this;
        }
        urlParamExtractors.add(extractor);
        return this;
    }

    public ConfigurableRequestItemParser<T> addCookieValueExtractor(BiFunction<T, String, String> extractor) {
        if (extractor == null) {
            return this;
        }
        cookieValueExtractors.add(extractor);
        return this;
    }
}
