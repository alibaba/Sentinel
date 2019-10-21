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

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelSpringMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerMapping;

/**
 * Spring mvc interceptor that integrates with sentinel.
 *
 * @Author kaizi2009
 */
public class SentinelInterceptor extends AbstractSentinelInterceptor {
    private static final String EMPTY_ORIGIN = "";
    private final static String COLON = ":";
    private SentinelSpringMvcConfig config;

    public SentinelInterceptor(SentinelSpringMvcConfig config) {
        if (config == null) {
            throw new SentinelSpringMvcException("Config can not be null");
        }
        this.config = config;
    }

    public SentinelInterceptor() {
        this(new SentinelSpringMvcConfig());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Entry urlEntry = null;
        Entry httpMethodUrlEntry = null;

        try {
            String target = getTarget(request);

            UrlCleaner urlCleaner = config.getUrlCleaner();
            if (urlCleaner != null) {
                target = urlCleaner.clean(target);
            }

            if (!StringUtil.isEmpty(target)) {
                // Parse the request origin using registered origin parser.
                String origin = parseOrigin(request);
                ContextUtil.enter(SPRING_MVC_CONTEXT_NAME, origin);
                urlEntry = SphU.entry(target, EntryType.IN);
                // Add method specification if necessary
                if (config.isHttpMethodSpecify()) {
                    httpMethodUrlEntry = SphU.entry(request.getMethod().toUpperCase() + COLON + target,
                            EntryType.IN);
                }
            }
            final EntryContainer entryContainer = new EntryContainer().setUrlEntry(urlEntry)
                    .setHttpMethodUrlEntry(httpMethodUrlEntry);
            setEntryContainerInReqeust(request, config.getRequestAttributeName(), entryContainer);
            return true;
        } catch (BlockException e) {
            //Throw BlockException and handle it in spring MVC
            throw e;
        } catch (RuntimeException e1) {
            Tracer.traceEntry(e1, urlEntry);
            Tracer.traceEntry(e1, httpMethodUrlEntry);
            throw e1;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        EntryContainer entryContainer = getEntryContainerInReqeust(request, config.getRequestAttributeName());
        if (entryContainer != null) {
            if (entryContainer.getHttpMethodUrlEntry() != null) {
                entryContainer.getHttpMethodUrlEntry().exit();
            }
            if (entryContainer.getUrlEntry() != null) {
                entryContainer.getUrlEntry().exit();
            }
            removeEntryContainerInReqeust(request, config.getRequestAttributeName());
        }
        ContextUtil.exit();
    }

    /**
     * Get target in HttpServletRequest
     *
     * @param request
     * @return
     * @see org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping
     * request.setAttribute(HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE, matrixVars);
     */
    protected String getTarget(HttpServletRequest request) {
        return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
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

}
