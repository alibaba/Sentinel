package com.alibaba.csp.sentinel.transport.endpoint;

/**
 * @author Leo Li
 * @author Yanming Zhou
 */
public enum Protocol {
    HTTP,
    HTTPS;

    public String getProtocol() {
        return name().toLowerCase();
    }
}
