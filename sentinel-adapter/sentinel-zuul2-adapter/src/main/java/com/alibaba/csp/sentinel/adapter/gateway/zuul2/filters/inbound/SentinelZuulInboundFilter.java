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
import java.util.function.Function;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.RequestItemParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.HttpRequestMessageItemParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.ZuulGatewayApiMatcherManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.matcher.HttpRequestMessageApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.SentinelZuul2Constants;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.EntryHolder;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.endpoint.SentinelZuulEndpoint;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpInboundFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.*;

/**
 * The Zuul inbound filter wrapped with Sentinel route and customized API group entries.
 *
 * @author wavesZh
 */
public class SentinelZuulInboundFilter extends HttpInboundFilter {

    private static final String DEFAULT_BLOCK_ENDPOINT_NAME = SentinelZuulEndpoint.class.getCanonicalName();

    private final int order;

    private final String blockedEndpointName;
    /**
     * If the executor is null, flow control action will be performed on I/O thread
     */
    private final Executor executor;
    /**
     * If true, the rest of inbound filters will be skipped when the request is blocked.
     */
    private final boolean fastError;
    private final Function<HttpRequestMessage, String> routeExtractor;

    private final GatewayParamParser<HttpRequestMessage> paramParser;

    /**
     * Constructor of the inbound filter, which extracts the route from the context route VIP attribute by default.
     *
     * @param order the order of the filter
     */
    public SentinelZuulInboundFilter(int order) {
        this(order, m -> m.getContext().getRouteVIP());
    }

    public SentinelZuulInboundFilter(int order, Function<HttpRequestMessage, String> routeExtractor) {
        this(order, null, routeExtractor);
    }

    public SentinelZuulInboundFilter(int order, Executor executor, Function<HttpRequestMessage, String> routeExtractor) {
        this(order, DEFAULT_BLOCK_ENDPOINT_NAME, executor, true, routeExtractor);
    }

    /**
     * Constructor of the inbound filter.
     *
     * @param order the order of the filter
     * @param blockedEndpointName the endpoint to go when the request is blocked
     * @param executor the executor where Sentinel do flow checking. If null, it will be executed in current thread.
     * @param fastError whether the rest of the filters will be skipped if the request is blocked
     * @param routeExtractor the route ID extractor
     */
    public SentinelZuulInboundFilter(int order, String blockedEndpointName, Executor executor, boolean fastError,
                                     Function<HttpRequestMessage, String> routeExtractor) {
        this(order, blockedEndpointName, executor, fastError, routeExtractor, new HttpRequestMessageItemParser());
	}

	public SentinelZuulInboundFilter(int order, String blockedEndpointName, Executor executor, boolean fastError,
                                     Function<HttpRequestMessage, String> routeExtractor, RequestItemParser<HttpRequestMessage> requestItemParser) {
        AssertUtil.notEmpty(blockedEndpointName, "blockedEndpointName cannot be empty");
        AssertUtil.notNull(routeExtractor, "routeExtractor cannot be null");
        AssertUtil.notNull(requestItemParser, "requestItemParser cannot be null");
        this.order = order;
		this.blockedEndpointName = blockedEndpointName;
		this.executor = executor;
		this.fastError = fastError;
		this.routeExtractor = routeExtractor;
		this.paramParser = new GatewayParamParser<>(requestItemParser);
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
        Deque<EntryHolder> holders = new ArrayDeque<>();
        String routeId = routeExtractor.apply(request);
        String fallBackRoute = routeId;
        try {
            if (StringUtil.isNotBlank(routeId)) {
                ContextUtil.enter(GATEWAY_CONTEXT_ROUTE_PREFIX + routeId);
                doSentinelEntry(routeId, RESOURCE_MODE_ROUTE_ID, request, holders);
            }
            Set<String> matchingApis = pickMatchingApiDefinitions(request);
            if (!matchingApis.isEmpty() && ContextUtil.getContext() == null) {
                ContextUtil.enter(SentinelZuul2Constants.ZUUL_DEFAULT_CONTEXT);
            }
            for (String apiName : matchingApis) {
                fallBackRoute = apiName;
                doSentinelEntry(apiName, RESOURCE_MODE_CUSTOM_API_NAME, request, holders);
            }
            return Observable.just(request);
        } catch (BlockException t) {
            context.put(SentinelZuul2Constants.ZUUL_CTX_SENTINEL_BLOCKED_FLAG, Boolean.TRUE);
            context.put(SentinelZuul2Constants.ZUUL_CTX_SENTINEL_FALLBACK_ROUTE, fallBackRoute);
            if (fastError) {
                context.setShouldSendErrorResponse(true);
                context.setErrorEndpoint(blockedEndpointName);
            } else {
                context.setEndpoint(blockedEndpointName);
            }
            return Observable.error(t);
        } finally {
            if (!holders.isEmpty()) {
                context.put(SentinelZuul2Constants.ZUUL_CTX_SENTINEL_ENTRIES_KEY, holders);
            }
            // clear context to avoid another request use incorrect context
            ContextUtil.exit();
        }
    }

    private void doSentinelEntry(String resourceName, final int resType, HttpRequestMessage input, Deque<EntryHolder> holders) throws BlockException {
        Object[] params = paramParser.parseParameterFor(resourceName, input, r -> r.getResourceMode() == resType);
        AsyncEntry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_API_GATEWAY, EntryType.IN, params);
        holders.push(new EntryHolder(entry, params));
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
