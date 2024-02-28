/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.WebServletLocalConfig;
import com.alibaba.csp.sentinel.setting.adapter.AdapterSettingManager;
import com.alibaba.csp.sentinel.setting.fallback.BlockFallbackConfig.WebBlockFallbackBehavior;
import com.alibaba.csp.sentinel.setting.fallback.BlockFallbackUtils;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import static com.alibaba.csp.sentinel.setting.SentinelAdapterConstants.WEB_FALLBACK_CONTENT_TYPE_JSON;
import static com.alibaba.csp.sentinel.setting.SentinelAdapterConstants.WEB_FALLBACK_CONTENT_TYPE_TEXT;

/**
 * Default handler for the blocked request.
 *
 * @since 1.8.8
 */
public class DefaultBlockExceptionHandler implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, String resourceName, BlockException ex)
        throws Exception {
        StringBuffer url = request.getRequestURL();

        if ("GET".equals(request.getMethod()) && StringUtil.isNotBlank(request.getQueryString())) {
            url.append("?").append(request.getQueryString());
        }

        WebBlockFallbackBehavior b = BlockFallbackUtils.getFallbackBehavior(resourceName, ex);
        if (b != null) {
            writeBlockPageWith(response, b);
        } else {
            if (StringUtil.isBlank(WebServletLocalConfig.getBlockPage())) {
                writeBlockPage(response, WebServletLocalConfig.getBlockPageHttpStatus());
            } else {
                String redirectUrl = WebServletLocalConfig.getBlockPage() + "?http_referer=" + url.toString();
                // Redirect to the customized block page.
                response.sendRedirect(redirectUrl);
            }
        }
    }

    private void writeBlockPageWith(HttpServletResponse response, WebBlockFallbackBehavior b) throws IOException {
        int status = 429;
        if (b.getWebRespStatusCode() != null && b.getWebRespStatusCode() > 0) {
            status = b.getWebRespStatusCode();
        }

        Integer contentType = b.getWebRespContentType();
        if (contentType != null && contentType == WEB_FALLBACK_CONTENT_TYPE_JSON) {
            setContentTypeToJson(response);
        }
        if (contentType != null && contentType == WEB_FALLBACK_CONTENT_TYPE_TEXT) {
            setContentTypeToText(response);
        }
        response.setStatus(status);
        setCharsetToUtf8(response);
        PrintWriter out = response.getWriter();
        out.print(b.getWebRespMessage());
        out.flush();
        out.close();
    }

    private void writeBlockPage(HttpServletResponse response, int httpStatus) throws IOException {
        response.setStatus(httpStatus);
        setCharsetToUtf8(response);

        Integer contentType = AdapterSettingManager.getWebRespContentType();
        if (contentType != null && contentType.equals(WEB_FALLBACK_CONTENT_TYPE_JSON)) {
            setContentTypeToJson(response);
        }
        if (contentType != null && contentType == WEB_FALLBACK_CONTENT_TYPE_TEXT) {
            setContentTypeToText(response);
        }
        String respMessage = valueOrDefault(AdapterSettingManager.getWebRespMessage(), DEFAULT_BLOCK_MSG);

        PrintWriter out = response.getWriter();
        out.print(respMessage);
        out.flush();
        out.close();
    }

    private static void setContentTypeToJson(HttpServletResponse response) {
        response.setContentType("application/json");
    }

    private static void setContentTypeToText(HttpServletResponse response) {
        response.setContentType("text/plain");
    }

    private static void setCharsetToUtf8(HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
    }

    private static <R> R valueOrDefault(R nullable, /*@NonNull*/ R defaultValue) {
        return nullable == null ? defaultValue : nullable;
    }

    private static final String DEFAULT_BLOCK_MSG = "Blocked by Sentinel (flow limiting)";
}
