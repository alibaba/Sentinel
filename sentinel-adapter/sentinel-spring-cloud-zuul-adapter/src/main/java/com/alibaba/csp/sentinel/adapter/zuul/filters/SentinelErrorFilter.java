package com.alibaba.csp.sentinel.adapter.zuul.filters;

import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

/**
 * This filter track routing exception and exit entry;
 *
 * @author tiger
 */
public class SentinelErrorFilter extends AbstractSentinelFilter {

    private Logger logger = LoggerFactory.getLogger(SentinelErrorFilter.class);

    public SentinelErrorFilter(SentinelZuulProperties sentinelZuulProperties) {
        super(sentinelZuulProperties);
    }

    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return getSentinelZuulProperties().isEnabled() && ctx.getThrowable() != null;
    }

    @Override
    public int filterOrder() {
        return getSentinelZuulProperties().getOrder().getError();
    }

    /**
     * Trace not {@link BlockException} ex.
     */
    @Override
    public Object run() throws ZuulException {
        try {
            logger.info("[Sentinel Error filter] start run");
            RequestContext ctx = RequestContext.getCurrentContext();
            Throwable throwable = ctx.getThrowable();
            if (throwable != null) {
                logger.info("[Sentinel Error filter] track throwable ex");
                if (!(throwable.getCause().getCause() instanceof BlockException)) {
                    Tracer.trace(throwable.getCause());
                }
            }
        } finally {
            if (ContextUtil.getContext() != null) {
                while (ContextUtil.getContext().getCurEntry() != null) {
                    ContextUtil.getContext().getCurEntry().exit();
                }
            }
            ContextUtil.exit();
        }
        return null;
    }
}
