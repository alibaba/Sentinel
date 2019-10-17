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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.callback.WebmvcCallbackManager;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.config.SpringWebmvcConfig;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring webmvc interceptor that integrates with Sentinel.
 *
 * @author zhaoyuguang
 */
public class SentinelHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // SentinelHandlerInterceptor will disable when using CommonFilter and SentinelHandlerInterceptor
        if (request.getAttribute(Constants.SENTINEL_REQUEST_ATTR_WEB_SERVLET_ENABLE_KEY) != null) {
            return true;
        }
        String target = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (StringUtil.isEmpty(target)) {
            return true;
        }
        try {
            ContextUtil.enter(SpringWebmvcConfig.SPRING_WEBMVC_CONTEXT_NAME);
            Entry entry = SphU.entry(target);
            request.setAttribute(SpringWebmvcConfig.SENTINEL_REQUEST_ATTR_SPRING_WEBMVC_KEY, entry);
            return true;
        } catch (BlockException ex) {
            WebmvcCallbackManager.getUrlBlockHandler().blocked(request, response, ex);
            ContextUtil.exit();
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Object e = request.getAttribute(SpringWebmvcConfig.SENTINEL_REQUEST_ATTR_SPRING_WEBMVC_KEY);
        if (e == null) {
            return;
        }
        Entry entry = (Entry) e;
        if (ex != null) {
            Tracer.traceEntry(ex, entry);
        }
        entry.exit();
        request.removeAttribute(SpringWebmvcConfig.SENTINEL_REQUEST_ATTR_SPRING_WEBMVC_KEY);
        ContextUtil.exit();
    }
}
