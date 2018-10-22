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

package com.alibaba.csp.sentinel.adapter.zuul.filters;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.adapter.servlet.util.FilterUtil;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.SentinelFallbackManager;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.SentinelFallbackProvider;
import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

/**
 * This pre filter get an entry of resource,the first order is ServiceId, then API Path.
 * When get a BlockException run fallback logic.
 *
 * @author tiger
 */
public class SentinelPreFilter extends AbstractSentinelFilter {

    private static final String EMPTY_ORIGIN = "";

    private final MockTestService mockTestService;

    private final ProxyRequestHelper proxyRequestHelper;

    public SentinelPreFilter(SentinelZuulProperties sentinelZuulProperties,
                             ProxyRequestHelper proxyRequestHelper,
                             MockTestService mockTestService) {
        super(sentinelZuulProperties);
        this.proxyRequestHelper = proxyRequestHelper;
        this.mockTestService = mockTestService;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    /**
     * This run before route filter so we can get more accurate RT time.
     */
    @Override
    public int filterOrder() {
        return getSentinelZuulProperties().getOrder().getPre();
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        String origin = parseOrigin(ctx.getRequest());
        String serviceTarget = (String) ctx.get(SERVICE_ID_KEY);
        // When serviceId blocked first get the service level fallback provider.
        String fallBackRoute = serviceTarget;
        try {
            RecordLog.info(String.format("[Sentinel Pre Filter] Origin: %s enter ServiceId: %s", origin, serviceTarget));
            ContextUtil.enter(serviceTarget, origin);
            SphU.entry(serviceTarget, EntryType.IN);
            // used for mock test.
            mockTestService.mockTest(serviceTarget);
            String uriTarget = FilterUtil.filterTarget(ctx.getRequest());
            // Clean and unify the URL.
            // For REST APIs, you have to clean the URL (e.g. `/foo/1` and `/foo/2` -> `/foo/:id`), or
            // the amount of context and resources will exceed the threshold.
            UrlCleaner urlCleaner = WebCallbackManager.getUrlCleaner();
            if (urlCleaner != null) {
                uriTarget = urlCleaner.clean(uriTarget);
            }
            RecordLog.info(String.format("[Sentinel Pre Filter] Origin: %s enter Uri Path: %s", origin, uriTarget));
            SphU.entry(uriTarget, EntryType.IN);
            fallBackRoute = uriTarget;
            // used for mock test.
            mockTestService.mockTest(uriTarget);
        } catch (BlockException ex) {
            try {
                RecordLog.warn(String.format("[Sentinel Pre Filter] Block Exception when Origin: %s enter fall back route: %s", origin, fallBackRoute), ex);
                SentinelFallbackProvider sentinelFallbackProvider = SentinelFallbackManager.getFallbackProvider(fallBackRoute);
                ClientHttpResponse clientHttpResponse = sentinelFallbackProvider.fallbackResponse(fallBackRoute, ex);
                LinkedMultiValueMap<String, String> responseHeaders = new LinkedMultiValueMap<String, String>();
                if (clientHttpResponse != null) {
                    int httpCode = clientHttpResponse.getStatusCode().value();
                    InputStream body = clientHttpResponse.getBody();
                    for (Map.Entry<String, List<String>> entry : clientHttpResponse.getHeaders().entrySet()) {
                        responseHeaders.put(entry.getKey(), entry.getValue());
                    }
                    this.proxyRequestHelper.setResponse(httpCode, body, responseHeaders);
                } else {
                    this.proxyRequestHelper.setResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, responseHeaders);
                    RecordLog.warn("[Sentinel Pre Filter] Fall back clientHttpResponse should not be null", ex);
                }
                // prevent routing from running
                ctx.setRouteHost(null);
                ctx.set(SERVICE_ID_KEY, null);
                return clientHttpResponse;
            } catch (IOException e) {
                throw new ZuulRuntimeException(ex);
            }
        } catch (RuntimeException ex) {
            throw new ZuulRuntimeException(ex);
        }
        return null;
    }

    private String parseOrigin(HttpServletRequest request) {
        RequestOriginParser originParser = WebCallbackManager.getRequestOriginParser();
        String origin = EMPTY_ORIGIN;
        if (originParser != null) {
            origin = originParser.parseOrigin(request);
            if (StringUtil.isEmpty(origin)) {
                return EMPTY_ORIGIN;
            }
        }
        return origin;
    }

    /**
     * static method mock is hard. use this service to mock throw {@link BlockException}
     */
    public static class MockTestService {
        String mockTest(String resource) {
            return resource;
        }
    }
}
