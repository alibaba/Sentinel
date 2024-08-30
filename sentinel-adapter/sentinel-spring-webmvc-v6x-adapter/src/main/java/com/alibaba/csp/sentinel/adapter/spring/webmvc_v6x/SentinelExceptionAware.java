package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Order(-1)
public class SentinelExceptionAware implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        addExceptionToRequest(request, ex);
        return null;
    }

    private void addExceptionToRequest(HttpServletRequest httpServletRequest, Exception exception) {
        if (BlockException.isBlockException(exception)) {
            return;
        }
        httpServletRequest.setAttribute(BaseWebMvcConfig.REQUEST_REF_EXCEPTION_NAME, exception);
    }
}
    