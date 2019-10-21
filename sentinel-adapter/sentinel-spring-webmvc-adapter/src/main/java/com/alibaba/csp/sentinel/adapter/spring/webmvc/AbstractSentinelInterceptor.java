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
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author kaizi2009
 */
public abstract class AbstractSentinelInterceptor implements HandlerInterceptor {

    public static final String SPRING_MVC_CONTEXT_NAME = "spring_mvc_context";

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    protected void setEntryContainerInReqeust(HttpServletRequest request, String name, EntryContainer entryContainer) {
        Object attrVal = request.getAttribute(name);
        if (attrVal != null) {
            throw new SentinelSpringMvcException("Already exist attribute name '" + name + "' in request");
        }
        request.setAttribute(name, entryContainer);
    }

    protected EntryContainer getEntryContainerInReqeust(HttpServletRequest request, String attrKey) {
        Object entityContainerObject = request.getAttribute(attrKey);
        if (entityContainerObject == null) {
            throw new SentinelSpringMvcException("EntryContainer is null in request");
        }
        return (EntryContainer) entityContainerObject;
    }

    protected void removeEntryContainerInReqeust(HttpServletRequest request, String attrKey) {
        request.removeAttribute(attrKey);
    }
}
