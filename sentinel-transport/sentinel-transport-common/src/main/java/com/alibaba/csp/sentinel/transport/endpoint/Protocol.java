package com.alibaba.csp.sentinel.transport.endpoint;

/**
 * @author Leo Li
 */
public enum Protocol {
    HTTP("http"),
    HTTPS("https");

    private String protocol;

    Protocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }
}
