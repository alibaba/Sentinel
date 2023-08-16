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
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Spring Web MVC interceptor that integrates with Sentinel.
 *
 * @author kaizi2009
 * @since 1.7.1
 */
public class SentinelWebInterceptor extends AbstractSentinelInterceptor {

    private final SentinelWebMvcConfig config;

    public SentinelWebInterceptor() {
        this(new SentinelWebMvcConfig());
    }

    public SentinelWebInterceptor(SentinelWebMvcConfig config) {
        super(config);
        if (config == null) {
            // Use the default config by default.
            this.config = new SentinelWebMvcConfig();
        } else {
            this.config = config;
        }
    }

    @Override
    protected String getResourceName(HttpServletRequest request) {
        // Resolve the Spring Web URL pattern from the request attribute.
        Object resourceNameObject = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (resourceNameObject == null || !(resourceNameObject instanceof String)) {
            return null;
        }
        String resourceName = (String) resourceNameObject;
        if (config.isAntPathSupport()) {
            resourceName = resourceMatcher(resourceName);
        }
        UrlCleaner urlCleaner = config.getUrlCleaner();
        if (urlCleaner != null) {
            resourceName = urlCleaner.clean(resourceName);
        }
        // Add method specification if necessary
        if (StringUtil.isNotEmpty(resourceName) && config.isHttpMethodSpecify()) {
            resourceName = request.getMethod().toUpperCase() + ":" + resourceName;
        }
        return resourceName;
    }

    @Override
    protected String getContextName(HttpServletRequest request) {
        if (config.isWebContextUnify()) {
            return super.getContextName(request);
        }

        return getResourceName(request);
    }

    private String resourceMatcher(String originUrl) {
        List<String> urls = new ArrayList<>();
        urls.addAll(getUrl(SystemRuleManager.getRules()));
        urls.addAll(getUrl(FlowRuleManager.getRules()));
        urls.addAll(getUrl(DegradeRuleManager.getRules()));
        urls.addAll(getUrl(ParamFlowRuleManager.getRules()));
        for (String url : urls) {
            if (new AntPathMatcher().match(url, originUrl)) {
                return url;
            }
        }
        return originUrl;
    }

    private static List<String> getUrl(List rules) {
        List<String> urls = new ArrayList<>();
        if (rules == null || rules.size() == 0) {
            return urls;
        }
        for (Object ruleObject : rules) {
            if (!(ruleObject instanceof AbstractRule)) {
                continue;
            }
            AbstractRule rule = (AbstractRule) ruleObject;
            if (null == rule.getResource()) {
                continue;
            }
            urls.add(rule.getResource());
        }
        Collections.sort(urls);
        return urls;
    }
}
