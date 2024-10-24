package com.alibaba.csp.sentinel.event.model.impl;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;

public class ClusterFallbackEvent extends SentinelRuleEvent {

    /**
     * resource name.
     */
    private final String resourceName;

    public ClusterFallbackEvent(String resourceName, AbstractRule rule) {
        super(rule);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
