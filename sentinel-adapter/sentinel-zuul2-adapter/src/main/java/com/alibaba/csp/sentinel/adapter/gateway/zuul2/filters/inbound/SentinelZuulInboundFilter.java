package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.inbound;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

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
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpInboundFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import rx.Observable;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME;

/**
 * InboundFilter for Sentinel.
 *
 * The filter decides which api request should be blocked.
 * When request is blocked, it set a blocked flag then will invoke a endpoint to handle exception, eg: {@link SentinelZuulEndpoint}.
 * Because Zuul2 is based on netty4, a event-drive framework, so we put a deque with asyncEntries in sessionContext to complete when
 * the request invocation is completed.
 *
 * @author wavesZh
 */
public class SentinelZuulInboundFilter extends HttpInboundFilter {

    private static final String DEFAULT_BLOCK_ENDPOINT_NAME = SentinelZuulEndpoint.class.getCanonicalName();

    private final int order;

    private final String blockedEndpointName;

    private final GatewayParamParser<HttpRequestMessage> paramParser = new GatewayParamParser<>(
            new HttpRequestMessageItemParser());

	public SentinelZuulInboundFilter(int order) {
		this(order, DEFAULT_BLOCK_ENDPOINT_NAME);
	}

    public SentinelZuulInboundFilter(int order, String blockedEndpointName) {
        this.order = order;
		this.blockedEndpointName = blockedEndpointName;
	}

    @Override
    public int filterOrder() {
        return order;
    }

    @Override
    public Observable<HttpRequestMessage> applyAsync(HttpRequestMessage request) {
        return Observable.just(request).flatMap(input->apply(input));
    }

    private Observable<HttpRequestMessage> apply(HttpRequestMessage request) {
        SessionContext context = request.getContext();
        String origin = parseOrigin(request);
        Deque<AsyncEntry> asyncEntries = new ArrayDeque<>();
        String fallBackRoute = null;
        try {
//            if (StringUtil.isNotBlank(routeId)) {
//                ContextUtil.enter(GATEWAY_CONTEXT_ROUTE_PREFIX + routeId, origin);
//                doSentinelEntry(routeId, RESOURCE_MODE_ROUTE_ID, ctx, asyncEntries);
//            }
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
			context.put(ZuulConstant.ZUUL_CTX_SENTINEL_BLOCK_FLAG , Boolean.TRUE);
			context.put(ZuulConstant.ZUUL_CTX_SENTINEL_FALLBACK_ROUTE, fallBackRoute);
            context.setEndpoint(blockedEndpointName);
            return Observable.error(t);
        } finally {
            if (!asyncEntries.isEmpty()) {
                context.put(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, asyncEntries);
                // clear context to avoid another invocations use incorrect context
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
