package com.alibaba.csp.sentinel.adapter.spring.webmvc;


import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.NestedServletException;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * SentinelWebMvcFilter
 * <p>
 * When the {@link SentinelResource} annotation is configured, and the BlockHandler and FallbackHandler are not configured,
 * the {@link BlockException} will be directly thrown.
 * This exception will be wrapped into {@link UndeclaredThrowableException}, and the interface will return a 500 (INTERNAL_SERVER_ERROR) exception.
 * The filter logic is Handle {@link UndeclaredThrowableException} and use the BlockExceptionHandler configured by
 * {@link BaseWebMvcConfig} to return the default status code
 *
 * @author idefav
 */
public class SentinelWebMvcFilter implements Filter {
    @Resource
    @Lazy
    private volatile BaseWebMvcConfig baseWebMvcConfig;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (NestedServletException | UndeclaredThrowableException e) {
            BlockException blockException = null;
            if (e instanceof NestedServletException && e.getCause() instanceof BlockException) {
                blockException = (BlockException) e.getCause();
            }
            if (e instanceof NestedServletException && e.getCause() instanceof UndeclaredThrowableException
                    && (((UndeclaredThrowableException) e.getCause()).getUndeclaredThrowable() instanceof BlockException)) {
                UndeclaredThrowableException undeclaredThrowableException = (UndeclaredThrowableException) e.getCause();
                blockException = (BlockException) undeclaredThrowableException.getUndeclaredThrowable();
            }
            if (e instanceof UndeclaredThrowableException
                    && ((UndeclaredThrowableException) e).getUndeclaredThrowable() instanceof BlockException) {
                blockException = (BlockException) ((UndeclaredThrowableException) e).getUndeclaredThrowable();
            }

            if (ObjectUtils.isEmpty(blockException)) {
                throw e;
            }
            handleBlockException((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, blockException);

        }
    }

    @Override
    public void destroy() {

    }

    private BaseWebMvcConfig getWebMvcConfig(ServletRequest servletRequest) {
        if (baseWebMvcConfig == null) {
            synchronized (this) {
                if (baseWebMvcConfig == null) {
                    ServletContext context = servletRequest.getServletContext();
                    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
                    if (ctx != null) {
                        baseWebMvcConfig = ctx.getBean(SentinelWebMvcConfig.class);
                    }
                }
            }
        }
        return baseWebMvcConfig;

    }

    protected void handleBlockException(HttpServletRequest request, HttpServletResponse response, BlockException e) {
        BaseWebMvcConfig webMvcConfig = getWebMvcConfig(request);
        if (webMvcConfig.getBlockExceptionHandler() != null) {
            try {
                webMvcConfig.getBlockExceptionHandler().handle(request, response, e);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            throw new RuntimeException(e);
        }
    }

}
