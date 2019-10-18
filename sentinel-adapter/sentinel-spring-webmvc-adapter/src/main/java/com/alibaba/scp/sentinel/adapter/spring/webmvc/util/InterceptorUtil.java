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
package com.alibaba.scp.sentinel.adapter.spring.webmvc.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.config.SentinelSpringWebmvcConfig;

/**
 * Util class for Spring webmvc interceptor.
 *
 * @author zhaoyuguang
 */
public final class InterceptorUtil {

    public static void blockRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuffer url = request.getRequestURL();

        if ("GET".equals(request.getMethod()) && StringUtil.isNotBlank(request.getQueryString())) {
            url.append("?").append(request.getQueryString());
        }

        if (StringUtil.isBlank(SentinelSpringWebmvcConfig.getBlockPage())) {
            writeDefaultBlockedPage(response);
        } else {
            String redirectUrl = SentinelSpringWebmvcConfig.getBlockPage() + "?http_referer=" + url.toString();
            // Redirect to the customized block page.
            response.sendRedirect(redirectUrl);
        }
    }

    private static void writeDefaultBlockedPage(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(DEFAULT_BLOCK_MSG);
        out.flush();
        out.close();
    }

    public static final String DEFAULT_BLOCK_MSG = "Blocked by Sentinel (flow limiting)";

    private InterceptorUtil() {
    }
}
