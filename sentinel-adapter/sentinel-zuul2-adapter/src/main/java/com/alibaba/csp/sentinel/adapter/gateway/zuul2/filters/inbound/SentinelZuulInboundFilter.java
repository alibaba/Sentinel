package com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.inbound;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.HttpMessageItemParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.ZuulGatewayApiMatcherManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.api.matcher.RequestContextApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants.ZuulConstant;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpInboundFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import rx.Observable;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.GATEWAY_CONTEXT_ROUTE_PREFIX;
import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME;
import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID;

/**
 * 该 Filter 开启 Sentinel entry. 当处理过程中发生异常
 */
public class SentinelZuulInboundFilter extends HttpInboundFilter {

    private final int order;

    private final GatewayParamParser<HttpRequestMessage> paramParser = new GatewayParamParser<>(
            new HttpMessageItemParser());

    public SentinelZuulInboundFilter(int order) {
        this.order = order;
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
        String routeId = (String) context.get(ZuulConstant.PROXY_ID_KEY);
        String origin = parseOrigin(request);

        Deque<AsyncEntry> asyncEntries = new ArrayDeque<>();
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
            // proxy
//            request.getContext().setEndpoint(ZuulEndPointRunner.PROXY_ENDPOINT_FILTER_NAME);
//            request.getContext().setRouteVIP("api");
//            request.getContext().put(ZuulConstant.ZUUL_CTX_SENTINEL_BLOCK_FLAG, Boolean.FALSE);
            return Observable.just(request);
        } catch (Throwable t) {
            // static request
            // ShouldSendErrorResponse 会停止对接下来的 inbound 类型的 Filter 进行处理？ 是否需要设置为false
//            request.getContext().setShouldSendErrorResponse(true);
            // Endpoint Filter 需要手动设置，默认 ProxyEndpoint
//            request.getContext().setEndpoint(SentinelZuulEndpoint.class.getCanonicalName());
//            request.getContext().put(ZuulConstant.ZUUL_CTX_SENTINEL_BLOCK_FLAG, Boolean.TRUE);
//            if (t instanceof BlockException) {
            System.out.println("block: " + t.getMessage());
            request.getContext().setShouldSendErrorResponse(true);
            request.getContext().put(ZuulConstant.ZUUL_CTX_SENTINEL_FAIL_ROUTE, fallBackRoute);
//            }
            return Observable.error(t);
        } finally {
            if (!asyncEntries.isEmpty()) {
                context.put(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, asyncEntries);
            }
        }
    }

    private void doSentinelEntry(String resourceName, final int resType, HttpRequestMessage input, Deque<AsyncEntry> asyncEntries) throws BlockException {
        Object[] params = paramParser.parseParameterFor(resourceName, input, new Predicate<GatewayFlowRule>() {
            @Override
            public boolean test(GatewayFlowRule r) {
                return r.getResourceMode() == resType;
            }
        });
        asyncEntries.push(SphU.asyncEntry(resourceName, EntryType.IN, 1, params));
    }

    private String parseOrigin(HttpRequestMessage request) {
//        return ZuulGatewayCallbackManager.getOriginParser().parseOrigin(request);
        return "";
    }

    private Set<String> pickMatchingApiDefinitions(HttpRequestMessage message) {
        Set<String> apis = new HashSet<>();
        for (RequestContextApiMatcher matcher : ZuulGatewayApiMatcherManager.getApiMatcherMap().values()) {
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
