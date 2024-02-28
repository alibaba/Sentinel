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
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x;

import com.alibaba.csp.sentinel.webflow.param.WebParamParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.param.HttpServletRequestItemParser;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Since request may be reprocessed in flow if any forwarding or including or other action
 * happened (see {@link jakarta.servlet.ServletRequest#getDispatcherType()}) we will only
 * deal with the initial request. So we use <b>reference count</b> to track in
 * dispatching "onion" though which we could figure out whether we are in initial type "REQUEST".
 * That means the sub-requests which we rarely meet in practice will NOT be recorded in Sentinel.
 * <p>
 * How to implement a forward sub-request in your action:
 * <pre>
 * initialRequest() {
 *     ModelAndView mav = new ModelAndView();
 *     mav.setViewName("another");
 *     return mav;
 * }
 * </pre>
 *
 * @since 1.8.8
 */
public abstract class AbstractSentinelInterceptor implements HandlerInterceptor {

    public static final String SENTINEL_SPRING_WEB_CONTEXT_NAME = "sentinel_spring_web_context";
    private static final String EMPTY_ORIGIN = "";

    private final BaseWebMvcConfig baseWebMvcConfig;

    protected final WebParamParser<HttpServletRequest> webParamParser;

    public AbstractSentinelInterceptor(BaseWebMvcConfig config) {
        this(config, new WebParamParser<HttpServletRequest>(new HttpServletRequestItemParser()));
    }

    public AbstractSentinelInterceptor(BaseWebMvcConfig config, WebParamParser<HttpServletRequest> webParamParser) {
        AssertUtil.notNull(config, "BaseWebMvcConfig should not be null");
        AssertUtil.assertNotBlank(config.getRequestAttributeName(), "requestAttributeName should not be blank");
        AssertUtil.assertNotNull(webParamParser, "webParamParser should not be null");
        this.baseWebMvcConfig = config;
        this.webParamParser = webParamParser;
    }

    /**
     * @param request
     * @param rcKey
     * @param step
     * @return reference count after increasing (initial value as zero to be increased)
     */
    private Integer increaseReference(HttpServletRequest request, String rcKey, int step) {
        Object obj = request.getAttribute(rcKey);

        if (obj == null) {
            // initial
            obj = Integer.valueOf(0);
        }

        Integer newRc = (Integer) obj + step;
        request.setAttribute(rcKey, newRc);
        return newRc;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String resourceName = getResourceName(request);
        if (StringUtil.isEmpty(resourceName)) {
            return true;
        }
        if (increaseReference(request, this.baseWebMvcConfig.getRequestRefName(), 1) != 1) {
            return true;
        }
        try {
            // Parse the request origin using registered origin parser.
            String origin = parseOrigin(request);
            String contextName = getContextName(request);
            ContextUtil.enter(contextName, origin);

//            Map<String, Object> params = webParamParser.parseParameterFor(resourceName, request, null);

            // Note that AsyncEntry is REQUIRED here (for async Servlet scenarios).
            // TODO: identify whether request is actually ASYNC here.
            Entry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.IN);

            request.setAttribute(baseWebMvcConfig.getRequestAttributeName(), entry);
            return true;
        } catch (BlockException e) {
            try {
                handleBlockException(request, response, resourceName, e);
            } finally {
                ContextUtil.exit();
            }
            return false;
        }
    }

    /**
     * Return the resource name of the target web resource.
     *
     * @param request web request
     * @return the resource name of the target web resource.
     */
    protected abstract String getResourceName(HttpServletRequest request);

    /**
     * Return the context name of the target web resource.
     *
     * @param request web request
     * @return the context name of the target web resource.
     */
    protected String getContextName(HttpServletRequest request) {
        return SENTINEL_SPRING_WEB_CONTEXT_NAME;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        if (increaseReference(request, this.baseWebMvcConfig.getRequestRefName(), -1) != 0) {
            return;
        }

        Entry entry = getEntryInRequest(request, baseWebMvcConfig.getRequestAttributeName());
        if (entry == null) {
            // should not happen
            RecordLog.warn("[{}] No entry found in request, key: {}",
                    getClass().getSimpleName(), baseWebMvcConfig.getRequestAttributeName());
            return;
        }

        // Record the status code here.
//        String resourceName = entry.getResourceWrapper().getName();
//        int status = response.getStatus();
//        StatusCodeMetricManager.getInstance().recordStatusCode(resourceName, status);

        traceExceptionAndExit(entry, ex);
        removeEntryInRequest(request);
        ContextUtil.exit();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    protected Entry getEntryInRequest(HttpServletRequest request, String attrKey) {
        Object entryObject = request.getAttribute(attrKey);
        return entryObject == null ? null : (Entry) entryObject;
    }

    protected void removeEntryInRequest(HttpServletRequest request) {
        request.removeAttribute(baseWebMvcConfig.getRequestAttributeName());
    }

    protected void traceExceptionAndExit(Entry entry, Exception ex) {
        if (entry != null) {
            if (ex != null) {
                Tracer.traceEntry(ex, entry);
            }
            entry.exit();
        }
    }

    protected void handleBlockException(HttpServletRequest request, HttpServletResponse response, String resourceName,
                                        BlockException e)
            throws Exception {
        if (baseWebMvcConfig.getBlockExceptionHandler() != null) {
            baseWebMvcConfig.getBlockExceptionHandler().handle(request, response, resourceName, e);

            // Record status when blocked
//            int status = response.getStatus();
//            StatusCodeMetricManager.getInstance().recordStatusCode(resourceName, status);
        } else {
            // Throw BlockException directly. Users need to handle it in Spring global exception handler.
            // NOTE: the status code statistics will be lost here!
            throw e;
        }
    }

    protected String parseOrigin(HttpServletRequest request) {
        String origin = EMPTY_ORIGIN;
        if (baseWebMvcConfig.getOriginParser() != null) {
            origin = baseWebMvcConfig.getOriginParser().parseOrigin(request);
            if (StringUtil.isEmpty(origin)) {
                return EMPTY_ORIGIN;
            }
        }
        return origin;
    }

}
