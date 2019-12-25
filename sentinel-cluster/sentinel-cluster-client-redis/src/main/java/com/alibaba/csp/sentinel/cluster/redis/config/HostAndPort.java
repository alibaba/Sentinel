package com.alibaba.csp.sentinel.cluster.redis.config;

public class HostAndPort {
    public final String hostText;
    public final int port;

    public HostAndPort(String hostText, int port) {
        this.hostText = hostText;
        this.port = port;
    }

    public String getHostText() {
        return hostText;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "HostAndPort{" +
                "hostText='" + hostText + '\'' +
                ", port=" + port +
                '}';
    }
}
