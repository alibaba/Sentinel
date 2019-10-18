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
package com.alibaba.scp.sentinel.adapter.spring.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.callback.WebmvcCallbackManager;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.config.SentinelSpringWebmvcConfig;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Spring webmvc interceptor that integrates with Sentinel.
 *
 * @author zhaoyuguang
 */
public class SentinelHandlerInterceptor extends HandlerInterceptorAdapter {

    private final static String COLON = ":";
    private SentinelSpringWebmvcConfig config;

    public SentinelHandlerInterceptor() {
        config = new SentinelSpringWebmvcConfig();
    }

    public SentinelHandlerInterceptor(SentinelSpringWebmvcConfig config) {
        this.config = config == null ? new SentinelSpringWebmvcConfig() : config;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // SentinelHandlerInterceptor will disable when using CommonFilter and SentinelHandlerInterceptor
        if (request.getAttribute(Constants.SENTINEL_REQUEST_ATTR_WEB_SERVLET_ENABLE_KEY) != null) {
            return true;
        }
        String target = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        UrlCleaner urlCleaner = config.getUrlCleaner();
        if (urlCleaner != null) {
            target = urlCleaner.clean(target);
        }
        if (StringUtil.isEmpty(target)) {
            return true;
        }
        String origin = parseOrigin(request);
        try {
            ContextUtil.enter(SentinelSpringWebmvcConfig.SPRING_WEBMVC_CONTEXT_NAME, origin);
            Entry entry = SphU.entry(target);
            int entryChainLength = config.isHttpMethodSpecify() ? 2 : 1;
            Object[] objects = new Object[entryChainLength];
            objects[entryChainLength - 1] = entry;
            if (config.isHttpMethodSpecify()) {
                String httpMethodUrlTarget = request.getMethod().toUpperCase() + COLON + target;
                Entry httpMethodUrlEntry = SphU.entry(httpMethodUrlTarget);
                objects[0] = httpMethodUrlEntry;
            }
            request.setAttribute(SentinelSpringWebmvcConfig.SENTINEL_REQUEST_ATTR_SPRING_WEBMVC_KEY, objects);
            return true;
        } catch (BlockException ex) {
            WebmvcCallbackManager.getUrlBlockHandler().blocked(request, response, ex);
            ContextUtil.exit();
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Object[] objects = (Object[]) request.getAttribute(SentinelSpringWebmvcConfig.SENTINEL_REQUEST_ATTR_SPRING_WEBMVC_KEY);
        if (objects == null) {
            return;
        }
        for (Object o : objects) {
            Entry entry = (Entry) o;
            if (ex != null) {
                Tracer.traceEntry(ex, entry);
            }
            entry.exit();
        }
        request.removeAttribute(SentinelSpringWebmvcConfig.SENTINEL_REQUEST_ATTR_SPRING_WEBMVC_KEY);
        ContextUtil.exit();
    }

    protected String parseOrigin(HttpServletRequest request) {
        RequestOriginParser originParser = config.getOriginParser();
        String origin = EMPTY_ORIGIN;
        if (originParser != null) {
            origin = originParser.parseOrigin(request);
            if (StringUtil.isEmpty(origin)) {
                return EMPTY_ORIGIN;
            }
        }
        return origin;
    }

    private static final String EMPTY_ORIGIN = "";
}
