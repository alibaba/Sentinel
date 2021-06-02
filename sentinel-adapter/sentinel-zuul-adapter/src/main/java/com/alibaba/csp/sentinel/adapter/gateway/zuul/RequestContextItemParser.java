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
package com.alibaba.csp.sentinel.adapter.gateway.zuul;

import javax.servlet.http.Cookie;

import com.alibaba.csp.sentinel.adapter.gateway.common.param.RequestItemParser;

import com.netflix.zuul.context.RequestContext;

/**
 * @author Eric Zhao
 * @since 1.6.0
 */
public class RequestContextItemParser implements RequestItemParser<RequestContext> {

    @Override
    public String getPath(RequestContext requestContext) {
        return requestContext.getRequest().getServletPath();
    }

    @Override
    public String getRemoteAddress(RequestContext requestContext) {
        return requestContext.getRequest().getRemoteAddr();
    }

    @Override
    public String getHeader(RequestContext requestContext, String headerKey) {
        return requestContext.getRequest().getHeader(headerKey);
    }

    @Override
    public String getUrlParam(RequestContext requestContext, String paramName) {
        return requestContext.getRequest().getParameter(paramName);
    }

    @Override
    public String getCookieValue(RequestContext requestContext, String cookieName) {
        Cookie[] cookies = requestContext.getRequest().getCookies();
        if (cookies == null || cookieName == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie != null && cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
