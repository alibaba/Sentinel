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

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

/**
 * Since request may be reprocessed in flow if any forwarding or including or other action
 * happened (see {@link javax.servlet.ServletRequest#getDispatcherType()}) we will only
 * deal with the initial request. So we use <b>reference count</b> to track in
 * dispathing "onion" though which we could figure out whether we are in initial type "REQUEST".
 * That means the sub-requests which we rarely meet in practice will NOT be recorded in Sentinel.
 * <p>
 * How to implement a forward sub-request in your action:
 * <pre>
 * initalRequest() {
 *     ModelAndView mav = new ModelAndView();
 *     mav.setViewName("another");
 *     return mav;
 * }
 * </pre>
 *
 * @author kaizi2009
 * @since 1.7.1
 */
public abstract class AbstractSentinelInterceptor implements AsyncHandlerInterceptor {

    public static final String SENTINEL_SPRING_WEB_CONTEXT_NAME = "sentinel_spring_web_context";
    private static final String EMPTY_ORIGIN = "";

    private final BaseWebMvcConfig baseWebMvcConfig;

    public AbstractSentinelInterceptor(BaseWebMvcConfig config) {
        AssertUtil.notNull(config, "BaseWebMvcConfig should not be null");
        AssertUtil.assertNotBlank(config.getRequestAttributeName(), "requestAttributeName should not be blank");
        this.baseWebMvcConfig = config;
    }

    /**
     * @param request
     * @param rcKey
     * @param step
     * @return reference count after increasing (initial value as zero to be increased)
     */
    private Integer increaseReferece(HttpServletRequest request, String rcKey, int step) {
        Object obj = request.getAttribute(rcKey);

        if (obj == null) {
            // initial
            obj = Integer.valueOf(0);
        }

        Integer newRc = (Integer)obj + step;
        request.setAttribute(rcKey, newRc);
        return newRc;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (isAsyncHandlerMethod(handler)) {
            return true;
        }

        afterConcurrentHandlingStarted(request, response, handler);
        return true;
    }

    /**
     * Called instead of {@code postHandle} and {@code afterCompletion}
     * when the handler is being executed concurrently.
     * <p>Implementations may use the provided request and response but should
     * avoid modifying them in ways that would conflict with the concurrent
     * execution of the handler. A typical use of this method would be to
     * clean up thread-local variables.
     *
     * @param request  the current request
     * @param response the current response
     * @param handler  the handler (or {@link HandlerMethod}) that started async
     *                 execution, for type and/or instance examination
     * @throws Exception in case of errors
     */
    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String resourceName = getResourceName(request);

            if (StringUtil.isEmpty(resourceName)) {
                return;
            }

            if (increaseReferece(request, this.baseWebMvcConfig.getRequestRefName(), 1) != 1) {
                return;
            }

            // Parse the request origin using registered origin parser.
            String origin = parseOrigin(request);
            String contextName = getContextName(request);
            ContextUtil.enter(contextName, origin);
            Entry entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.IN);
            request.setAttribute(baseWebMvcConfig.getRequestAttributeName(), entry);
        } catch (BlockException e) {
            try {
                handleBlockException(request, response, e);
            } finally {
                ContextUtil.exit();
            }
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
        if (increaseReferece(request, this.baseWebMvcConfig.getRequestRefName(), -1) != 0) {
            return;
        }

        Entry entry = getEntryInRequest(request, baseWebMvcConfig.getRequestAttributeName());
        if (entry == null) {
            // should not happen
            RecordLog.warn("[{}] No entry found in request, key: {}",
                    getClass().getSimpleName(), baseWebMvcConfig.getRequestAttributeName());
            return;
        }

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
        return entryObject == null ? null : (Entry)entryObject;
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

    protected void handleBlockException(HttpServletRequest request, HttpServletResponse response, BlockException e)
        throws Exception {
        if (baseWebMvcConfig.getBlockExceptionHandler() != null) {
            baseWebMvcConfig.getBlockExceptionHandler().handle(request, response, e);
        } else {
            // Throw BlockException directly. Users need to handle it in Spring global exception handler.
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

    protected boolean isAsyncHandlerMethod(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Class<?> returnType = handlerMethod.getReturnType().getParameterType();
        return WebAsyncTask.class.isAssignableFrom(returnType) ||
                Callable.class.isAssignableFrom(returnType) ||
                DeferredResult.class.isAssignableFrom(returnType) ||
                ListenableFuture.class.isAssignableFrom(returnType) ||
                CompletionStage.class.isAssignableFrom(returnType);
    }
}
