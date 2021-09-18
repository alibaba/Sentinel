package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Tank
 * @since 1.8.2
 */
public class SentinelHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {
    private SentinelWebMvcConfig config = new SentinelWebMvcConfig();

    protected Entry getEntryInRequest(HttpServletRequest request, String attrKey) {
        Object entryObject = request.getAttribute(attrKey);
        return entryObject == null ? null : (Entry) entryObject;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Entry entry = getEntryInRequest(request, config.getRequestAttributeName());
        if (entry == null) {
            RecordLog.warn("[{}] No entry found in request, key: {}",
                    getClass().getSimpleName(), config.getRequestAttributeName());
        }
        Tracer.traceEntry(ex, entry);
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
