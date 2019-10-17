package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-17
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
        return (EntryContainer)entityContainerObject;
    }
}
