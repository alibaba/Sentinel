package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Make exception visible to Sentinel.SentinelExceptionAware should be front of ExceptionHandlerExceptionResolver
 * whose order is 0 {@link  org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver}
 *
 * @author lemonJ
 */
@Order(-1)
public class SentinelExceptionAware implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        addExceptionToRequest(request, ex);
        return null;
    }

    private void addExceptionToRequest(HttpServletRequest httpServletRequest, Exception exception) {
        if (httpServletRequest == null || exception instanceof BlockException) {
            return;
        }
        httpServletRequest.setAttribute(BaseWebMvcConfig.REQUEST_REF_EXCEPTION_NAME, exception);
    }
}
