package com.alibaba.csp.sentinel.transport.endpoint;

import java.net.InetSocketAddress;

/**
 * @author Leo Li
 */
public class Endpoint {
    private Protocol protocol;

    private String host;

    private int port;

    public Endpoint(Protocol protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Endpoint{" + "protocol=" + protocol + ", host='" + host + ", port=" + port + '}';
    }
}
