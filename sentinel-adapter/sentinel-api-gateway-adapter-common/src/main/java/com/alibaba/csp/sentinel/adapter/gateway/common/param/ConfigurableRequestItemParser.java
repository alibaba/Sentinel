package com.alibaba.csp.sentinel.adapter.gateway.common.param;

import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
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

    public void addPathExtractor(Function<T, String> extractor) {
        if (extractor == null) {
            return;
        }
        pathExtractors.add(extractor);
    }

    public void addRemoteAddressExtractor(Function<T, String> extractor) {
        if (extractor == null) {
            return;
        }
        remoteAddressExtractors.add(extractor);
    }

    public void addHeaderExtractor(BiFunction<T, String, String> extractor) {
        if (extractor == null) {
            return;
        }
        headerExtractors.add(extractor);
    }

    public void addUrlParamExtractor(BiFunction<T, String, String> extractor) {
        if (extractor == null) {
            return;
        }
        urlParamExtractors.add(extractor);
    }

    public void addCookieValueExtractor(BiFunction<T, String, String> extractor) {
        if (extractor == null) {
            return;
        }
        cookieValueExtractors.add(extractor);
    }
}
