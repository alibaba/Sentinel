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
package com.alibaba.csp.sentinel.adapter.spring.webmvc.callback;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.fallback.FallbackRule;
import com.alibaba.csp.sentinel.fallback.FallbackRuleManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Default handler for the blocked request.
 *
 * @author kaizi2009
 */
public class DefaultBlockExceptionHandler implements BlockExceptionHandler {

    public static final String DEFAULT_FALLBACK = "Blocked by Sentinel (flow limiting)";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        // Return 429 (Too Many Requests) by default.
        response.setStatus(429);

        StringBuffer url = request.getRequestURL();

        if ("GET".equals(request.getMethod()) && StringUtil.isNotBlank(request.getQueryString())) {
            url.append("?").append(request.getQueryString());
        }

        doFallback(response, DEFAULT_FALLBACK);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e, String resourceName) throws Exception {
        // Return 200, can use fallback config instead.
        response.setStatus(200);
        StringBuffer url = request.getRequestURL();
        if ("GET".equals(request.getMethod()) && StringUtil.isNotBlank(request.getQueryString())) {
            url.append("?").append(request.getQueryString());
        }
        FallbackRule fallbackRule = FallbackRuleManager.getFallbackRule(resourceName);
        if (StringUtil.isNotEmpty(fallbackRule.getFallback())) {
            if (Constants.NULL_FALLBACK.equals(fallbackRule.getFallback())) {
                doFallback(response, null);
            } else {
                doFallback(response, fallbackRule.getFallback());
            }
        } else {
            doFallback(response, DEFAULT_FALLBACK);
        }
    }

    /**
     * default fallback
     *
     * @param response
     * @throws IOException
     */
    private void doFallback(HttpServletResponse response, String fallback) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(fallback);
        out.flush();
        out.close();
    }

}
