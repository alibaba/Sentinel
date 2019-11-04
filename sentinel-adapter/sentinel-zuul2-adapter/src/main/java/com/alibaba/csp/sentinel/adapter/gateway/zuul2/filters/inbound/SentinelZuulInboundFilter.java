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

package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.inbound;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.HttpRequestMessageItemParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.ZuulGatewayApiMatcherManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.matcher.HttpRequestMessageApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.callback.ZuulGatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.ZuulConstant;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.endpoint.SentinelZuulEndpoint;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpInboundFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.*;

/**
 * Zuul2 inboundFilter for Sentinel.
 *
 * @author wavesZh
 */
public class SentinelZuulInboundFilter extends HttpInboundFilter {

    private static final String DEFAULT_BLOCK_ENDPOINT_NAME = SentinelZuulEndpoint.class.getCanonicalName();

    private final int order;

    private final String blockedEndpointName;
    /**
     * if executor is null, flow control action will do on I/O thread
     */
    private final Executor executor;

    private final GatewayParamParser<HttpRequestMessage> paramParser = new GatewayParamParser<>(
            new HttpRequestMessageItemParser());

    public SentinelZuulInboundFilter(int order) {
        this(order, null);
    }

	public SentinelZuulInboundFilter(int order, Executor executor) {
        this(order, DEFAULT_BLOCK_ENDPOINT_NAME, executor);
	}

    public SentinelZuulInboundFilter(int order, String blockedEndpointName, Executor executor) {
        this.order = order;
		this.blockedEndpointName = blockedEndpointName;
		this.executor = executor;
	}

    @Override
    public int filterOrder() {
        return order;
    }

    @Override
    public Observable<HttpRequestMessage> applyAsync(HttpRequestMessage request) {
        if (executor != null) {
            return Observable.just(request).subscribeOn(Schedulers.from(executor)).flatMap(this::apply);
        } else {
            return Observable.just(request).flatMap(this::apply);
        }
    }

    private Observable<HttpRequestMessage> apply(HttpRequestMessage request) {
        SessionContext context = request.getContext();
        String origin = parseOrigin(request);
        Deque<AsyncEntry> asyncEntries = new ArrayDeque<>();
        String routeId = (String) context.get(ZuulConstant.PROXY_ID_KEY);
        String fallBackRoute = routeId;
        try {
            if (StringUtil.isNotBlank(routeId)) {
                ContextUtil.enter(GATEWAY_CONTEXT_ROUTE_PREFIX + routeId, origin);
                doSentinelEntry(routeId, RESOURCE_MODE_ROUTE_ID, request, asyncEntries);
            }
            Set<String> matchingApis = pickMatchingApiDefinitions(request);
            if (!matchingApis.isEmpty() && ContextUtil.getContext() == null) {
                ContextUtil.enter(ZuulConstant.ZUUL_DEFAULT_CONTEXT, origin);
            }
            for (String apiName : matchingApis) {
                fallBackRoute = apiName;
                doSentinelEntry(apiName, RESOURCE_MODE_CUSTOM_API_NAME, request, asyncEntries);
            }
            return Observable.just(request);
        } catch (Throwable t) {
			context.put(ZuulConstant.ZUUL_CTX_SENTINEL_BLOCKED_FLAG, Boolean.TRUE);
			context.put(ZuulConstant.ZUUL_CTX_SENTINEL_FALLBACK_ROUTE, fallBackRoute);
            context.setEndpoint(blockedEndpointName);
            return Observable.error(t);
        } finally {
            if (!asyncEntries.isEmpty()) {
                context.put(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, asyncEntries);
                // clear context to avoid another request use incorrect context
                ContextUtil.replaceContext(null);
            }
        }
    }

    private void doSentinelEntry(String resourceName, final int resType, HttpRequestMessage input, Deque<AsyncEntry> asyncEntries) throws BlockException {
        Object[] params = paramParser.parseParameterFor(resourceName, input, r -> r.getResourceMode() == resType);
        asyncEntries.push(SphU.asyncEntry(resourceName, EntryType.IN, 1, params));
    }

    private String parseOrigin(HttpRequestMessage request) {
        return ZuulGatewayCallbackManager.getOriginParser().parseOrigin(request);
    }

    private Set<String> pickMatchingApiDefinitions(HttpRequestMessage message) {
        Set<String> apis = new HashSet<>();
        for (HttpRequestMessageApiMatcher matcher : ZuulGatewayApiMatcherManager.getApiMatcherMap().values()) {
            if (matcher.test(message)) {
                apis.add(matcher.getApiName());
            }
        }
        return apis;
    }

    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        return true;
    }
}
