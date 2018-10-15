package com.alibaba.csp.sentinel.adapter.zuul.filters;

import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;


/**
 * This filter do success routing RT statistic
 *
 * @author tiger
 */
public class SentinelPostFilter extends AbstractSentinelFilter {
    private Logger logger = LoggerFactory.getLogger(SentinelPostFilter.class);

    public SentinelPostFilter(SentinelZuulProperties sentinelZuulProperties) {
        super(sentinelZuulProperties);
    }

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return getSentinelZuulProperties().getOrder().getPost();
    }

    @Override
    public Object run() throws ZuulException {
        logger.info("[Sentinel Post filter] start run");
        if (ContextUtil.getContext() != null) {
            while (ContextUtil.getContext().getCurEntry() != null) {
                logger.info("[Sentinel Post filter] exit current entry:{}",ContextUtil.getContext().getCurEntry().toString());
                ContextUtil.getContext().getCurEntry().exit();
            }
        }
        ContextUtil.exit();
        return null;
    }
}
