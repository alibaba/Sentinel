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

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

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
    private static final Integer PRE_HANDLER_EVENT = 0;
    private static final Integer POST_HANDLER_EVENT = 1;
    private final BaseWebMvcConfig baseWebMvcConfig;

    public AbstractSentinelInterceptor(BaseWebMvcConfig config) {
        AssertUtil.notNull(config, "BaseWebMvcConfig should not be null");
        AssertUtil.assertNotBlank(config.getRequestAttributeName(), "requestAttributeName should not be blank");
        this.baseWebMvcConfig = config;
    }
    
    /**
     * @param request
     * @param rcKey
     * @param eventType
     * @return Stack size after pushing (initial size is zero)
     */
    private Integer pushStack(HttpServletRequest request, String rcKey, Integer eventType) {
        Stack<Integer> stack = (Stack<Integer>) request.getAttribute(rcKey);
        if (stack == null) {
            stack = new Stack<>();
        }
        stack.push(eventType);
        request.setAttribute(rcKey, stack);
        return stack.size();
    }

    /**
     * @param request
     * @param rcKey
     * @return
     */
    private Integer popStack(HttpServletRequest request, String rcKey) {
        Stack<Integer> stack = (Stack<Integer>) request.getAttribute(rcKey);
        if (stack == null) {
            // should not happen
            stack = new Stack<>();
        }
        // async count
        Set<String> set = (Set<String>) request.getAttribute(this.baseWebMvcConfig.getRequestAsyncSet());
        if (set == null) {
            set = new HashSet<>();
        }

        if (stack.size() > 0) {
            int matchedCount = 0;
            int minMatchedNum = set.size();
            Integer lastEvent = stack.pop();
            while (stack.size() > 0) {
                Integer curr = stack.pop();
                if (curr.equals(lastEvent)) {
                    //do nothing
                } else if (matched(lastEvent, curr)) {
                    // Keep trying to match forward until no match
                    while (stack.size() > 0) {
                        curr = stack.pop();
                        if (!matched(lastEvent, curr)) {
                            stack.push(curr);
                            break;
                        }
                    }
                    matchedCount++;
                    if (matchedCount >= minMatchedNum) {
                        break;
                    }
                } else {
                    /**
                     * The type of lastEvent is {@link AbstractSentinelInterceptor#PRE_HANDLER_EVENT},
                     * indicating that an exception occurred in the program.
                     * The postHandler is not executed normally, so it is automatically closed and skipped.
                     */
                    if (matchedCount++ >= minMatchedNum) {
                        break;
                    }
                    lastEvent = curr;
                }
            }
        }
        //clear async set
        removeAttrInRequest(request, baseWebMvcConfig.getRequestAsyncSet());
        return stack.size();
    }

    private boolean matched(Integer last, Integer curr) {
        return last.equals(POST_HANDLER_EVENT) && curr.equals(PRE_HANDLER_EVENT);
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        try {
            String resourceName = getResourceName(request);

            if (StringUtil.isEmpty(resourceName)) {
                return true;
            }

            if (pushStack(request, this.baseWebMvcConfig.getRequestRefName(), PRE_HANDLER_EVENT) != 1) {
                return true;
            }
            
            // Parse the request origin using registered origin parser.
            String origin = parseOrigin(request);
            String contextName = getContextName(request);
            ContextUtil.enter(contextName, origin);
            Entry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.IN);
            request.setAttribute(baseWebMvcConfig.getRequestAttributeName(), entry);
            return true;
        } catch (BlockException e) {
            try {
                handleBlockException(request, response, e);
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
        if (popStack(request, this.baseWebMvcConfig.getRequestRefName()) != 0) {
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
        removeAttrInRequest(request, baseWebMvcConfig.getRequestAttributeName());
        removeAttrInRequest(request, baseWebMvcConfig.getRequestRefName());
        ContextUtil.exit();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        pushStack(request, baseWebMvcConfig.getRequestRefName(), POST_HANDLER_EVENT);
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Set<String> set = (Set<String>) request.getAttribute(baseWebMvcConfig.getRequestAsyncSet());
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(request.getRequestURI());
        request.setAttribute(baseWebMvcConfig.getRequestAsyncSet(), set);
        ContextUtil.exit();
    }

    protected Entry getEntryInRequest(HttpServletRequest request, String attrKey) {
        Object entryObject = request.getAttribute(attrKey);
        return entryObject == null ? null : (Entry)entryObject;
    }

    protected void removeAttrInRequest(HttpServletRequest request, String attrName) {
        request.removeAttribute(attrName);
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

}
