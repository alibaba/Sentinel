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
package com.alibaba.csp.sentinel.webflow.param;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;
import com.alibaba.csp.sentinel.webflow.rule.WebFlowRule;
import com.alibaba.csp.sentinel.webflow.rule.WebFlowRuleConverter;
import com.alibaba.csp.sentinel.webflow.rule.WebFlowRuleManager;
import com.alibaba.csp.sentinel.webflow.SentinelWebFlowConstants;
import com.alibaba.csp.sentinel.webflow.rule.WebParamItem;

/**
 * @author guanyu
 * @author Eric Zhao
 * @since 1.10.0
 */
public class WebParamParser<T> {

    private static final String UNKNOWN = "unknown";
    private static final String SEPARATOR = ",";

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    private static final String HEADER_HTTP_CLIENT_IP = "HTTP_CLIENT_IP";

    private final RequestItemParser<T> requestItemParser;

    public WebParamParser(RequestItemParser<T> requestItemParser) {
        AssertUtil.notNull(requestItemParser, "requestItemParser cannot be null");
        this.requestItemParser = requestItemParser;
    }

    /**
     * Parse parameters for given resource from the request entity on condition of the rule predicate.
     *
     * @param resource      valid resource name
     * @param request       valid request
     * @param rulePredicate rule predicate indicating the rules to refer, can be null
     * @return the parameter array
     */
    public Map<String, Object> parseParameterFor(String resource, T request, Predicate<WebFlowRule> rulePredicate) {
        if (StringUtil.isEmpty(resource) || request == null) {
            return new HashMap<String, Object>();
        }
        Set<WebFlowRule> webFlowRules = new HashSet<WebFlowRule>();
        Set<Boolean> predSet = new HashSet<Boolean>();
        // TODO: optimize the logic here.
        boolean hasNonParamRule = false;
        for (WebFlowRule rule : WebFlowRuleManager.getRulesForResource(resource)) {
            if (rule.getParamItem() != null) {
                webFlowRules.add(rule);
                predSet.add(rulePredicate == null || rulePredicate.test(rule));
            } else {
                hasNonParamRule = true;
            }
        }

        if (!hasNonParamRule && webFlowRules.isEmpty()) {
            return new HashMap<String, Object>();
        }
        if (predSet.size() > 1 || predSet.contains(false)) {
            return new HashMap<String, Object>();
        }
        Map<String, Object> argMap = new HashMap<String, Object>();
        for (WebFlowRule rule : webFlowRules) {
            WebParamItem paramItem = rule.getParamItem();
            if (paramItem == null) {
                continue;
            }
            String paramValue = parseInternal(paramItem, request);
            // Use the cached param key to reduce memory footprint.
            String paramKey = WebFlowRuleConverter.getCachedConvertedParamKey(paramItem);
            argMap.put(paramKey, paramValue);
        }
        if (hasNonParamRule) {
            // TODO: review the logic here
            argMap.put("default", SentinelWebFlowConstants.WEB_FLOW_NON_PARAM_DEFAULT_KEY);
        }
        return argMap;
    }

    public String parseInternal(WebParamItem item, T request) {
        switch (item.getParseStrategy()) {
            case SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_CLIENT_IP:
                return parseClientIp(item, request);
            case SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_HOST:
                return parseHost(item, request);
            case SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_HEADER:
                return parseHeader(item, request);
            case SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_URL_PARAM:
                return parseUrlParameter(item, request);
            case SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_COOKIE:
                return parseCookie(item, request);
            case SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_BODY_PARAM:
                return parseBody(item, request);
            case SentinelWebFlowConstants.PARAM_PARSE_STRATEGY_PATH_PARAM:
                return parsePath(item, request);
            default:
                return null;
        }
    }


