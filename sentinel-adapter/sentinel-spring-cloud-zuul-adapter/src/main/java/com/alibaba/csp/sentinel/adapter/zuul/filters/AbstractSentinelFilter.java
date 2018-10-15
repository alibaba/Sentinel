package com.alibaba.csp.sentinel.adapter.zuul.filters;

import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.netflix.zuul.ZuulFilter;

/**
 * @author tiger
 */
public abstract class AbstractSentinelFilter extends ZuulFilter {

    private SentinelZuulProperties sentinelZuulProperties;

    public SentinelZuulProperties getSentinelZuulProperties() {
        return sentinelZuulProperties;
    }

    public AbstractSentinelFilter(SentinelZuulProperties sentinelZuulProperties) {
        this.sentinelZuulProperties = sentinelZuulProperties;
    }

    @Override
    public boolean shouldFilter() {
        return sentinelZuulProperties.isEnabled();
    }

}
