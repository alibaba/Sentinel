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
package com.alibaba.csp.sentinel.adapter.gateway.common.param;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public class GatewayParamParser<T> {

    private final RequestItemParser<T> requestItemParser;

    public GatewayParamParser(RequestItemParser<T> requestItemParser) {
        AssertUtil.notNull(requestItemParser, "requestItemParser cannot be null");
        this.requestItemParser = requestItemParser;
    }

    /**
     * Parse parameters for given resource from the request entity on condition of the rule predicate.
     *
     * @param resource      valid resource name
     * @param request       valid request
     * @param rulePredicate rule predicate indicating the rules to refer
     * @return the parameter array
     */
    public Object[] parseParameterFor(String resource, T request, Predicate<GatewayFlowRule> rulePredicate) {
        if (StringUtil.isEmpty(resource) || request == null || rulePredicate == null) {
            return new Object[0];
        }
        Set<GatewayFlowRule> gatewayRules = new HashSet<>();
        Set<Boolean> predSet = new HashSet<>();
        boolean hasNonParamRule = false;
        for (GatewayFlowRule rule : GatewayRuleManager.getRulesForResource(resource)) {
            if (rule.getParamItem() != null) {
                gatewayRules.add(rule);
                predSet.add(rulePredicate.test(rule));
            } else {
                hasNonParamRule = true;
            }
        }
        if (!hasNonParamRule && gatewayRules.isEmpty()) {
            return new Object[0];
        }
        if (predSet.size() > 1 || predSet.contains(false)) {
            return new Object[0];
        }
        int size = hasNonParamRule ? gatewayRules.size() + 1 : gatewayRules.size();
        Object[] arr = new Object[size];
        for (GatewayFlowRule rule : gatewayRules) {
            GatewayParamFlowItem paramItem = rule.getParamItem();
            int idx = paramItem.getIndex();
            String param = parseInternal(paramItem, request);
            arr[idx] = param;
        }
        if (hasNonParamRule) {
            arr[size - 1] = SentinelGatewayConstants.GATEWAY_DEFAULT_PARAM;
        }
        return arr;
    }

    private String parseInternal(GatewayParamFlowItem item, T request) {
        switch (item.getParseStrategy()) {
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP:
                return parseClientIp(item, request);
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HOST:
                return parseHost(item, request);
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER:
                return parseHeader(item, request);
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM:
                return parseUrlParameter(item, request);
            case SentinelGatewayConstants.PARAM_PARSE_STRATEGY_COOKIE:
                return parseCookie(item, request);
            default:
                return null;
        }
    }

    private String parseClientIp(/*@Valid*/ GatewayParamFlowItem item, T request) {
        String clientIp = requestItemParser.getRemoteAddress(request);
        String pattern = item.getPattern();
        if (StringUtil.isEmpty(pattern)) {
            return clientIp;
        }
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), clientIp, pattern);
    }

    private String parseHeader(/*@Valid*/ GatewayParamFlowItem item, T request) {
        String headerKey = item.getFieldName();
        String pattern = item.getPattern();
        // TODO: what if the header has multiple values?
        String headerValue = requestItemParser.getHeader(request, headerKey);
        if (StringUtil.isEmpty(pattern)) {
            return headerValue;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), headerValue, pattern);
    }

    private String parseHost(/*@Valid*/ GatewayParamFlowItem item, T request) {
        String pattern = item.getPattern();
        String host = requestItemParser.getHeader(request, "Host");
        if (StringUtil.isEmpty(pattern)) {
            return host;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), host, pattern);
    }

    private String parseUrlParameter(/*@Valid*/ GatewayParamFlowItem item, T request) {
        String paramName = item.getFieldName();
        String pattern = item.getPattern();
        String param = requestItemParser.getUrlParam(request, paramName);
        if (StringUtil.isEmpty(pattern)) {
            return param;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), param, pattern);
    }

    private String parseCookie(/*@Valid*/ GatewayParamFlowItem item, T request) {
        String cookieName = item.getFieldName();
        String pattern = item.getPattern();
        String param = requestItemParser.getCookieValue(request, cookieName);
        if (StringUtil.isEmpty(pattern)) {
            return param;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), param, pattern);
    }

    private String parseWithMatchStrategyInternal(int matchStrategy, String value, String pattern) {
        if (value == null) {
            return null;
        }
        switch (matchStrategy) {
            case SentinelGatewayConstants.PARAM_MATCH_STRATEGY_EXACT:
                return value.equals(pattern) ? value : SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM;
            case SentinelGatewayConstants.PARAM_MATCH_STRATEGY_CONTAINS:
                return value.contains(pattern) ? value : SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM;
            case SentinelGatewayConstants.PARAM_MATCH_STRATEGY_REGEX:
                Pattern regex = GatewayRegexCache.getRegexPattern(pattern);
                if (regex == null) {
                    return value;
                }
                return regex.matcher(value).matches() ? value : SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM;
            default:
                return value;
        }
    }
}
