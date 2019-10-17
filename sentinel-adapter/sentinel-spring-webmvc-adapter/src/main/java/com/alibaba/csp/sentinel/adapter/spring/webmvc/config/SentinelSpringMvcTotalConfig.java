package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

/**
 * @author zhangkai
 */
public class SentinelSpringMvcTotalConfig {
    public static final String DEFAULT_TOTAL_TARGET = "spring_mvc_total_url_request";
    public static final String DEFAULT_REQUEST_ATTRIBUTE_NAME = "sentinel_spring_mvc_total_entity_container";

    private String totalTarget = DEFAULT_TOTAL_TARGET;
    private String requestAttributeName = DEFAULT_REQUEST_ATTRIBUTE_NAME;

    public String getTotalTarget() {
        return totalTarget;
    }

    public SentinelSpringMvcTotalConfig setTotalTarget(String totalTarget) {
        this.totalTarget = totalTarget;
        return this;
    }

    public String getRequestAttributeName() {
        return requestAttributeName;
    }

    public SentinelSpringMvcTotalConfig setRequestAttributeName(String requestAttributeName) {
        this.requestAttributeName = requestAttributeName;
        return this;
    }
}