    public String parsePath(/*@Valid*/ WebParamItem item, T request) {
        String pathName = item.getFieldName();
        String pattern = item.getPattern();
        String param = requestItemParser.getPathValue(request, pathName);
        if (StringUtil.isEmpty(pattern)) {
            return param;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), param, pattern);
    }


    public String parseClientIp(/*@Valid*/ WebParamItem item, T request) {
        // X-Forwarded-For请求头获取多级代理转发流程:client、proxy1、proxy2
        String ipAddress = requestItemParser.getHeader(request, HEADER_X_FORWARDED_FOR);
        // 若 X-Forwarded-For 为空尝试自定义头中的IP
        if (StringUtil.isBlank(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = requestItemParser.getHeader(request, HEADER_PROXY_CLIENT_IP);
        }
        // 尝试 Apache HTTP 代理服务器请求头
        if (StringUtil.isBlank(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = requestItemParser.getHeader(request, HEADER_PROXY_CLIENT_IP);
        }
        // 尝试 Apache 代理时 WebLogic 插件的请求头
        if (StringUtil.isBlank(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = requestItemParser.getHeader(request, HEADER_WL_PROXY_CLIENT_IP);
        }
        // 尝试 Nginx 代理使用的请求头
        if (StringUtil.isBlank(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = requestItemParser.getHeader(request, HEADER_X_REAL_IP);
        }
        // 高级匿名代理等情况使用的请求头
        if (StringUtil.isBlank(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = requestItemParser.getHeader(request, HEADER_HTTP_CLIENT_IP);
        }
        // TODO: 如果是如果 localhost (127.0.0.1)，是否需要取本地真实 IP
        if (StringUtil.isBlank(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = requestItemParser.getRemoteAddress(request);
        }

        // 通过多级反向代理的情况,X-Forwarded-For是一串IP,第一个IP为真实IP,IP按照','分割
        if (!StringUtil.isEmpty(ipAddress) && ipAddress.contains(SEPARATOR)) {
            // TODO: optimize here
            ipAddress = ipAddress.split(SEPARATOR)[0].trim();
        }

        String pattern = item.getPattern();
        if (StringUtil.isEmpty(pattern)) {
            return ipAddress;
        }
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), ipAddress, pattern);
    }

    public String parseHeader(/*@Valid*/ WebParamItem item, T request) {
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

    public String parseHost(/*@Valid*/ WebParamItem item, T request) {
        String pattern = item.getPattern();
        String host = requestItemParser.getHeader(request, "Host");
        if (StringUtil.isEmpty(pattern)) {
            return host;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), host, pattern);
    }

    public String parseUrlParameter(/*@Valid*/ WebParamItem item, T request) {
        String paramName = item.getFieldName();
        String pattern = item.getPattern();
        String param = requestItemParser.getUrlParam(request, paramName);
        if (StringUtil.isEmpty(pattern)) {
            return param;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), param, pattern);
    }

    public String parseCookie(/*@Valid*/ WebParamItem item, T request) {
        String cookieName = item.getFieldName();
        String pattern = item.getPattern();
        String param = requestItemParser.getCookieValue(request, cookieName);
        if (StringUtil.isEmpty(pattern)) {
            return param;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), param, pattern);
    }

    public String parseBody(/*@Valid*/ WebParamItem item, T request) {
        String bodyName = item.getFieldName();
        String pattern = item.getPattern();
        String param = requestItemParser.getBodyValue(request, bodyName);
        if (StringUtil.isEmpty(pattern)) {
            return param;
        }
        // Match value according to regex pattern or exact mode.
        return parseWithMatchStrategyInternal(item.getMatchStrategy(), param, pattern);
    }


    static String parseWithMatchStrategyInternal(Integer matchStrategy, String value, String pattern) {
        if (value == null || matchStrategy == null) {
            return null;
        }
        switch (matchStrategy) {
            case SentinelWebFlowConstants.PARAM_MATCH_STRATEGY_EXACT:
                return value.equals(pattern) ? value : SentinelWebFlowConstants.WEB_FLOW_NOT_MATCH_PARAM;
            case SentinelWebFlowConstants.PARAM_MATCH_STRATEGY_CONTAINS:
                return value.contains(pattern) ? value : SentinelWebFlowConstants.WEB_FLOW_NOT_MATCH_PARAM;
            case SentinelWebFlowConstants.PARAM_MATCH_STRATEGY_REGEX:
                Pattern regex = ParamRegexCache.getRegexPattern(pattern);
                if (regex == null) {
                    return value;
                }
                return regex.matcher(value).matches() ? value : SentinelWebFlowConstants.WEB_FLOW_NOT_MATCH_PARAM;
            default:
                return value;
        }
    }

}
